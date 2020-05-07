package ru.gdcn.polytorrent.tracker;

import java.util.List;

public class FileData {

    private final int length;

    private final List<String> path;

    public FileData(int length, List<String> path) {
        this.length = length;
        this.path = path;
    }

    public int getLength() {
        return length;
    }

    public List<String> getPath() {
        return path;
    }
}
