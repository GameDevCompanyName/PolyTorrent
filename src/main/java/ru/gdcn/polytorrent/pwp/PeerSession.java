package ru.gdcn.polytorrent.pwp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.gdcn.polytorrent.filesaver.FileSaver;
import ru.gdcn.polytorrent.pwp.message.Handshake;
import ru.gdcn.polytorrent.pwp.message.Have;
import ru.gdcn.polytorrent.pwp.message.Message;
import ru.gdcn.polytorrent.pwp.message.MessageId;

import java.io.*;
import java.net.Socket;
import java.util.Arrays;

public class PeerSession {
    private static final Logger logger = LoggerFactory.getLogger(FileSaver.class);
    private final Peer peer;
    private final Socket socket;
    private final InputStream in;
    private final OutputStream out;

    public PeerSession(Peer peer, Socket socket) throws IOException {
        this.peer = peer;
        this.socket = socket;
        in = socket.getInputStream();
        out = socket.getOutputStream();
        Runnable task = this::mainCycle;
        Thread thread = new Thread(task);
        thread.start();
    }

    private void mainCycle() {
        if (!handshake()) {
            logger.error("Некорректное handshake сообщение от пира");
            return;
        }
        PackageReader packageReader = new PackageReader().read(getMsg());
        getPieceInfo(packageReader);
    }

    private boolean handshake() {
        Handshake handshake = new Handshake(SessionInfo.infoHash, SessionInfo.ourPeerId);
        sendMsg(handshake.getBytes());
        return Handshake.isHandshakeResponse(Arrays.copyOfRange(getMsg(), 0, 68), SessionInfo.infoHash, peer.getPeerId());
    }

    private void getPieceInfo(PackageReader packageReader) {
        for (Message message : packageReader.getMessage()) {
            if (message.getMessageId().equals(MessageId.HAVE)) {
                Have have = (Have) message;
                peer.addPieceId(have);
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
