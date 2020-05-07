package ru.gamedevcompanyname.polytorrent.tracker;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
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
