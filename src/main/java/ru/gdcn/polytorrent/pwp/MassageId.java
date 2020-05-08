package ru.gdcn.polytorrent.pwp;

public enum MassageId {
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

    MassageId(int id) {
        this.id = id;
    }
}
