package ru.gdcn.polytorrent

import com.dampcake.bencode.Bencode
import com.dampcake.bencode.Type
import org.slf4j.LoggerFactory
import ru.gdcn.polytorrent.filesaver.Piece
import ru.gdcn.polytorrent.torrent.FileData
import java.io.File
import java.io.FileInputStream
import java.security.MessageDigest

class Metadata(private val metafile: File) {

    val BLOCK_SIZE = 16384
    private var dictionary: Map<String, Any>
    private val logger = LoggerFactory.getLogger(this::class.java)

    init {
        logger.info("Пытаюсь распарсить метафайл")
        val content = metafile.inputStream().readAllBytes()
        dictionary = Bencode(Charsets.US_ASCII).decode(content, Type.DICTIONARY)
//        dictionary = Bencode().decode(content, Type.DICTIONARY)
    }

    val announce: String
        get() = dictionary["announce"].toString()

    val announceList: List<String>
        get() {
            val announceLists: List<List<String>> = dictionary.getOrDefault(
                "announce-list",
                listOf(emptyList<String>())
            ) as List<List<String>>
            return if (announceLists.isEmpty()) {
                emptyList()
            } else {
                val resultList = mutableListOf<String>()
                for (list in announceLists) {
                    resultList.addAll(list)
                }
                resultList
            }
        }

    val comment: String
        get() = dictionary.getOrDefault("comment", "").toString()

    val createdBy: String
        get() = dictionary.getOrDefault("created by", "").toString()

    val creationDate: String
        get() = dictionary.getOrDefault("creation date", "").toString()

    val info: Metainfo
        get() = Metainfo(dictionary["info"] as Map<String, Any>)

    val infoHash: Array<Byte>
        get() {
            val inputStream = FileInputStream(metafile)
            val fileBytes = inputStream.readAllBytes()
            val firstIndex = String(fileBytes).indexOf("4:infod6:") + 6
            val subBytes = fileBytes.copyOfRange(firstIndex, fileBytes.size - 1)
            val hasher = MessageDigest.getInstance("SHA-1")
            return hasher.digest(subBytes).toTypedArray()
        }

    override fun toString(): String {
        return "{$announce\n$announceList\n$comment\n$createdBy\n$creationDate\n$info}"
    }

    val blockQuantity: Long
        get() = (info.pieceLength / BLOCK_SIZE).toLong() + ((info.pieceLength % BLOCK_SIZE) > 0).toInt()

    private fun Boolean.toInt() = if (this) 1 else 0

    class Metainfo(map: Map<String, Any>) {
        private var dictionary: Map<String, Any> = map

        val isSingleFile: Boolean = !dictionary.containsKey("files")

        val fileDatas: List<FileData>
            get() {
                if (isSingleFile) {
                    return listOf(
                        FileData(
                            dictionary["length"].toString().toLong(),
                            listOf(name.toByteArray(Charsets.US_ASCII).toString(Charsets.UTF_8)),
                            dictionary.getOrDefault("md5sum", "").toString()
                        )
                    )
                } else {
                    val list = mutableListOf<FileData>()
                    for (flex in dictionary["files"] as List<Map<String, Any>>) {
                        list.add(
                            FileData(
                                flex["length"].toString().toLong(),
                                (flex["path"] as List<String>).map {
                                    it.toByteArray(Charsets.US_ASCII).toString(Charsets.UTF_8)
                                },
                                flex.getOrDefault("md5sum", "").toString()
                            )
                        )
                    }
                    return list
                }
            }

        val fullLength: Long = fileDatas.map { it.length }.sum()

        val name: String
            get() = dictionary["name"].toString()

        val pieceLength: Int = dictionary["piece length"].toString().toInt()

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
                    list.add(PieceHash(currentParts.toByteArray()))
                }
                return list
            }
    }

}
