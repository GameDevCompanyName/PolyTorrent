package ru.gdcn.polytorrent.pwp.message;

public enum MessageId {
    CHOKE(0),
    UNCHOKE(1),
    INTERESTED(2),
    NOT_INTERESTED(3),
    HAVE(4),
    BITFIELD(5),
    REQUEST(6),
    PIECE(7),
    CANCEL(8),
    PORT(9);

    private int id;

    MessageId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static MessageId getMessageId(int id) {
        for(MessageId m: MessageId.values()) {
            if(m.getId() == id) return m;
        }
        return null;
    }
}
