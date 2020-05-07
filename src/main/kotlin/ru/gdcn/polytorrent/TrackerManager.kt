package ru.gdcn.polytorrent

import com.dampcake.bencode.Bencode
import com.dampcake.bencode.Type
import khttp.*
import khttp.responses.Response
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class TrackerManager(private val metafile: Metafile, private val peerId: ByteArray) {
    private val trackerList: MutableList<String> = mutableListOf()

    init {
//        trackerList.add(metafile.announce)
//        trackerList.addAll(metafile.announceList)
//        trackerList.shuffle()
        trackerList.add("http://tracker.dler.org:6969/announce")
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
            val responseDictionary = Bencode().decode(response.text.toByteArray(), Type.DICTIONARY)
            if (responseDictionary.containsKey("failure reason")) {
                println("Ошибка от сервера $urlString: ${responseDictionary["failure reason"].toString()}")
                trackerList.remove(urlString)
                continue
            }
            if (responseDictionary.containsKey("peers")) {
                println("Получили данные о пирах от $urlString")
                announceInfo = AnnounceInfo()
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
        parameters["info_hash"] = metafile.info.md5sum
        parameters["peer_id"] = Utils.byteArrayToString(peerId)
        parameters["port"] = Utils.PORT
        parameters["uploaded"] = "0"
        parameters["downloaded"] = "0"
        parameters["left"] = metafile.info.length.toString()

        println(parameters.entries.joinToString())

        return try {
            val response = get(urlString, parameters, timeout = Utils.TRACKER_TIMEOUT)
            response
        } catch (e: SocketTimeoutException) {
            println("Трекер не ответил")
            null
        } catch (e: UnknownHostException) {
            println("Не удалось узнать адрес хоста")
            null
        }
    }

}