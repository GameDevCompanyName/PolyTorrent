package ru.gdcn.polytorrent.pwp.message;

import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.gdcn.polytorrent.Utilities;
import ru.gdcn.polytorrent.pwp.PeerSession;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Data
public class Handshake implements Message{
    private final Logger logger = LoggerFactory.getLogger(Handshake.class);
    private final int PSTRLEN = 19;
    private final String PSTR = "BitTorrent protocol";
    private final byte[] RESERVED = {0, 0, 0, 0, 0, 0, 0, 0};
    private MessageId messageId;
    private Byte[] infoHash;
    private Byte[] ourPeerId;

    public Handshake(Byte[] infoHash, Byte[] ourPeerId) {
        this.messageId = MessageId.HANDSHAKE;
        this.infoHash = infoHash;
        this.ourPeerId = ourPeerId;
    }

    public boolean isHandshakeResponse(byte[] bytes, Byte[] infoHash, Byte[] peerId) {
        if (bytes[0] != (byte) PSTRLEN) {
            logger.error("Wrong pstrlen");
            return false;
        } else if(!Arrays.equals(Arrays.copyOfRange(bytes, 1, 20), PSTR.getBytes())) {
            logger.error("Wrong pstr:\n" +
                    "Our: " + Arrays.toString(Arrays.copyOfRange(bytes, 1, 20)) + "\n" +
                    "Get: " + Arrays.toString(PSTR.getBytes()));
            return false;
        } else if(!Arrays.equals(Arrays.copyOfRange(bytes, 28, 48), Utilities.byteArrayToPrimitive(infoHash))) {
            logger.error("Wrong infohash:\n" +
                    "Our: " + this.infoHash + "\n" +
                    "Get: " + infoHash);
            return false;
        }else if(!Arrays.equals(Arrays.copyOfRange(bytes, 48, 68), Utilities.byteArrayToPrimitive(peerId))) {
            logger.error("Wrong PeerId:\n" +
                    "Our: " + Arrays.toString(peerId) +"\n" +
                    "Get: " + Arrays.toString(Arrays.copyOfRange(bytes, 48, 68)));
            return false;
        }
        return true;
//        return bytes[0] == (byte) PSTRLEN &&
//                Arrays.equals(Arrays.copyOfRange(bytes, 1, 20), PSTR.getBytes()) &&
//                Arrays.equals(Arrays.copyOfRange(bytes, 28, 48), Utilities.byteArrayToPrimitive(infoHash)) &&
//                Arrays.equals(Arrays.copyOfRange(bytes, 48, 68), Utilities.byteArrayToPrimitive(peerId));
    }

    @Override
    public byte[] getBytes() {
        List<Byte> handshakeBytes = new ArrayList<>();
        handshakeBytes.add((byte) PSTRLEN);
        handshakeBytes.addAll(Arrays.asList(Utilities.byteArrayToObject(PSTR.getBytes())));
        handshakeBytes.addAll(Arrays.asList(Utilities.byteArrayToObject(RESERVED)));
        handshakeBytes.addAll(Arrays.asList(infoHash));
        handshakeBytes.addAll(Arrays.asList(ourPeerId));
        return Utilities.byteListToPrimitive(handshakeBytes);
    }
}
