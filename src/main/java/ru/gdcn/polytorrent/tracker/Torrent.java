package ru.gdcn.polytorrent.tracker;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.gdcn.polytorrent.Metafile;
import ru.gdcn.polytorrent.filesaver.FileSaver;

import java.io.File;
import java.io.IOException;

public class Torrent {
    private static final int PORT = 6881;
    private static final Logger logger = LoggerFactory.getLogger(Torrent.class);

    public static void main(String[] args) {
        String filename = args[0];

        Metafile metafile = new Metafile(new File(filename));
        FileSaver fileSaver = FileSaver.getInstance();
        try {
            fileSaver.init();
        } catch (IOException e) {
            logger.info("Ошибка при инициализации загрузчика", e);
        }
        metafile.getInfo().getPieceHashes().get(0).getBytes();
    }
}
