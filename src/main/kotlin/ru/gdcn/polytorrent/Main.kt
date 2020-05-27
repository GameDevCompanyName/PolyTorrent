package ru.gdcn.polytorrent

import org.apache.log4j.BasicConfigurator
import org.apache.log4j.LogManager
import ru.gdcn.polytorrent.filesaver.FileSaver
import ru.gdcn.polytorrent.pwp.SessionInfo
import java.io.File
import java.lang.Exception
import kotlin.random.Random


fun main() {

    BasicConfigurator.configure()
    LogManager.getRootLogger().level = TorrentConfig.LOGGING_LEVEL

    val peerId = Random.nextBytes(20)
    val file = File("torrents/ubuntu20.torrent")
    val metafile = Metadata(file)
    println(metafile.info.pieceHashes.size)
    SessionInfo.infoHash = metafile.infoHash
    SessionInfo.ourPeerId = peerId.toTypedArray()
    SessionInfo.NUM_OF_BLOCKS = metafile.blockQuantity
    SessionInfo.totalPieces = metafile.info.pieceHashes.size

    val trackerManager = TrackerManager(metafile, peerId)
    val announceInfo: AnnounceInfo = trackerManager.getAnnounceInfo()
    println(announceInfo.complete)
    println(announceInfo.incomplete)
    println(announceInfo.interval)
    println(announceInfo.peers.joinToString("\n"))

    val saver = FileSaver.getInstance(metafile, File("."))
    SessionInfo.fileSaver = saver
    saver.init()

    val peerManager = PeerManager(announceInfo.peers)
    try {
        peerManager.start()
    } catch (e: Exception) {
        peerManager.close()
    }

}
