package ru.gdcn.polytorrent.filesaver;

import java.util.ArrayList;
import java.util.List;

public class Piece {
    private final Byte[] sha1;
    private long length;
    private final List<FilePieceMapper> filePointers;

    public Piece(Byte[] sha1) {
        this.sha1 = sha1;
        this.filePointers = new ArrayList<>();
    };

    public long getLength() {
        return length;
    }

    public void setLength(long length) {
        this.length = length;
    }

    public List<FilePieceMapper> getFilePointers() {
        return filePointers;
    }

    public void addFilePointer(FilePieceMapper filePointer) {
        filePointers.add(filePointer);
    }

}
