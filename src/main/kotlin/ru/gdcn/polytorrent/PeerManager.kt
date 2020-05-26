package ru.gdcn.polytorrent

import com.sun.jmx.remote.internal.ArrayQueue
import ru.gdcn.polytorrent.pwp.Peer
import java.io.Closeable
import java.util.*
import java.util.concurrent.LinkedBlockingDeque
import java.util.concurrent.Semaphore
import kotlin.collections.ArrayDeque

class PeerManager(peers: Collection<Peer>) : Closeable{

    private val peerQueue : Queue<Peer> = LinkedList()
    private val openedConnections : MutableSet<Peer> = mutableSetOf()
    private val semaphore = Semaphore(TorrentConfig.MAX_PEER_CONNECTIONS)

    init {
        peers.forEach { peerQueue.offer(it) }
    }

    fun start(){
        while (peerQueue.isNotEmpty()){
            val nextPeer = peerQueue.poll()

        }
    }

    override fun close() {
        TODO("Not yet implemented")
    }

}