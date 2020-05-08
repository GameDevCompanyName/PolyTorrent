package ru.gdcn.polytorrent.pwp;

import java.io.*;
import java.net.Socket;

public class TcpConnect {
    private Socket socket;
    private BufferedReader in;
    private BufferedWriter out;

    public void run() throws IOException {
        try {
            socket = new Socket();
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            String input = in.readLine();
            out.write("output");
            out.flush();
        } finally {
            socket.close();
        }
    }
}
