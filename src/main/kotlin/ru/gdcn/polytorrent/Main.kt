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
    manager.getAnnounceInfo()
}
