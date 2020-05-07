package ru.gdcn.polytorrent

import com.dampcake.bencode.Bencode
import com.dampcake.bencode.Type
import java.io.File
import java.io.FileInputStream
import java.security.MessageDigest

class Metafile(val metafile: File) {
    private var dictionary: Map<String, Any>

    init {
        val content = metafile.inputStream().readAllBytes()
        dictionary = Bencode(Charsets.US_ASCII).decode(content, Type.DICTIONARY)
    }

    val announce: String
        get() = dictionary["announce"].toString()

    val announceList: List<String>
        get() = (dictionary.getOrDefault(
            "announce-list",
            listOf(emptyList<String>())
        ) as List<List<String>>).map { it.first() }

    val comment: String
        get() = dictionary.getOrDefault("comment", "").toString()

    val createdBy: String
        get() = dictionary.getOrDefault("created by", "").toString()

    val creationDate: String
        get() = dictionary.getOrDefault("creation date", "").toString()

    val info: Metainfo
        get() = Metainfo(dictionary["info"] as Map<String, Any>)

    val infoSha1: ByteArray
        get() {
            val inputStream = FileInputStream(metafile)
            val fileBytes = inputStream.readAllBytes()
            val firstIndex = String(fileBytes).indexOf("4:infod6:") + 6
            val subBytes = fileBytes.copyOfRange(firstIndex, fileBytes.size - 1)
            val hasher = MessageDigest.getInstance("SHA-1")
            return hasher.digest(subBytes)
        }

    override fun toString(): String {
        return "{$announce\n$announceList\n$comment\n$createdBy\n$creationDate\n$info}"
    }

    class Metainfo(map: Map<String, Any>) {
        private var dictionary: Map<String, Any> = map

        val length: Long
            get() = dictionary["length"].toString().toLong()

        val md5sum: String
            get() = dictionary.getOrDefault("md5sum", "").toString()

        val name: String
            get() = dictionary["name"].toString()

        val pieceLength: Int
            get() = dictionary["piece length"].toString().toInt()

        val pieceHashes: List<PieceHash>
            get() {
                val byteParts = (dictionary["pieces"] as String).toByteArray(Charsets.US_ASCII).toList()
                if (byteParts.size % 20 != 0)
                    throw IllegalArgumentException("Pieces should have size 20 bytes each")
                if (byteParts.isEmpty())
                    throw IllegalArgumentException("Pieces should not be empty")

                val list = mutableListOf<PieceHash>()
                for (i in 0 until (byteParts.size / 20)) {
                    val currentParts = byteParts.subList(20 * i, 20 * (i + 1))
                    list.add(PieceHash(currentParts))
                }
                return list
            }

        override fun toString(): String {
            return "{$length,$md5sum,$name,$pieceLength,${pieceHashes.joinToString()}}"
        }
    }

}
