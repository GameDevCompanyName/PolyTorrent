package ru.gdcn.polytorrent.pwp;

import lombok.Data;
import ru.gdcn.polytorrent.filesaver.FileSaver;

import java.util.HashSet;
import java.util.Set;

@Data
public class SessionInfo {
    public static final int NUM_OF_PEERS = 5;
    public static final int PIECE_LEN = 16384;
    public static Integer totalPieces;
    public static Long NUM_OF_BLOCKS;
    public static Set<Peer> peers;
    public static Byte[] infoHash;
    public static Byte[] ourPeerId;
    public static Set<Integer> receivedPieces = new HashSet<>();
    public static Set<Integer> requestedPieces = new HashSet<>();
    public static FileSaver fileSaver;
}
