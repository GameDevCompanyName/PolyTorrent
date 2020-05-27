package ru.gdcn.polytorrent.torrent;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.gdcn.polytorrent.AnnounceInfo;
import ru.gdcn.polytorrent.Metadata;
import ru.gdcn.polytorrent.TrackerManager;
import ru.gdcn.polytorrent.filesaver.FileSaver;

import java.io.File;
import java.io.IOException;
import java.util.Random;

public class Torrent {
    private static final int PORT = 6881;
    private static final Logger logger = LogManager.getLogger(Torrent.class);

    public static void main(String[] args) {

        String filename = args[0];
        byte[] peerId = new byte[20];
        new Random().nextBytes(peerId);
        File savedirectory = new File(filename);
        Metadata metadata = new Metadata(savedirectory);

        //Его теперь нужно закрывать, так как он Closable
        //Правда пока не знаю где
        TrackerManager manager = new TrackerManager(metadata, peerId);
        AnnounceInfo announceInfo = manager.getAnnounceInfo();
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
