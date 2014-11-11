package io.github.gelx_.protocollib.protocol;

public class PacketInfos{
    private Class<? extends Packet> type;
    private short id;
    private boolean receivable;
    private Class<? extends Packet> response;

    public PacketInfos(Class<? extends Packet> type, short id, boolean receivable, Class<? extends Packet> response){
        this.type = type;
        this.id = id;
        this.receivable = receivable;
        this.response = response;
    }

    public Class<? extends Packet> getType(){
        return type;
    }

    public short getId(){
        return id;
    }

    public Class<? extends Packet> getResponsePacket(){
        return response;
    }

    public boolean hasResponse(){
        return response != null;
    }

    public boolean isReceivable(){
        return receivable;
    }
}