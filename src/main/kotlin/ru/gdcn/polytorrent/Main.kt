package ru.gdcn.polytorrent

import java.io.File
import java.util.*
import kotlin.random.Random


fun main() {
    val peerId = Random.nextBytes(20)
    val file = File("torrents/ubuntu20.torrent")
    val metafile = Metadata(file)

    val manager = TrackerManager(metafile, peerId)
    val announceInfo: Optional<AnnounceInfo> = manager.getAnnounceInfo()
    if (announceInfo.isPresent){
        println(announceInfo.get().complete)
        println(announceInfo.get().incomplete)
        println(announceInfo.get().interval)
        println(announceInfo.get().peers.joinToString("\n"))
    }

}
