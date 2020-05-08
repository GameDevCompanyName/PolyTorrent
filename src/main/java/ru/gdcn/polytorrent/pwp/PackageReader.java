package ru.gdcn.polytorrent.pwp;

import lombok.Data;
import ru.gdcn.polytorrent.Utilities;
import ru.gdcn.polytorrent.pwp.message.*;

import java.util.Arrays;

@Data
public class PackageReader {
    private static final int HEADER_LEN = 5;
    private Message message;

    public PackageReader(byte[] bytes) {
        switch (MessageId.getMessageId(bytes[4])) {
            case HAVE:
                readHaveMsg(bytes);
                break;
            case BITFIELD:
                readBitfieldMsg(bytes);
                break;
            case REQUEST:
                readRequestMsg(bytes);
                break;
            case PIECE:
                readPieceMsg(bytes);
                break;
        }
    }

    public boolean isHandshake() {
        return true;
    }

    private void readHaveMsg(byte[] bytes) {
        message = new Have(Utilities.getIntFromFourBytes(Arrays.copyOfRange(bytes, HEADER_LEN, bytes.length)));
    }

    private void readBitfieldMsg(byte[] bytes) {
        message = new Bitfield(Arrays.copyOfRange(bytes, HEADER_LEN, bytes.length));
    }

    private void readRequestMsg(byte[] bytes) {
        int pos = HEADER_LEN;
        int pieceId = Utilities.getIntFromFourBytes(Arrays.copyOfRange(bytes, pos, pos + 4));
        pos += 4;
        int offset = Utilities.getIntFromFourBytes(Arrays.copyOfRange(bytes, pos, pos + 4));
        pos += 4;
        int pieceLen = Utilities.getIntFromFourBytes(Arrays.copyOfRange(bytes, pos, pos + 4));
        message = new Request(pieceId, offset, pieceLen);
    }

    private void readPieceMsg(byte[] bytes) {
        int pos = HEADER_LEN;
        int pieceId = Utilities.getIntFromFourBytes(Arrays.copyOfRange(bytes, pos, pos + 4));
        pos += 4;
        int offset = Utilities.getIntFromFourBytes(Arrays.copyOfRange(bytes, pos, pos + 4));
        pos += 4;
        message = new Piece(pieceId, offset, Arrays.copyOfRange(bytes, pos, bytes.length));
    }
}
