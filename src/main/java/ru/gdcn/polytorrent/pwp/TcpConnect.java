package ru.gdcn.polytorrent.pwp;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.*;
import java.net.Socket;
import java.util.Set;
import java.util.concurrent.Semaphore;

public class TcpConnect implements Closeable {
    private static final Logger logger = LogManager.getLogger(TcpConnect.class);
    private Socket socket;
    private Peer peer;
    private Semaphore semaphore;

    public TcpConnect(Peer peer, Semaphore semaphore) {
        this.peer = peer;
        this.semaphore = semaphore;
    }

    public void run() throws IOException {
        try {
            socket = new Socket(peer.getIp(), peer.getPort()); //TODO ConnectionTimeout
            logger.info("Tcp connected");
            new PeerSession(peer, socket, semaphore);
        } catch (IOException e) {
//            e.printStackTrace();
            logger.error("Ошибка открытия сокета");
            semaphore.release();
        }
    }

    @Override
    public void close() throws IOException {
        semaphore.release();
        socket.close();
    }
}
