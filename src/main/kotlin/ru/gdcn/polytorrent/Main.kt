package ru.gdcn.polytorrent

import java.io.File
import kotlin.random.Random


fun main() {
    val peerId = Random.nextBytes(20)
    val file = File("torrents/thumper.torrent")
    val metafile = Metafile(file)

    val manager = TrackerManager(metafile, peerId)
    manager.getAnnounceInfo()
}
