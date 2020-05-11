package ru.gdcn.polytorrent.pwp;

import lombok.Builder;
import lombok.Data;
import org.jetbrains.annotations.NotNull;
import ru.gdcn.polytorrent.pwp.message.Have;

import java.net.InetAddress;
import java.util.HashSet;
import java.util.Set;

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
        piecesId = new HashSet<>();
    }

    private InetAddress ip;
    private int port;
    private Byte[] peerId;
    private Set<Integer> piecesId;

    public void addPiece(Have have) {
        piecesId.add(have.getPieceId());
    }
}
