package ru.gdcn.polytorrent.pwp;

import lombok.Data;
import lombok.NoArgsConstructor;
import ru.gdcn.polytorrent.Utilities;
import ru.gdcn.polytorrent.pwp.message.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Data
@NoArgsConstructor
public class PackageReader {
    private static final int HEADER_LEN = 5;
    private List<Message> message;

    public PackageReader read(byte[] bytes) {
        message = new ArrayList<>();
        int pos = 0;
        while (pos < bytes.length) {
            int len = bytes[pos + 3];
            int end = pos + len + 4;
            switch (MessageId.getMessageId(bytes[pos + 4])) {
                case CHOKE:
                    message.add(new StateMessage().choke());
                    break;
                case UNCHOKE:
                    message.add(new StateMessage().unChoke());
                    break;
                case INTERESTED:
                    message.add(new StateMessage().interested());
                    break;
                case NOT_INTERESTED:
                    message.add(new StateMessage().notInterested());
                    break;
                case HAVE:
                    readHaveMsg(bytes, pos, end);
                    break;
                case BITFIELD:
                    readBitfieldMsg(bytes, pos, end);
                    break;
                case REQUEST:
                    readRequestMsg(bytes, pos, end);
                    break;
                case PIECE:
                    readPieceMsg(bytes, pos, end);
                    break;
            }
            pos += len + 4;
        }
        return this;
    }

    public boolean isHandshake() {
        return true;
    }

    private void readHaveMsg(byte[] bytes, int pos, int end) {
        message.add(new Have(Utilities.getIntFromFourBytes(Arrays.copyOfRange(bytes, pos + HEADER_LEN, end))));
    }

    private void readBitfieldMsg(byte[] bytes, int pos, int end) {
        message.add(new Bitfield(Arrays.copyOfRange(bytes, pos + HEADER_LEN, end)));
    }

    private void readRequestMsg(byte[] bytes, int pos, int end) {
        pos += HEADER_LEN;
        int pieceId = Utilities.getIntFromFourBytes(Arrays.copyOfRange(bytes, pos, pos + 4));
        pos += 4;
        int offset = Utilities.getIntFromFourBytes(Arrays.copyOfRange(bytes, pos, pos + 4));
        pos += 4;
        int pieceLen = Utilities.getIntFromFourBytes(Arrays.copyOfRange(bytes, pos, end));
        message.add(new Request(pieceId, offset, pieceLen));
    }

    private void readPieceMsg(byte[] bytes, int pos, int end) {
        pos += HEADER_LEN;
        int pieceId = Utilities.getIntFromFourBytes(Arrays.copyOfRange(bytes, pos, pos + 4));
        pos += 4;
        int offset = Utilities.getIntFromFourBytes(Arrays.copyOfRange(bytes, pos, pos + 4));
        pos += 4;
        message.add(new Piece(pieceId, offset, Arrays.copyOfRange(bytes, pos, end)));
    }
}
