package ru.gdcn.polytorrent

class Piece(val bytes: List<Byte>) {
    init {
        if (bytes.size != 20)
            throw IllegalArgumentException("ru.gdcn.polytorrent.Piece can't have size other than 20 bytes")
    }

    override fun toString(): String {
        return Utils.byteArrayToString(bytes.toByteArray())
    }


}
