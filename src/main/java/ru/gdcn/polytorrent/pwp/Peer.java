package ru.gdcn.polytorrent.pwp;

import lombok.Builder;
import lombok.Data;
import org.jetbrains.annotations.NotNull;
import ru.gdcn.polytorrent.Utilities;
import ru.gdcn.polytorrent.pwp.message.Bitfield;
import ru.gdcn.polytorrent.pwp.message.Have;
import ru.gdcn.polytorrent.pwp.message.Have;

import java.net.InetAddress;
import java.util.HashSet;
import java.util.Set;

import static ru.gdcn.polytorrent.Utilities.isBitSet;

@Data
public class Peer {
    public Peer(
            @NotNull InetAddress ip,
            int port,
            @NotNull Byte[] peerId
    ) {
        this.ip = ip;
        this.port = port;
        this.peerId = peerId;
        this.piecesId = new HashSet<>();
    }

    private InetAddress ip;
    private int port;
    private Byte[] peerId;
    private Set<Long> piecesId;

    public void addBitfield(Bitfield bitfield) {
        Byte[] bits = bitfield.getData();
        for (int i = 0; i < bits.length; i++) {
            final Byte currentByte = bits[i];
            for (int j = 0; j < 8; j++) {
                if (isBitSet(currentByte, j)) {
                    piecesId.add(i * 8L + j);
                }
            }
        }
    }

    public void addPieceId(Have have) {
        piecesId.add((long) have.getPieceId());
    }
}
