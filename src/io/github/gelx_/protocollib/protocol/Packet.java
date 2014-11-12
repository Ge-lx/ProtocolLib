package io.github.gelx_.protocollib.protocol;

import java.net.SocketAddress;

@SuppressWarnings("unused")
public abstract class Packet{

    protected SocketAddress src, dst;
    protected byte[] data;

    public Packet(SocketAddress src, SocketAddress dst, byte[] data){
        this.src = src;
        this.dst = dst;
        this.data = data;
    }

    public Packet(SocketAddress src, SocketAddress dst){
        this(src, dst, null);
    }

    public SocketAddress getSourceAddress(){
        return src;
    }
    public SocketAddress getDestinationAddress() {
        return dst;
    }
    public byte[] getData(){
        return data;
    }
    public abstract short getID();
}