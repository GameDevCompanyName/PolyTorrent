package ru.gdcn.polytorrent

object Utils {

    const val TRACKER_TIMEOUT: Double = 5.0
    const val PORT = "6000"

    fun byteArrayToString(array: ByteArray): String {
        return "[${array.joinToString("") { "%02x".format(it) }}]"
    }

}