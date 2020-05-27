package ru.gdcn.polytorrent

import org.apache.logging.log4j.LogManager
import ru.gdcn.polytorrent.pwp.Peer
import ru.gdcn.polytorrent.pwp.TcpConnect
import java.io.Closeable
import java.util.*
import java.util.concurrent.Semaphore

class PeerManager(peers: Collection<Peer>) : Closeable{

    private val logger = LogManager.getLogger(PeerManager::class.java)
    private val peerQueue : Queue<Peer> = LinkedList()
    private val openedConnections : MutableSet<TcpConnect> = mutableSetOf()
    private val semaphore = Semaphore(TorrentConfig.MAX_PEER_CONNECTIONS)

    init {
        peers.forEach { peerQueue.offer(it) }
    }

    fun start(){
        while (peerQueue.isNotEmpty()){
            val nextPeer = peerQueue.poll()
            val newConnection = TcpConnect(nextPeer, semaphore)
            openedConnections.add(newConnection)
            newConnection.run()
        }
        logger.warn("Пиры закончились.")
    }

    override fun close() {
        openedConnections.forEach{ it.close() }
    }

}