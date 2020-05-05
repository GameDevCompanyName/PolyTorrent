package ru.gamedevcompanyname.polytorrent.tracker;

import com.sun.org.slf4j.internal.Logger;
import com.sun.org.slf4j.internal.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class Tracker {
    private static final int PORT = 6881;
    private static final Logger logger = LoggerFactory.getLogger(Tracker.class);

    public static void main(String[] args) {
        String filename = args[0];

        MetaDataProvider provider = new MetaDataProvider();
        MetaData metaData;
        try (FileInputStream fis = new FileInputStream(filename)){
            metaData = provider.provide(fis);
        } catch (IOException e) {
            logger.error("Не удалось открыть torrent-файл", e);
        }

    }
}
