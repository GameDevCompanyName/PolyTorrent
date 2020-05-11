package ru.gdcn.polytorrent.tracker;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.gdcn.polytorrent.AnnounceInfo;
import ru.gdcn.polytorrent.Metafile;
import ru.gdcn.polytorrent.TrackerManager;
import ru.gdcn.polytorrent.Utilities;
import ru.gdcn.polytorrent.filesaver.FileSaver;

import java.io.File;
import java.io.IOException;
import java.util.Random;

import static ru.gdcn.polytorrent.Utilities.isBitSet;

public class Torrent {
    private static final int PORT = 6881;
    private static final Logger logger = LoggerFactory.getLogger(Torrent.class);

    public static void main(String[] args) {

        String filename = args[0];
        byte[] peerId = new byte[20];
        new Random().nextBytes(peerId);
        File savedirectory = new File(filename);
        Metafile metafile = new Metafile(savedirectory);

        TrackerManager manager = new TrackerManager(metafile, peerId);
        AnnounceInfo announceInfo = manager.getAnnounceInfo();
        System.out.println(announceInfo.getPeers().size());

        FileSaver fileSaver = FileSaver.getInstance(metafile, savedirectory);
        try {
            fileSaver.init();
        } catch (IOException e) {
            logger.info("Ошибка при инициализации загрузчика", e);
        }
        metafile.getInfo().getPieceHashes().get(0).getBytes();
    }
}
