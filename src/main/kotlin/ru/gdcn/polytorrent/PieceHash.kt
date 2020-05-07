package ru.gdcn.polytorrent

class PieceHash(val bytes: List<Byte>) {
    init {
        if (bytes.size != 20)
            throw IllegalArgumentException("Piece can't have size other than 20 bytes")
    }

    override fun toString(): String {
        return Utils.byteArrayToString(bytes.toByteArray())
    }
}
