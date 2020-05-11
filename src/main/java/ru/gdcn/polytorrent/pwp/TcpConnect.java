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
            if (!socket.isClosed()) {
                System.out.println("connected");
                new PeerSession(peer, socket);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
