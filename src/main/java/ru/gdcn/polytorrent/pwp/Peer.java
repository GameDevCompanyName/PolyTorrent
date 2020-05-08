package ru.gdcn.polytorrent.pwp;

import lombok.Data;

import java.net.InetAddress;
import java.util.Set;

@Data
public class Peer {
    private InetAddress ip;
    private int port;
    private int peerId;
    private Set<Long> piecesId;
}
