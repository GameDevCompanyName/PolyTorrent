package ru.gdcn.polytorrent.pwp;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import ru.gdcn.polytorrent.Utilities;
import ru.gdcn.polytorrent.filesaver.FileSaver;
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
        try {
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
                List<Piece> pieces = new ArrayList<>();
                for (int i = 0; i < SessionInfo.NUM_OF_BLOCKS; i++) {
                    sendMsg(new Request(tempPieceId, offset, SessionInfo.PIECE_LEN).getBytes());
                    offset += SessionInfo.PIECE_LEN;
                    Piece piece = (Piece) packageReader.read(getMsg()).getMessage(); //TODO ClassCastException
                    pieces.add(piece);
//                    logger.info("Get piece with id: " + piece.getPieceId());
                }
                SessionInfo.fileSaver.savePiece(pieces);
                System.out.println("Download progress: " + (SessionInfo.receivedPieces.size() * 1.0 / SessionInfo.totalPieces));
                tempPieceId = choosePiece(tempPieceId);
            }
        } catch (Exception e) {
            logger.error("Исключение в главном цикле", e);
        } finally {
            closeSocket();
        }
        return;
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
            if (SessionInfo.receivedPieces.contains(id) || SessionInfo.requestedPieces.contains(id)) {
//                peer.getPiecesId().remove(id);
                continue;
            } else {
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
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        try {
            return in.readNBytes(68);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
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
            Thread.sleep(1500); //TODO Избавиться
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        try {
            byte[] lenBytes = new byte[4];
            in.read(lenBytes, 0, 4); //TODO SocketException
            int len = Utilities.getIntFromFourBytes(lenBytes);
//            logger.info("Message len: " + len);
            byte[] msg = new byte[len]; //TODO JavaHeap
            in.read(msg, 0, len);
//            logger.info("Len of get bytes: " + msg.length);
//            logger.info("BYTES: " + Arrays.toString(msg));
            return msg;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
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
