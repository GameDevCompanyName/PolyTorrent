package ru.gdcn.polytorrent.pwp;

import lombok.Data;

import java.util.Set;

@Data
public class SessionInfo {
    private Set<Peer> peers;
    private Byte[] infoHash;
    private Set<Long> receivedPieces;
    private Set<Long> requestedPieces;
}
