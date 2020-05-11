package ru.gdcn.polytorrent.filesaver;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.EOFException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class Piece {
    private static final Logger logger = LoggerFactory.getLogger(Piece.class);

    private final byte[] sha1;

    @Getter
    @Setter
    private long length;

    private final List<FilePieceMapper> filePointers;
    private Set<PieceBlock> blocks;

    public Piece(Byte[] sha1) {
        this.sha1 = new byte[sha1.length];
        for (int i = 0; i < sha1.length; i++) {
            this.sha1[i] = sha1[i];
        }
        this.filePointers = new ArrayList<>();
        blocks = new TreeSet<>();
    }

    public List<FilePieceMapper> getFilePointers() {
        return filePointers;
    }

    public void addFilePointer(FilePieceMapper filePointer) {
        filePointers.add(filePointer);
    }

    public long getCompleted() {
        long completed = 0;

        for (PieceBlock block : blocks) {
            completed += block.length;
        }
        return completed;
    }

    public boolean isCompleted() {
        return getCompleted() == length;
    }

    public void write(int begin, byte[] block) throws IOException {

        long filePieceIndex = findFilePieceIndex(begin);
        FilePieceMapper filePiece = filePointers.get((int) filePieceIndex);

        long writtenBytes = 0;
        while (writtenBytes < block.length) {
            RandomAccessFile raf = filePiece.getFile();
            long seek = filePiece.getFileOffset() + ((begin + writtenBytes) - filePiece.getPieceOffset());
            raf.seek(seek);

            long byteToWrite = block.length - writtenBytes;
            long byteAvaiableInThisFile = raf.length() - seek;

            long byteAvaiableToWrite = Math.min(byteToWrite, byteAvaiableInThisFile);
            raf.write(block, (int) writtenBytes, (int) byteAvaiableToWrite);
            writtenBytes += byteAvaiableToWrite;

            if (byteAvaiableToWrite == byteAvaiableInThisFile && writtenBytes < block.length) {
                filePiece = filePointers.get((int) ++filePieceIndex);
            }
        }

        addPieceBlock(begin, block.length);

        if (isCompleted()) {
            if (!checkSha1()) {
                blocks.clear();
                throw new IOException("sha check failed");
            }
        }
    }

    public byte[] read(int begin, int length) throws IOException {

        if (!isAvaiable(begin, length)) {
            throw new EOFException("Data not available " + "begin: " + begin + " length: " + length);
        }
        int filePieceIndex = findFilePieceIndex(begin);
        FilePieceMapper filePiece = filePointers.get(filePieceIndex);
        byte[] block = new byte[length];

        int readBytes = 0;
        while (readBytes < length) {
            RandomAccessFile raf = filePiece.getFile();
            long seek = filePiece.getFileOffset() + ((begin + readBytes) - filePiece.getPieceOffset());
            raf.seek(seek);

            int byteToRead = length - readBytes;
            long byteAvaiableInThisFile = raf.length() - seek;

            Long byteAvaiableToRead = byteToRead < byteAvaiableInThisFile ? byteToRead : byteAvaiableInThisFile;
            raf.readFully(block, readBytes, byteAvaiableToRead.intValue());
            readBytes += byteAvaiableToRead.intValue();

            if (byteAvaiableToRead.equals(byteAvaiableInThisFile) && readBytes < length) {
                filePiece = filePointers.get(++filePieceIndex);
            }
        }

        return block;
    }

    public void addPieceBlock(int begin, int length) {

        PieceBlock newPieceBlock = new PieceBlock(begin, length);

        blocks.add(newPieceBlock);

/*        Iterator<PieceBlock> iterator = blocks.iterator();
        PieceBlock prev = iterator.next();

        Collection<PieceBlock> blocksToBeRemoved = new LinkedList<>();

        while (iterator.hasNext()) {
            PieceBlock p = iterator.next();
            if (prev.begin + prev.length >= p.begin) {

                p.length = Math.max(p.length + (p.begin - prev.begin), prev.length);
                p.begin = prev.begin;
                blocksToBeRemoved.add(prev);
            }
            prev = p;
        }

        for (PieceBlock pb : blocksToBeRemoved) {
            blocks.remove(pb);
        }*/
    }

    public boolean isAvaiable(int begin, int length) {
        for (PieceBlock block : blocks) {
            if (begin >= block.begin && length <= block.length) {
                return true;
            } else if (begin + length < block.begin) {
                return false;
            }
        }

        return false;
    }

    public boolean checkSha1() throws IOException {

        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("SHA1");
        } catch (NoSuchAlgorithmException ex) {
            ex.printStackTrace();
        }

        byte[] pieceBuffer = read(0, (int) length);
        byte[] sha1Digest = new byte[0];
        if (md != null) {
            sha1Digest = md.digest(pieceBuffer);
        } else {
            logger.error("Ошибка при проверке sha1-суммы");
        }
        return Arrays.equals(sha1, sha1Digest);
    }

    private int findFilePieceIndex(int begin) {
        int i;
        for (i = 0; i < filePointers.size() - 1; i++) {
            if (filePointers.get(i).getPieceOffset() <= begin && filePointers.get(i + 1).getPieceOffset() > begin) {
                return i;
            }
        }
        return i;
    }

    private class PieceBlock implements Comparable<PieceBlock> {

        public PieceBlock(long begin, long length) {
            this.begin = begin;
            this.length = length;
        }

        public long begin;
        public long length;

        @Override
        public int compareTo(@NotNull PieceBlock pieceBlock) {
            long beginDiff = this.begin - pieceBlock.begin;
            if (beginDiff != 0) {
                return (int) beginDiff;
            }
            return (int) (this.length - pieceBlock.length);
        }
    }
}
