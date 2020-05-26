package ru.gdcn.polytorrent.pwp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;
import java.util.Set;

public class TcpConnect {
    private static final Logger logger = LoggerFactory.getLogger(TcpConnect.class);
    private Socket socket;
    private Peer peer;

    public TcpConnect(Peer peer) {
        this.peer = peer;
    }

    public void run() throws IOException {
        try {
            socket = new Socket(peer.getIp(), peer.getPort());
            if (!socket.isClosed()) {
                logger.info("Tcp connected");
                new PeerSession(peer, socket);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
