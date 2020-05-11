package ru.gdcn.polytorrent.pwp;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.gdcn.polytorrent.Utilities;
import ru.gdcn.polytorrent.pwp.message.*;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static ru.gdcn.polytorrent.Utilities.getIntFromFourBytes;

@Data
@NoArgsConstructor
public class PackageReader {
    private static final Logger logger = LoggerFactory.getLogger(PackageReader.class);
    private static final int HEADER_LEN = 5;
    private List<Message> messages;
    private Message message;

    public PackageReader read(byte[] bytes) {
        switch (MessageId.getMessageId(bytes[4])) {
            case CHOKE:
                message = new StateMessage().choke();
                break;
            case UNCHOKE:
                message = new StateMessage().unChoke();
                break;
            case INTERESTED:
                message = new StateMessage().interested();
                break;
            case NOT_INTERESTED:
                message = new StateMessage().notInterested();
                break;
            case HAVE:
                readHaveMsg(bytes, 0, bytes.length);
                break;
            case BITFIELD:
                readBitfieldMsg(bytes, 0, bytes.length);
                break;
            case REQUEST:
                readRequestMsg(bytes, 0, bytes.length);
                break;
            case PIECE:
                readPieceMsg(bytes, 0, bytes.length);
                break;
            default:
                logger.info("Read unknown message");
                break;
        }
        return this;
    }

    public PackageReader readAll(byte[] bytes) {
        messages = new ArrayList<>();
        int pos = 0;
        while (pos < bytes.length) {
            int len = getIntFromFourBytes(Arrays.copyOfRange(bytes, pos, pos + 4));
            int end = pos + len + 4;
            switch (MessageId.getMessageId(bytes[pos + 4])) {
                case CHOKE:
                    messages.add(new StateMessage().choke());
                    break;
                case UNCHOKE:
                    messages.add(new StateMessage().unChoke());
                    break;
                case INTERESTED:
                    messages.add(new StateMessage().interested());
                    break;
                case NOT_INTERESTED:
                    messages.add(new StateMessage().notInterested());
                    break;
                case HAVE:
                    messages.add(readHaveMsg(bytes, pos + HEADER_LEN, end));
                    break;
                case BITFIELD:
                    messages.add(readBitfieldMsg(bytes, pos + HEADER_LEN, end));
                    break;
                case REQUEST:
                    messages.add(readRequestMsg(bytes, pos + HEADER_LEN, end));
                    break;
                case PIECE:
                    messages.add(readPieceMsg(bytes, pos + HEADER_LEN, end));
                    break;
            }
            pos += len + 4;
        }
        return this;
    }

    private Have readHaveMsg(byte[] bytes, int pos, int end) {
        return new Have(getIntFromFourBytes(Arrays.copyOfRange(bytes, pos, end)));
    }

    private Bitfield readBitfieldMsg(byte[] bytes, int pos, int end) {
        return new Bitfield(Arrays.copyOfRange(bytes, pos, end));
    }

    private Request readRequestMsg(byte[] bytes, int pos, int end) {
        int pieceId = getIntFromFourBytes(Arrays.copyOfRange(bytes, pos, pos + 4));
        pos += 4;
        int offset = getIntFromFourBytes(Arrays.copyOfRange(bytes, pos, pos + 4));
        pos += 4;
        int pieceLen = getIntFromFourBytes(Arrays.copyOfRange(bytes, pos, end));
        return new Request(pieceId, offset, pieceLen);
    }

    private Piece readPieceMsg(byte[] bytes, int pos, int end) {
        int pieceId = getIntFromFourBytes(Arrays.copyOfRange(bytes, pos, pos + 4));
        pos += 4;
        int offset = getIntFromFourBytes(Arrays.copyOfRange(bytes, pos, pos + 4));
        pos += 4;
        return new Piece(pieceId, offset, Arrays.copyOfRange(bytes, pos, end));
    }
}
