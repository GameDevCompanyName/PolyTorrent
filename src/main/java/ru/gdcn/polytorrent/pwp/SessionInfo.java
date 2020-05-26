package ru.gdcn.polytorrent.pwp;

import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Data
public class SessionInfo {
    public static final int NUM_OF_PEERS = 5;
    public static final int PIECE_LEN = 16384;
    public static final int NUM_OF_BLOCKS = 64;
    public static Set<Peer> peers;
    public static Byte[] infoHash;
    public static Byte[] ourPeerId;
    public static Set<Integer> receivedPieces = new HashSet<>();
    public static Set<Integer> requestedPieces = new HashSet<>();
}
