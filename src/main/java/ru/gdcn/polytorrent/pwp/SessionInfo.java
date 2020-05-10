package ru.gdcn.polytorrent.pwp;

import lombok.Data;

import java.util.Set;

@Data
public class SessionInfo {
    public static final int NUM_OF_PEERS = 5;
    public static Set<Peer> peers;
    public static Byte[] infoHash;
    public static Byte[] ourPeerId;
    public static Set<Long> receivedPieces;
    public static Set<Long> requestedPieces;
}
