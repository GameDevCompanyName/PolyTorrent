package ru.gdcn.polytorrent.filesaver;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import ru.gdcn.polytorrent.Metadata;
import ru.gdcn.polytorrent.PieceHash;
import ru.gdcn.polytorrent.torrent.FileData;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class FileSaver {
    private static final Logger logger = LogManager.getLogger(FileSaver.class);

    Metadata.Metainfo metafile;
    private final List<FilePiece> pieces = new ArrayList<>();
    List<RandomAccessFile> files = new LinkedList<>();
    File saveDirectory;

    private static FileSaver fileSaver;

    private FileSaver(Metadata metafile, File saveDirectory) {
        this.metafile = metafile.getInfo();
        this.saveDirectory = saveDirectory;
    }

    public static FileSaver getInstance(Metadata metafile, File saveDirectory) {
        if (fileSaver == null) {
            fileSaver = new FileSaver(metafile, saveDirectory);
        }
        return fileSaver;
    }

    public synchronized boolean savePiece(List<ru.gdcn.polytorrent.pwp.message.Piece> blocks) {
        for (ru.gdcn.polytorrent.pwp.message.Piece block : blocks) {
            try {
                saveBlock(block.getPieceId(), block.getOffset(), block.getBytes());
            } catch (IOException e) {
                logger.error("Ошибка при записи piece №" + block.getPieceId(), e);
                return false;
            }
        }
        return true;
    }

    public synchronized void saveBlock(int index, int begin, byte[] block) throws IOException {
        FilePiece piece = pieces.get(index);
        if (!piece.isCompleted()) {
            piece.write(begin, block);
        }
    }

    public synchronized boolean init() throws IOException {
        boolean resume = false;

        createPiecesList();
        saveDirectory.mkdirs();
//        if (!saveDirectory.mkdirs()) {
//            throw new RuntimeException("Не удалось создать головную директорию");
//        }
        // создаем дерево файлов и директорий
        if (metafile.isSingleFile()) {
            File persistentFile = new File(saveDirectory, metafile.getName());
            if (persistentFile.exists()) {
                resume = true;
            }
            RandomAccessFile raf = new RandomAccessFile(persistentFile, "rw");
            raf.setLength(metafile.getFullLength());
            files.add(raf);
        } else {
            if (!saveDirectory.getName().equals(metafile.getName())) {
                saveDirectory = new File(saveDirectory, metafile.getName());
//                if (!saveDirectory.mkdir()) {
//                    logger.error("Не удалось создать директорию");
//                    throw new RuntimeException();
//                }
                saveDirectory.mkdir();
            }

            for (FileData fileData : metafile.getFileDatas()) {
                List<String> path = fileData.getPath();
                StringBuilder pathName = new StringBuilder();

                for (int i = 0; i < path.size(); i++) {
                    String pathPart = path.get(i);
                    pathName.append("/").append(pathPart);
                    if (i == path.size() - 2) {
                        new File(saveDirectory, pathName.toString()).mkdir();
                    }
//                    System.out.println(pathName);
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
        List<PieceHash> hashes = metafile.getPieceHashes();
        int size = metafile.getPieceHashes().size();
        long fraction = metafile.getFullLength() % metafile.getPieceLength();
        for (PieceHash hash : metafile.getPieceHashes()) {
            FilePiece piece = new FilePiece(hash.getBytes());
            if ((pieceNumber < size - 1 && fraction > 0)
                    || fraction == 0) {
                // если не последний
                piece.setLength(metafile.getPieceLength());
            } else {
                // если последний
                piece.setLength(metafile.getFullLength() % metafile.getPieceLength());
            }
            pieces.add(piece);
//            System.out.println(pieceNumber);
            pieceNumber++;
        }
//        System.out.println("закончилось");
    }

    private void mapPiecesWithFiles() throws IOException {
    /* Сопоставляем pieces и файлы (учитываем, что один пис может содержать
    данные из разных файлов*/
        Iterator<RandomAccessFile> fileIterator = files.iterator();
        Iterator<FilePiece> pieceIterator = pieces.iterator();

        long fileOffset = 0L;
        long pieceOffset = 0L;

        FilePiece piece = pieceIterator.next();
        RandomAccessFile file = fileIterator.next();

        while (piece != null && file != null) {

            piece.addFilePointer(new FilePieceMapper(file, fileOffset, pieceOffset));

            long pieceFreeBytes = piece.getLength() - pieceOffset;
            long fileMissingBytes = file.length() - fileOffset;

            // если Piece часть одного файла - обновляем оффсет файла, берем новый ru.gdcn.polytorrent.Piece
            if (pieceFreeBytes < fileMissingBytes) {
                fileOffset += pieceFreeBytes;
                if (pieceIterator.hasNext()) {
                    piece = pieceIterator.next();
                } else {
                    piece = null;
                }
                pieceOffset = 0L;
                // если Piece часть двух файлов - обновляем оффсет ru.gdcn.polytorrent.Piece, берем новый файл
            } else if (pieceFreeBytes > fileMissingBytes) {
                pieceOffset += fileMissingBytes;
                if (fileIterator.hasNext()) {
                    file = fileIterator.next();
                } else {
                    file = null;
                }
                fileOffset = 0L;
                logger.info("Найден piece который содержит куски разных файлов");
                // если Piece ровно ложится на конец файла, берем новый ru.gdcn.polytorrent.Piece и файл, обнуляем оффсеты
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
