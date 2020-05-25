package ru.gdcn.polytorrent

import java.io.File
import java.util.*
import kotlin.random.Random


fun main() {
    val peerId = Random.nextBytes(20)
    val file = File("torrents/rec.torrent")
    val metafile = Metadata(file)
    println(metafile.info.isSingleFile)
    metafile.info.fileDatas.forEach {
        println(it.path.joinToString("/"))
        println(it.length)
        println(it.md5sum)
    }
//    println(metafile.info.pieceHashes)
//    val manager = TrackerManager(metafile, peerId)
//    val announceInfo: AnnounceInfo = manager.getAnnounceInfo()
//    println(announceInfo.complete)
//    println(announceInfo.incomplete)
//    println(announceInfo.interval)
//    println(announceInfo.peers.joinToString("\n"))
}
