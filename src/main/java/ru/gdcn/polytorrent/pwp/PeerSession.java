package ru.gdcn.polytorrent.pwp;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import ru.gdcn.polytorrent.Utilities;
import ru.gdcn.polytorrent.pwp.message.*;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Semaphore;

public class PeerSession {
    private static final Logger logger = LogManager.getLogger(PeerSession.class);
    private final Peer peer;
    private final Socket socket;
    private final DataInputStream in;
    private final DataOutputStream out;
    private final Semaphore semaphore;

    public PeerSession(Peer peer, Socket socket, Semaphore semaphore) throws IOException {
        this.peer = peer;
        this.socket = socket;
        this.semaphore = semaphore;

        in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
        out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
        Runnable task = this::mainCycle;
        Thread thread = new Thread(task);
        try {
            semaphore.acquire();
            thread.start();
        } catch (InterruptedException e) {
            e.printStackTrace();
            semaphore.release();
        }
    }

    private void mainCycle() {
        logger.info("In main cycle");
        if (!handshake()) {
            logger.error("Wrong handshake answer");
            closeSocket();
            return;
        }
        logger.info("Handshake done");
        PackageReader packageReader = new PackageReader().read(getMsg());

        getPieceInfo(packageReader);
        logger.info("Get bitfield");

        sendMsg(new StateMessage().interested().getBytes());
        logger.info("Send interested message");

        if (!getUnckoke(packageReader.read(getMsg()))) {
            logger.error("No unchoke answer");
            closeSocket();
            return;
        }
        logger.info("Get unchoke");

        int tempPieceId = choosePiece(-1);
        while (tempPieceId != -1) {
            int offset = 0;
            int receivedBlocks = 0;
            List<Piece> pieces = new ArrayList<>();
            while (receivedBlocks < SessionInfo.NUM_OF_BLOCKS) {
                int num = SessionInfo.REQUESTED_BLOCKS;
                if(receivedBlocks > SessionInfo.NUM_OF_BLOCKS - SessionInfo.REQUESTED_BLOCKS) {
                    num = SessionInfo.NUM_OF_BLOCKS - receivedBlocks;
                }
                for (int i = 0; i < num; i++) {
                    sendMsg(new Request(tempPieceId, offset, SessionInfo.PIECE_LEN).getBytes());
                    offset += SessionInfo.PIECE_LEN;
                }
                for (int i = 0; i < num; i++) {
                    Piece piece = (Piece) packageReader.read(getMsg()).getMessage();
                    pieces.add(piece);
//                    logger.warn("Get piece with id: " + piece.getPieceId() + " offset: " + piece.getOffset());
                }
                receivedBlocks += SessionInfo.REQUESTED_BLOCKS;
            }
            if (SessionInfo.fileSaver.savePiece(pieces)){
                System.out.println("Download progress: " + (SessionInfo.receivedPieces.size() * 1.0 / SessionInfo.totalPieces));
                tempPieceId = choosePiece(tempPieceId);
            } else {
                logger.error("Wrong hashsum!");
                tempPieceId = choosePiece(-1);
            }
        }
        closeSocket();
    }

    private synchronized int choosePiece(int prevId) {
        if (prevId != -1) {
            SessionInfo.requestedPieces.remove(prevId);
            SessionInfo.receivedPieces.add(prevId);
            peer.getPiecesId().remove(prevId);
        }
        Iterator<Integer> iterator = peer.getPiecesId().iterator();
        while (iterator.hasNext()) {
            int id = iterator.next();
            if (!SessionInfo.receivedPieces.contains(id) && !SessionInfo.requestedPieces.contains(id)) {
                SessionInfo.requestedPieces.add(id);
                return id;
            }
        }
        return -1;
    }

    private boolean handshake() {
        Handshake handshake = new Handshake(SessionInfo.infoHash, SessionInfo.ourPeerId);
        sendMsg(handshake.getBytes());
        return handshake.isHandshakeResponse(getHandshakeMsg(), SessionInfo.infoHash, peer.getPeerId());
    }

    private void getPieceInfo(PackageReader packageReader) {
        peer.addBitfield((Bitfield) packageReader.getMessage());
    }


    private byte[] getHandshakeMsg() {
        while (true) {
            try {
                byte[] handshake = new byte[68];
                in.readFully(handshake);
                return handshake;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    private boolean getUnckoke(PackageReader packageReader) {
        if (packageReader.getMessage().getMessageId().equals(MessageId.UNCHOKE)) return true;
        return false;
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
            int len = in.readInt();
            byte[] msg = new byte[len];
            in.readFully(msg);
            return msg;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void closeSocket() {
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            semaphore.release();
        }
    }

}
