package ru.gdcn.polytorrent

import com.dampcake.bencode.Bencode
import com.dampcake.bencode.BencodeException
import com.dampcake.bencode.Type
import khttp.*
import khttp.responses.Response
import ru.gdcn.polytorrent.Utilities.byteArrayToURLString
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.*
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.Future
import kotlin.streams.toList

class TrackerManager(private val metafile: Metadata, private val peerId: ByteArray) {
    private val trackerList: MutableList<String> = mutableListOf()

    init {
        if (metafile.announceList.isEmpty()) {
            trackerList.add(metafile.announce)
        } else {
            trackerList.addAll(metafile.announceList)
            trackerList.shuffle()
        }
    }

    fun getAnnounceInfo(): AnnounceInfo {
        val futures = mutableListOf<Future<Optional<Response>>>()
        val executorService = Executors.newFixedThreadPool(4)

        for (tracker in trackerList) {
            if (!tracker.startsWith("http")) {
                continue
            }

            val future: Future<Optional<Response>> = executorService.submit<Optional<Response>> {
                return@submit askTracker(tracker)
            }
            futures.add(future)
        }

        //Ждём пока все фучуры выполнятся
        for (future in futures) {
            future.get()
        }

        //Вырубаем работяг
        executorService.shutdown()

//        val dictionaries = mutableListOf<MutableMap<String, Any>>()
        //А теперь точно всё выполнилось и проверяем чё получили
        for (future in futures) {
            if (future.get().isEmpty){
                continue
            } else {
                try {
                    val responseDictionary = Bencode().decode(future.get().get().content, Type.DICTIONARY)
                    if (responseDictionary.containsKey("failure reason")) {
                    println("Ошибка от сервера: ${responseDictionary["failure reason"].toString()}")
//                    trackerList.remove(urlString)
                        continue
                    } else {
//                        dictionaries.add(responseDictionary)
                        return AnnounceInfo(responseDictionary)
                    }
                } catch (e: BencodeException){
                    //Не удалось распарсить ответ
                    continue
                }
            }
        }

        throw IllegalStateException("Не удалось получить информацию об анонсах")
    }

    private fun askTracker(urlString: String): Optional<Response> {
        val parameters = mutableMapOf<String, String>()
        parameters["info_hash"] = byteArrayToURLString(metafile.infoHash.toByteArray())
        parameters["peer_id"] = byteArrayToURLString(peerId)
        parameters["port"] = Utils.PORT
        parameters["uploaded"] = "0"
        parameters["downloaded"] = "0"
        parameters["left"] = metafile.info.length.toString()
//        parameters["compact"] = "1"

        val encodedUrl = urlString + "?" + parameters.entries.stream()
            .map { it.key + "=" + it.value }
            .toList()
            .joinToString("&")

//        println(parameters.entries.joinToString())

        return try {
            val response = get(encodedUrl, timeout = Utils.TRACKER_TIMEOUT)
            Optional.of(response)
        } catch (e: SocketTimeoutException) {
            println("Трекер не ответил")
            Optional.empty()
        } catch (e: UnknownHostException) {
            println("Не удалось узнать адрес хоста")
            Optional.empty()
        } catch (e: ConnectException) {
            println("Ошибка подключения")
            Optional.empty()
        }
    }

}