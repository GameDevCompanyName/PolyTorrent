package ru.gdcn.polytorrent.torrent;

import java.util.List;

public class FileData {

    private final long length;

    private final List<String> path;

    private final String md5sum;

    public FileData(long length, List<String> path, String md5sum) {
        this.length = length;
        this.path = path;
        this.md5sum = md5sum;
    }

    public long getLength() {
        return length;
    }

    public List<String> getPath() {
        return path;
    }

    public String getMd5sum() {
        return md5sum;
    }
}
