package ru.gdcn.polytorrent

import ru.gdcn.polytorrent.pwp.Peer
import ru.gdcn.polytorrent.pwp.TcpConnect
import java.io.Closeable
import java.util.*
import java.util.concurrent.Semaphore

class PeerManager(peers: Collection<Peer>) : Closeable{

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
    }

    override fun close() {
        openedConnections.forEach{ it.close() }
    }

}