package ru.gdcn.polytorrent.pwp.message;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class StateMessage implements Message{
    private static final int LENGTH = 1;
    private MessageId messageId;

    public StateMessage choke() {
        messageId = MessageId.CHOKE;
        return this;
    }

    public StateMessage unChoke() {
        messageId = MessageId.UNCHOKE;
        return this;
    }

    public StateMessage interested() {
        messageId = MessageId.INTERESTED;
        return this;
    }

    public StateMessage notInterested() {
        messageId = MessageId.NOT_INTERESTED;
        return this;
    }

    @Override
    public byte[] getBytes() {
        byte[] stateMessageBytes = {0, 0, 0, LENGTH, (byte) messageId.getId()};
        return stateMessageBytes;
    }
}
