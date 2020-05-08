package ru.gdcn.polytorrent

import java.net.URLEncoder


object Utils {

    const val TRACKER_TIMEOUT: Double = 2.5
    const val PORT = "6000"
    const val UNKNOWN_PEER_ID = "0000000000000000000000000000000000000000"

    fun byteArrayToString(array: ByteArray): String {
        return array.joinToString("") { "%02x".format(it) }
    }

    fun urlEncodeValue(value: String): String {
        return URLEncoder.encode(value, Charsets.UTF_8.toString())
    }

}