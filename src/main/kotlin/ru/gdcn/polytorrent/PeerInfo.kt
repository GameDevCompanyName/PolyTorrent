package ru.gdcn.polytorrent

import java.net.InetAddress

data class PeerInfo(
    val peerId: String,
    val ip: InetAddress,
    val port: Int
) {

    override fun toString(): String {
        return "PeerInfo(peerId='$peerId', ip='$ip', port=$port)"
    }
}
