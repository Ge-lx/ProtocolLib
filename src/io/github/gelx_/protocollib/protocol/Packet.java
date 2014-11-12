package io.github.gelx_.protocollib.protocol;

import java.net.SocketAddress;

public abstract class Packet{

    protected SocketAddress address;
    protected byte[] data;

    public Packet(SocketAddress address, byte[] data){
        this.address = address;
        this.data = data;
    }

    public Packet(SocketAddress address){
        this(address, null);
    }

    public SocketAddress getAddress(){
        return address;
    }
    public byte[] getData(){
        return data;
    }
    public abstract short getID();
}