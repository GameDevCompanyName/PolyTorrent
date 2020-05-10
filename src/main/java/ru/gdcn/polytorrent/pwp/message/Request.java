package ru.gdcn.polytorrent.pwp.message;

import lombok.Data;
import ru.gdcn.polytorrent.Utilities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Data
public class Request implements Message{
    private static final int LENGTH = 13;
    private MessageId messageId;
    private int pieceId;
    private int offset;
    private int pieceLen;

    public Request(int pieceId, int offset, int pieceLen) {
        messageId = MessageId.REQUEST;
        this.pieceId = pieceId;
        this.offset = offset;
        this.pieceLen = pieceLen;
    }

    @Override
    public byte[] getBytes() {
        List<Byte> requestMsgBytes = new ArrayList<>();
        requestMsgBytes.addAll(Arrays.asList(Utilities.getFourBytesFromInt(LENGTH)));
        requestMsgBytes.add((byte) messageId.getId());
        requestMsgBytes.addAll(Arrays.asList(Utilities.getFourBytesFromInt(pieceId)));
        requestMsgBytes.addAll(Arrays.asList(Utilities.getFourBytesFromInt(offset)));
        requestMsgBytes.addAll(Arrays.asList(Utilities.getFourBytesFromInt(pieceLen)));
        return Utilities.byteListToPrimitive(requestMsgBytes);
    }
}
