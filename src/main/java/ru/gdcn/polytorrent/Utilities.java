package ru.gdcn.polytorrent;

import java.util.List;

public class Utilities {

    public static String byteArrayToURLString(byte in[]) {
        byte ch = 0x00;
        int i = 0;
        if (in == null || in.length <= 0) {
            return null;
        }
        String pseudo[] = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9",
                "A", "B", "C", "D", "E", "F"};
        StringBuffer out = new StringBuffer(in.length * 2);

        while (i < in.length) {
            // First check to see if we need ASCII or HEX
            if ((in[i] >= '0' && in[i] <= '9') || (in[i] >= 'a' && in[i] <= 'z') || (in[i] >= 'A' && in[i] <= 'Z') || in[i] == '-' || in[i] == '_' || in[i] == '.' || in[i] == '~') {
                out.append((char) in[i]);
                i++;
            } else {
                out.append('%');
                ch = (byte) (in[i] & 0xF0); // Strip off high nibble
                ch = (byte) (ch >>> 4); // shift the bits down
                ch = (byte) (ch & 0x0F); // must do this is high order bit is
                // on!
                out.append(pseudo[(int) ch]); // convert the nibble to a
                // String Character
                ch = (byte) (in[i] & 0x0F); // Strip off low nibble
                out.append(pseudo[(int) ch]); // convert the nibble to a
                // String Character
                i++;
            }
        }

        String rslt = new String(out);
        return rslt;
    }

    public static int getIntFromTwoBytes(byte byte1, byte byte2) {
        return (byteToUnsignedInt(byte1) << 8) +
                byteToUnsignedInt(byte2);
    }

    public static int getIntFromFourBytes(byte[] bytes) {
        return (byteToUnsignedInt(bytes[0]) << 24) +
                (byteToUnsignedInt(bytes[1]) << 16) +
                (byteToUnsignedInt(bytes[2]) << 8) +
                byteToUnsignedInt(bytes[3]);
    }

    public static Byte[] getTwoBytesFromInt(int a) {
        return new Byte[]{
                (byte) (a >>> 8),
                (byte) (a)
        };
    }

    public static Byte[] getFourBytesFromInt(int a) {
        return new Byte[]{
                (byte) (a >>> 24),
                (byte) (a >>> 16),
                (byte) (a >>> 8),
                (byte) (a)
        };
    }

    public static int byteToUnsignedInt(byte b) {
        if (b < 0) return b + 256;
        else return b;
    }

    public static byte[] byteListToPrimitive(List<Byte> byteList) {
        byte[] byteArray = new byte[byteList.size()];
        for (int i = 0; i < byteArray.length; i++) {
            byteArray[i] = byteList.get(i);
        }
        return byteArray;
    }

    public static Byte[] byteArrayToObject(byte[] bytes) {
        Byte[] bytesObj = new Byte[bytes.length];
        int i = 0;
        for (byte b : bytes) bytesObj[i++] = b;
        return bytesObj;
    }

    public static byte[] byteArrayToPrimitive(Byte[] bytesObj) {
        byte[] bytes = new byte[bytesObj.length];
        int i = 0;
        for (Byte b : bytesObj) bytes[i++] = b;
        return bytes;
    }
}
