package ru.gdcn.polytorrent.pwp;

import lombok.Builder;
import lombok.Data;

import java.net.InetAddress;
import java.util.Set;

@Data
@Builder
public class Peer {
    private InetAddress ip;
    private int port;
    private Byte[] peerId;
    private Set<Long> piecesId;
}
