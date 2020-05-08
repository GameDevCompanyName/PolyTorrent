package ru.gdcn.polytorrent.filesaver;

import ru.gdcn.polytorrent.tracker.FileData;
import ru.gdcn.polytorrent.tracker.MetaData;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class FileSaver {

    MetaData metaData;
    private final List<Piece> pieces = new ArrayList<>();
    List<RandomAccessFile> files = new LinkedList<>();
    File saveDirectory;

    private static FileSaver fileSaver;

    public FileSaver(MetaData metaData, File saveDirectory) {
        this.metaData = metaData;
        this.saveDirectory = saveDirectory;
    }

    private FileSaver() {}


    public static FileSaver getInstance() {
        if (fileSaver == null) {
            fileSaver = new FileSaver();
        }
        return fileSaver;
    }

    public synchronized boolean init() throws IOException {
        boolean resume = false;

        createPiecesList();

        if(!saveDirectory.mkdirs()) {
            throw new RuntimeException("Не удалось создать головную директорию");
        }
        // создаем дерево файлов и директорий
        if (metaData.isSingleFile()) {
            File persistentFile = new File(saveDirectory, metaData.getName());
            if (persistentFile.exists()) {
                resume = true;
            }
            RandomAccessFile raf = new RandomAccessFile(persistentFile, "rw");
            raf.setLength(metaData.getLength());
            files.add(raf);
        } else {
            if (!saveDirectory.getName().equals(metaData.getName())) {
                saveDirectory = new File(saveDirectory, metaData.getName());
                if (!saveDirectory.mkdir()) {
                    throw new RuntimeException("Не удалось создать директорию");
                }
            }

            for (FileData fileData : metaData.getFiles()) {
                List<String> path = fileData.getPath();
                StringBuilder pathName = new StringBuilder();

                for (int i = 0; i < path.size(); i++) {
                    String pathPart = path.get(i);
                    pathName.append("/").append(pathPart);
                    if (i == path.size() - 1) {
                        if (!new File(saveDirectory, pathName.toString()).mkdir()) {
                            throw new RuntimeException("Не удалось создать директорию");
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

        for (byte[] sha1 : metaData.getPieces()) {
            Piece piece = new Piece(sha1);
            if (pieceNumber < metaData.getPieces().size() - 1 && (metaData.getLength() % metaData.getPieceLength()) > 0) {
                piece.setLength(metaData.getPieceLength());
            } else {
                piece.setLength(new Long(metaData.getLength() % metaData.getPieceLength()).intValue());
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
