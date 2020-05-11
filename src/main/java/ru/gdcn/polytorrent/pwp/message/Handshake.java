package ru.gdcn.polytorrent.pwp.message;

import lombok.Data;
import ru.gdcn.polytorrent.Utilities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Data
public class Handshake implements Message{
    private static final int PSTRLEN = 19;
    private static final String PSTR = "BitTorrent protocol";
    private static final byte[] RESERVED = {0, 0, 0, 0, 0, 0, 0, 0};
    private MessageId messageId;
    private Byte[] infoHash;
    private Byte[] ourPeerId;

    public Handshake(Byte[] infoHash, Byte[] ourPeerId) {
        this.messageId = MessageId.HANDSHAKE;
        this.infoHash = infoHash;
        this.ourPeerId = ourPeerId;
    }

    public static boolean isHandshakeResponse(byte[] bytes, Byte[] infoHash, Byte[] ourPeerId) {
        return bytes[0] == (byte) PSTRLEN &&
                Arrays.equals(Arrays.copyOfRange(bytes, 1, 20), PSTR.getBytes()) &&
                Arrays.equals(Arrays.copyOfRange(bytes, 28, 48), Utilities.byteArrayToPrimitive(infoHash)) &&
                Arrays.equals(Arrays.copyOfRange(bytes, 48, 68), Utilities.byteArrayToPrimitive(ourPeerId));
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
