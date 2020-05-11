package ru.gdcn.polytorrent.tracker;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.gdcn.polytorrent.AnnounceInfo;
import ru.gdcn.polytorrent.Metadata;
import ru.gdcn.polytorrent.TrackerManager;
import ru.gdcn.polytorrent.filesaver.FileSaver;

import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.Random;

public class Torrent {
    private static final int PORT = 6881;
    private static final Logger logger = LoggerFactory.getLogger(Torrent.class);

    public static void main(String[] args) {

        String filename = args[0];
        byte[] peerId = new byte[20];
        new Random().nextBytes(peerId);
        File savedirectory = new File(filename);
        Metadata metadata = new Metadata(savedirectory);

        TrackerManager manager = new TrackerManager(metadata, peerId);
        Optional<AnnounceInfo> trackerResponse = manager.getAnnounceInfo();
        AnnounceInfo announceInfo;
        if (trackerResponse.isPresent()){
            announceInfo = trackerResponse.get();
        } else {
            throw new IllegalStateException("Не удалось получить информацию от трекеров.");
        }
        System.out.println(announceInfo.getPeers().size());

        FileSaver fileSaver = FileSaver.getInstance(metadata, savedirectory);
        try {
            fileSaver.init();
        } catch (IOException e) {
            logger.info("Ошибка при инициализации загрузчика", e);
        }
        metadata.getInfo().getPieceHashes().get(0).getBytes();
    }
}
