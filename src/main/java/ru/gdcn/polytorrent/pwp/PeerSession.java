package ru.gdcn.polytorrent.pwp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.gdcn.polytorrent.filesaver.FileSaver;
import ru.gdcn.polytorrent.pwp.message.*;

import java.io.*;
import java.net.Socket;
import java.util.Arrays;

public class PeerSession {
    private static final Logger logger = LoggerFactory.getLogger(PeerSession.class);
    private final Peer peer;
    private final Socket socket;
    private final DataInputStream in;
    private final DataOutputStream out;

    public PeerSession(Peer peer, Socket socket) throws IOException {
        this.peer = peer;
        this.socket = socket;
        in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
        out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
        Runnable task = this::mainCycle;
        Thread thread = new Thread(task);
        thread.start();
    }

    private void mainCycle() {
        System.out.println("in main cycle");
        if (!handshake()) {
            logger.error("Некорректное handshake сообщение от пира");
            return;
        }
        System.out.println("handshake done");
        PackageReader packageReader = new PackageReader().read(getMsg());
        getPieceInfo(packageReader);
        System.out.println("get bitfield");
        sendMsg(new StateMessage().interested().getBytes());
        System.out.println("Send interested message");
        packageReader.read(getMsg());

    }

    private boolean handshake() {
        Handshake handshake = new Handshake(SessionInfo.infoHash, SessionInfo.ourPeerId);
        sendMsg(handshake.getBytes());
        byte[] bytes = getMsg();
        return Handshake.isHandshakeResponse(Arrays.copyOfRange(bytes, 0, 68), SessionInfo.infoHash, peer.getPeerId());
    }

    private void getPieceInfo(PackageReader packageReader) {
        for (Message message : packageReader.getMessage()) {
            switch (message.getMessageId()) {
                case HAVE:
                    peer.addPiece((Have) message);
                    break;
                case BITFIELD:
                    break;
            }
        }
    }

    private void sendMsg(byte[] bytes) {
        try {
            out.write(bytes);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private byte[] getMsg() {
        try {
            return in.readAllBytes();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
