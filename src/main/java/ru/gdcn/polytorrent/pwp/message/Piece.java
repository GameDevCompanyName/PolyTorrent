package ru.gdcn.polytorrent.pwp.message;

import lombok.Data;
import ru.gdcn.polytorrent.Utilities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Data
public class Piece implements Message{
    private static final int MIN_LENGTH = 9;
    private MessageId messageId;
    private int pieceId;
    private int offset;
    private Byte[] data;

    public Piece (int pieceId, int offset, byte[] data) {
        messageId = MessageId.PIECE;
        this.pieceId = pieceId;
        this.offset = offset;
        this.data = Utilities.byteArrayToObject(data);
    }

    @Override
    public byte[] getBytes() {
        List<Byte> pieceMsgBytes = new ArrayList<>();
        pieceMsgBytes.addAll(Arrays.asList(Utilities.getFourBytesFromInt(MIN_LENGTH + data.length)));
        pieceMsgBytes.add((byte) messageId.getId());
        pieceMsgBytes.addAll(Arrays.asList(Utilities.getFourBytesFromInt(pieceId)));
        pieceMsgBytes.addAll(Arrays.asList(Utilities.getFourBytesFromInt(offset)));
        pieceMsgBytes.addAll(Arrays.asList(data));
        return Utilities.byteListToPrimitive(pieceMsgBytes);
    }
}
