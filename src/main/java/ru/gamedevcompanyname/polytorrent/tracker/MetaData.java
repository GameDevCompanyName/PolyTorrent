package ru.gamedevcompanyname.polytorrent.tracker;

import java.util.List;

public class MetaData {
    private final List<FileData> files;
    private final List<byte[]> pieces;
    private final long pieceLength;

    public MetaData(List<FileData> files, List<byte[]> pieces, long pieceLength) {
        this.files = files;
        this.pieces = pieces;
        this.pieceLength = pieceLength;
    }

    public boolean isSingleFile() {
        return false;
    }

    public String getName() {
        return "";
    }

    public int getLength() {
        return 0;
    }

    public List<FileData> getFiles() {
        return files;
    }

    public List<byte[]> getPieces() {
        return pieces;
    }

    public long getPieceLength() {
        return pieceLength;
    }
}
