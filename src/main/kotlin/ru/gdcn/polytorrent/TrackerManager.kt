package ru.gdcn.polytorrent

import com.dampcake.bencode.Bencode
import com.dampcake.bencode.Type
import khttp.*
import khttp.responses.Response
import ru.gdcn.polytorrent.Utilities.byteArrayToURLString
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import kotlin.streams.toList

class TrackerManager(private val metafile: Metadata, private val peerId: ByteArray) {
    private val trackerList: MutableList<String> = mutableListOf()

    init {
        if (metafile.announceList.isEmpty()){
            trackerList.add(metafile.announce)
        } else {
            trackerList.addAll(metafile.announceList)
            trackerList.shuffle()
        }
    }

    fun getAnnounceInfo(): AnnounceInfo {
        var announceInfo: AnnounceInfo? = null
        while (announceInfo == null && trackerList.isNotEmpty()) {
            val urlString = trackerList.first()
            if (!urlString.startsWith("http")) {
                trackerList.remove(urlString)
                continue
            }
            println("Спрашиваем $urlString")
            val response: Response? = askTracker(urlString)
            if (response == null) {
                println("Ошибка соединения с сервером $urlString")
                trackerList.remove(urlString)
                continue
            }
            println(response.text)

            val responseDictionary = Bencode().decode(response.content, Type.DICTIONARY)
            if (responseDictionary.containsKey("failure reason")) {
                println("Ошибка от сервера $urlString: ${responseDictionary["failure reason"].toString()}")
                trackerList.remove(urlString)
                continue
            }
            if (responseDictionary.containsKey("peers")) {
                println("Получили данные о пирах от $urlString")
                return AnnounceInfo(responseDictionary)
                //TODO OPTIONAL
                break
            }
        }
        if (announceInfo == null) {
            throw IllegalStateException("Информация аннонса недоступна")
        }
        return announceInfo
    }

    private fun askTracker(urlString: String): Response? {
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

        println(parameters.entries.joinToString())

        return try {
            val response = get(encodedUrl, timeout = Utils.TRACKER_TIMEOUT)
            response
        } catch (e: SocketTimeoutException) {
            println("Трекер не ответил")
            null //TODO OPTIONAL
        } catch (e: UnknownHostException) {
            println("Не удалось узнать адрес хоста")
            null
        } catch (e: ConnectException) {
            println("Ошибка подключения")
            null
        }
    }

}