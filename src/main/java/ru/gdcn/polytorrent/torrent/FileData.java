package ru.gdcn.polytorrent.torrent;

import java.util.List;

public class FileData {

    private final long length;

    private final List<String> path;

    public FileData(long length, List<String> path) {
        this.length = length;
        this.path = path;
    }

    public long getLength() {
        return length;
    }

    public List<String> getPath() {
        return path;
    }
}
