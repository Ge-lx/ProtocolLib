package io.github.gelx_.protocollib.protocol;

import io.github.gelx_.protocollib.ProtocolConnection;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public abstract class Protocol {

    private HashMap<Short, Class<? extends Packet>> packets;
    private HashMap<Class<? extends Packet>, PacketInfos> packetInfos = new HashMap<>();
    private Class<? extends PacketHandler> handlerClass;

    public Protocol(Class<? extends PacketHandler> handlerClass){
        this.packets = registerPackets();
        this.handlerClass = handlerClass;

        for(Class<? extends  Packet> type : packets.values()){
            PacketInfo annotation = type.getAnnotation(PacketInfo.class);
            packetInfos.put(type, new PacketInfos(type, annotation.id(), annotation.receivable(), annotation.response()));
        }
    }

    public abstract HashMap<Short, Class<? extends Packet>> registerPackets();

    public ByteBuffer packPacket(Packet packet){
        ByteBuffer buffer = ByteBuffer.allocate(6 + packet.getData().length);
        buffer.putShort(packet.getID());
        buffer.putInt(packet.getData().length);
        buffer.put(packet.getData());
        buffer.flip();
        return buffer;
    }

    public Packet unpackPacket(SocketAddress address, short id, byte[] data) {
        if (!packets.containsKey(id)) {
            throw new IllegalArgumentException("Received unknown packet with id: " + id);
        }
        Class<? extends Packet> packetClass = packets.get(id);

        Constructor<? extends Packet> constructor;
        try {
            constructor = packetClass.getConstructor(SocketAddress.class, byte[].class);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Packet " + packetClass.toString() + " does not implement a correct constructor!", e);
        }

        Packet packet;
        try {
             packet = constructor.newInstance(address, data);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new InternalError("Error while instantiating packet " + packetClass.toString() + ": " + e.getMessage());
        }

        return packet;
    }

    public List<PacketInfos> getPacketInfos(){
        return new ArrayList<>(packetInfos.values());
    }

    public PacketInfos getPacketInfos(Class<? extends Packet> packet){
        if(!packetInfos.containsKey(packet))
            throw new IllegalArgumentException("That packet is not registered!");
        return packetInfos.get(packet);
    }

    public PacketHandler getNewHandler(ProtocolConnection connection){
        try{
            return handlerClass.getConstructor(Protocol.class, ProtocolConnection.class).newInstance(this, connection);
        } catch (InvocationTargetException | IllegalAccessException | InstantiationException | NoSuchMethodException e) {
            throw new RuntimeException("Could not instantiate a PacketHandler!", e);
        }
    }

}
