package ru.gdcn.polytorrent.pwp;

import java.io.*;
import java.net.Socket;
import java.util.Set;

public class TcpConnect {
    private Socket socket;
    private Peer peer;

    public TcpConnect(Peer peer) {
        this.peer = peer;
    }

    public void run() throws IOException {
        try {
            socket = new Socket(peer.getIp(), peer.getPort());
            new PeerSession(peer, socket);
        } finally {
            socket.close();
        }
    }
}
