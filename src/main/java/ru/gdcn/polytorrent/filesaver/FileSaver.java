package ru.gdcn.polytorrent.filesaver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.gdcn.polytorrent.Metafile;
import ru.gdcn.polytorrent.PieceHash;
import ru.gdcn.polytorrent.tracker.FileData;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class FileSaver {
    private static final Logger logger = LoggerFactory.getLogger(FileSaver.class);

    Metafile.Metainfo metafile;
    private final List<Piece> pieces = new ArrayList<>();
    List<RandomAccessFile> files = new LinkedList<>();
    File saveDirectory;

    private static FileSaver fileSaver;

    public FileSaver(Metafile metafile, File saveDirectory) {
        this.metafile = metafile.getInfo();
        this.saveDirectory = saveDirectory;
    }


    public synchronized void saveBlock(int index, int begin, byte[] block) {
        Piece piece = pieces.get(index);
        if (!piece.isCompleted()) {
            try {
                piece.write(begin, block);
            } catch (IOException e) {
                logger.error("Ошибка при записи piece: " + index, e);
            }
        }
    }

    public synchronized boolean init() throws IOException {
        boolean resume = false;

        createPiecesList();

        if(!saveDirectory.mkdirs()) {
            throw new RuntimeException("Не удалось создать головную директорию");
        }
        // создаем дерево файлов и директорий
        if (metafile.isSingleFile()) {
            File persistentFile = new File(saveDirectory, metafile.getName());
            if (persistentFile.exists()) {
                resume = true;
            }
            RandomAccessFile raf = new RandomAccessFile(persistentFile, "rw");
            raf.setLength(metafile.getLength());
            files.add(raf);
        } else {
            if (!saveDirectory.getName().equals(metafile.getName())) {
                saveDirectory = new File(saveDirectory, metafile.getName());
                if (!saveDirectory.mkdir()) {
                    logger.error("Не удалось создать директорию");
                    throw new RuntimeException();
                }
            }

            for (FileData fileData : metafile.getFileDatas()) {
                List<String> path = fileData.getPath();
                StringBuilder pathName = new StringBuilder();

                for (int i = 0; i < path.size(); i++) {
                    String pathPart = path.get(i);
                    pathName.append("/").append(pathPart);
                    if (i == path.size() - 1) {
                        if (!new File(saveDirectory, pathName.toString()).mkdir()) {
                            logger.error("Не удалось создать директорию");
                            throw new RuntimeException();
                        }
                    }
                }

                File persistentFile = new File(saveDirectory.getAbsolutePath() + pathName);
                if (persistentFile.exists()) {
                    resume = true;
                }
                RandomAccessFile raf = new RandomAccessFile(persistentFile, "rw");
                raf.setLength(fileData.getLength());
                files.add(raf);
            }
        }

        mapPiecesWithFiles();

        return resume;
    }

    private void createPiecesList() {
        // составляем список ru.gdcn.polytorrent.Piece'ов
        long pieceNumber = 0L;

        for (PieceHash hash : metafile.getPieceHashes()) {
            Byte[] sha1 = new Byte[hash.getBytes().size()];
            sha1 = hash.getBytes().toArray(sha1);
            Piece piece = new Piece(sha1);
            if (pieceNumber < metafile.getPieceHashes().size() - 1 && (metafile.getLength() % metafile.getPieceLength()) > 0) {
                piece.setLength(metafile.getPieceLength());
            } else {
                piece.setLength(metafile.getLength() % metafile.getPieceLength());
            }
            pieces.add(piece);

            pieceNumber++;
        }
    }

    private void mapPiecesWithFiles() throws IOException {
    /* Сопоставляем pieces и файлы (учитываем, что один пис может содержать
    данные из разных файлов*/
        Iterator<RandomAccessFile> fileIterator = files.iterator();
        Iterator<Piece> pieceIterator = pieces.iterator();

        long fileOffset = 0L;
        long pieceOffset = 0L;

        Piece piece = pieceIterator.next();
        RandomAccessFile file = fileIterator.next();

        while (piece != null && file != null) {

            piece.addFilePointer(new FilePieceMapper(file, fileOffset, pieceOffset));

            long pieceFreeBytes = piece.getLength() - pieceOffset;
            long fileMissingBytes = file.length() - fileOffset;

            // если ru.gdcn.polytorrent.Piece часть одного файла - обновляем оффсет файла, берем новый ru.gdcn.polytorrent.Piece
            if (pieceFreeBytes < fileMissingBytes) {
                fileOffset += pieceFreeBytes;
                if (pieceIterator.hasNext()) {
                    piece = pieceIterator.next();
                } else {
                    piece = null;
                }
                pieceOffset = 0L;
                // если ru.gdcn.polytorrent.Piece часть двух файлов - обновляем оффсет ru.gdcn.polytorrent.Piece, берем новый файл
            } else if (pieceFreeBytes > fileMissingBytes) {
                pieceOffset += fileMissingBytes;
                if (fileIterator.hasNext()) {
                    file = fileIterator.next();
                } else {
                    file = null;
                }
                fileOffset = 0L;
                logger.info("Найден piece который содержит куски разных файлов");
                // если ru.gdcn.polytorrent.Piece ровно ложится на конец файла, берем новый ru.gdcn.polytorrent.Piece и файл, обнуляем оффсеты
            } else {
                fileOffset = 0L;
                pieceOffset = 0L;
                if (fileIterator.hasNext()) {
                    file = fileIterator.next();
                } else {
                    file = null;
                }
                if (pieceIterator.hasNext()) {
                    piece = pieceIterator.next();
                } else {
                    piece = null;
                }
            }
        }
    }
}
