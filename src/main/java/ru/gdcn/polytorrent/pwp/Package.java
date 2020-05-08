package ru.gdcn.polytorrent.pwp;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Data
@NoArgsConstructor
public class Package {
    private static final Byte NAME_LEN = 19;
    private static final Byte[] NAME = {66, 105, 116, 84, 111, 114, 114, 101, 110, 116, 32, 112, 114, 111, 116, 111, 99, 111, 108}; //BitTorrent protocol
    private int length;
    private MassageId massageId;
    private int pieceId;
    private int beginOfPiece;
    private int pieceLen;
    private Byte[] data;

    public List<Byte> buildHandshake(byte[] infoHash, byte[] peerID) {
        List<Byte> handShakeBytesList = new ArrayList<>();
        handShakeBytesList.add(NAME_LEN);
        return handShakeBytesList;
    }
}
