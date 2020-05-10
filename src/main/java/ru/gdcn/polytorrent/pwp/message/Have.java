package ru.gdcn.polytorrent.pwp.message;

import lombok.Data;
import ru.gdcn.polytorrent.Utilities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Data
public class Have implements Message{
    private static final int LENGTH = 5;
    private MessageId messageId;
    private int pieceId;

    public Have(int pieceId) {
        messageId = MessageId.HAVE;
        this.pieceId = pieceId;
    }

    @Override
    public byte[] getBytes() {
        List<Byte> haveMsgBytes = new ArrayList<>();
        haveMsgBytes.addAll(Arrays.asList(Utilities.getFourBytesFromInt(LENGTH)));
        haveMsgBytes.add((byte) messageId.getId());
        haveMsgBytes.addAll(Arrays.asList(Utilities.getFourBytesFromInt(pieceId)));
        return Utilities.byteListToPrimitive(haveMsgBytes);
    }
}
