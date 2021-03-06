package ru.gdcn.polytorrent

import ru.gdcn.polytorrent.pwp.Peer
import java.lang.NumberFormatException
import java.net.InetAddress

class AnnounceInfo(val dictionary: Map<String, Any>) {

    var peers: MutableList<Peer>

    init {
        try {
            val peerDictionaryList = dictionary["peers"] as List<Map<String, Any>>
            peers = peerDictionaryList.map {
                Peer(
                    InetAddress.getByName(it["ip"].toString()),
                    it["port"].toString().toInt(),
                    it["peer id"].toString().toByteArray(Charsets.US_ASCII).toTypedArray()
                )
            }.toMutableList()
        } catch (e: Exception) {
            val peerBytes = dictionary["peers"].toString().toByteArray(Charsets.US_ASCII)
            if (peerBytes.size % 6 != 0) {
                throw IllegalStateException("Если пиры в бинарной форме, количество байт должно быть кратно 6")
            }

            val peerList = mutableListOf<Peer>()

            for (i in 0 until peerBytes.size / 6) {
                val currentBytes = peerBytes.copyOfRange(i * 6, (i + 1) * 6)
                val ipBytes = currentBytes.copyOfRange(0, 4)
                val portBytes = currentBytes.copyOfRange(4, 6)
                println(ipBytes[0])
                println(ipBytes[1])
                println(ipBytes[2])
                println(ipBytes[3])
                val ip = InetAddress.getByAddress(ipBytes)
                val port = Utilities.getIntFromTwoBytes(portBytes[0], portBytes[1])
                peerList.add(
                    Peer(
                        ip,
                        port,
                        Utils.UNKNOWN_PEER_ID.toByteArray(Charsets.US_ASCII).toTypedArray()
                    )
                )
            }
            peers = peerList
        }
    }

    val interval: Int
        get() = dictionary["interval"].toString().toInt()

    val minInterval: Int?
        get() {
            if (!dictionary.containsKey("min interval"))
                return null
            return dictionary["min interval"].toString().toInt()
        }

    val trackerId: String?
        get() {
            if (!dictionary.containsKey("tracker id"))
                return null
            return dictionary["tracker id"].toString()
        }

    val complete: Int
        get() {
            return try {
                dictionary["complete"].toString().toInt()
            } catch (e: NumberFormatException) {
                0
            }
        }

    val incomplete: Int
        get() {
            return try {
                dictionary["incomplete"].toString().toInt()
            } catch (e: NumberFormatException) {
                0
            }
        }

}