package ru.gdcn.polytorrent

import java.io.File
import java.net.URLEncoder
import java.util.*
import kotlin.random.Random


fun main() {
    val peerId = Random.nextBytes(20)
    val file = File("torrents/rec.torrent")
    val metafile = Metafile(file)

    val manager = TrackerManager(metafile, peerId)
    val announceInfo = manager.getAnnounceInfo()
    println(announceInfo.complete)
    println(announceInfo.incomplete)
    println(announceInfo.interval)
    println(announceInfo.peers.joinToString("\n"))
}
