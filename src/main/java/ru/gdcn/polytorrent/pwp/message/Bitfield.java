package ru.gdcn.polytorrent.pwp.message;

import lombok.Data;
import ru.gdcn.polytorrent.Utilities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Data
public class Bitfield implements Message{
    private static final int MIN_LENGTH = 1;
    private MessageId messageId;
    private Byte[] data;

    public Bitfield(byte[] data) {
        messageId = MessageId.BITFIELD;
        this.data = Utilities.byteArrayToObject(data);
    }

    @Override
    public byte[] getBytes() {
        List<Byte> bitfieldMsgBytes = new ArrayList<>();
        bitfieldMsgBytes.addAll(Arrays.asList(Utilities.getFourBytesFromInt(MIN_LENGTH + data.length)));
        bitfieldMsgBytes.add((byte) messageId.getId());
        bitfieldMsgBytes.addAll(Arrays.asList(data));
        return Utilities.byteListToPrimitive(bitfieldMsgBytes);
    }
}
