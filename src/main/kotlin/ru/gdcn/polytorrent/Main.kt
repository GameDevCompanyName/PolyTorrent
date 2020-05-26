package ru.gdcn.polytorrent

import org.slf4j.LoggerFactory
import ru.gdcn.polytorrent.filesaver.FileSaver
import ru.gdcn.polytorrent.pwp.SessionInfo
import java.io.File
import java.lang.Exception
import kotlin.random.Random


fun main() {



    val peerId = Random.nextBytes(20)
    val file = File("torrents/ubuntu20.torrent")
    val metafile = Metadata(file)
    SessionInfo.infoHash = metafile.infoHash
    SessionInfo.ourPeerId = peerId.toTypedArray()
    val saver = FileSaver.getInstance(metafile, File("."))
    SessionInfo.fileSaver = saver
    saver.init()
    val trackerManager = TrackerManager(metafile, peerId)
    val announceInfo: AnnounceInfo = trackerManager.getAnnounceInfo()
    println(announceInfo.complete)
    println(announceInfo.incomplete)
    println(announceInfo.interval)
    println(announceInfo.peers.joinToString("\n"))

    val peerManager = PeerManager(announceInfo.peers)
    try {
        peerManager.start()
    } catch (e: Exception) {
        peerManager.close()
    }

}
