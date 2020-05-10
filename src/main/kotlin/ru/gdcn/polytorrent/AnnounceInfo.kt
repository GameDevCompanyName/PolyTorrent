package ru.gdcn.polytorrent

import java.net.InetAddress

class AnnounceInfo(val dictionary: Map<String, Any>) {

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
        get() = dictionary["complete"].toString().toInt()

    val incomplete: Int
        get() = dictionary["incomplete"].toString().toInt()

    val peers: List<PeerInfo>
        get() {
            try {
                val peerDictionaryList = dictionary["peers"] as List<Map<String, Any>>
                return peerDictionaryList.map {
                    PeerInfo(
                        it["peer id"].toString(),
                        it["ip"].toString(),
                        it["port"].toString().toInt()
                    )
                }
            } catch (e: Exception) {
                val peerBytes = dictionary["peers"].toString().toByteArray(Charsets.US_ASCII)
                if (peerBytes.size % 6 != 0) {
                    throw IllegalStateException("Если пиры в бинарной форме, количество байт должно быть кратно 6")
                }

                val peerList = mutableListOf<PeerInfo>()

                for (i in 0 until peerBytes.size / 6) {
                    val currentBytes = peerBytes.copyOfRange(i * 6, (i + 1) * 6)
                    val ipBytes = currentBytes.copyOfRange(0, 4)
                    val portBytes = currentBytes.copyOfRange(4, 6)
                    println(ipBytes[0])
                    println(ipBytes[1])
                    println(ipBytes[2])
                    println(ipBytes[3])
                    val ip = InetAddress.getByAddress(ipBytes).hostAddress
                    val port = Utilities.getIntFromTwoBytes(portBytes[0], portBytes[1])
                    peerList.add(
                        PeerInfo(Utils.UNKNOWN_PEER_ID, ip, port)
                    )
                }

                return peerList
            }
        }

}