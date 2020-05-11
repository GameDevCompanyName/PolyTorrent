package ru.gdcn.polytorrent

import java.io.File
import java.util.*
import kotlin.random.Random


fun main() {
    val peerId = Random.nextBytes(20)
    val file = File("torrents/ubuntu20.torrent")
    val metafile = Metadata(file)

    val manager = TrackerManager(metafile, peerId)
    val announceInfo: AnnounceInfo = manager.getAnnounceInfo()
    println(announceInfo.complete)
    println(announceInfo.incomplete)
    println(announceInfo.interval)
    println(announceInfo.peers.joinToString("\n"))

}
