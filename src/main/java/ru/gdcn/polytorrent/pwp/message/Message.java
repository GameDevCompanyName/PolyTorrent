package ru.gdcn.polytorrent.pwp.message;

public interface Message {
    public byte[] getBytes();

    public MessageId getMessageId();
}
