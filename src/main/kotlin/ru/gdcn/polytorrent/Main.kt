package ru.gdcn.polytorrent

import java.io.File
import kotlin.random.Random


fun main() {
    val peerId = Random.nextBytes(20)
    val file = File("thumper.torrent")
    val metafile = Metafile(file)

    val fineHash = "[2cfd9f1709f040aa24b363a7afcf8e4a12a5da60]"
    println(fineHash)
    println(Utils.byteArrayToString(metafile.infoSha1))

//    val trackerManager = ru.gdcn.polytorrent.TrackerManager(metafile, peerId)
//    trackerManager.getAnnounceInfo()
}
