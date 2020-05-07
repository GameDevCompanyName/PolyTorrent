package ru.gdcn.polytorrent.filesaver;

import java.io.RandomAccessFile;

public class FilePieceMapper {

    private final long fileOffset;
    private final long pieceOffset;
    private final RandomAccessFile file;

    public FilePieceMapper(RandomAccessFile file, Long fileOffset, Long pieceOffset) {
        this.file = file;
        this.fileOffset = fileOffset;
        this.pieceOffset = pieceOffset;
    }

    public long getFileOffset() {
        return fileOffset;
    }

    public long getPieceOffset() {
        return pieceOffset;
    }

    public RandomAccessFile getFile() {
        return file;
    }
}
