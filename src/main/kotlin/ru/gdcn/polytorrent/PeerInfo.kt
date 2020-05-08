package ru.gdcn.polytorrent

data class PeerInfo(
    val peerId: String,
    val ip: String,
    val port: Int
) {

    override fun toString(): String {
        return "PeerInfo(peerId='$peerId', ip='$ip', port=$port)"
    }
}
