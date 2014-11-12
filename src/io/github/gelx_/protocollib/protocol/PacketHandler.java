package io.github.gelx_.protocollib.protocol;

import com.sun.media.jfxmedia.logging.Logger;
import io.github.gelx_.protocollib.ProtocolConnection;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public abstract class PacketHandler {

    private HashMap<Short, Method> handlers = new HashMap<>();
    private HashMap<Class<? extends Packet>, List<PacketListener>> listeners = new HashMap<>();

    private BlockingQueue<Packet> packetQueue = new LinkedBlockingQueue<>(50);

    private Thread handler;

    protected ProtocolConnection connection;

    public PacketHandler(Protocol protocol, ProtocolConnection connection) {
        this.connection = connection;
        List<PacketInfos> infos = protocol.getPacketInfos();
        for (PacketInfos packetInfo : infos) {
            if (packetInfo.isReceivable()) {
                try {
                    String packetName = packetInfo.getType().getSimpleName();
                    Method m = this.getClass().getMethod("handle" + packetName, Packet.class);
                    this.handlers.put(packetInfo.getId(), m);
                    this.listeners.put(packetInfo.getType(), new ArrayList<PacketListener>());
                } catch (NoSuchMethodException e) {
                    throw new RuntimeException("No handling for packet " + packetInfo.getType().getSimpleName() + " implemented!", e);
                }
            }
        }

        handler = new Thread(new Runnable() {
            public void run() {
                runHandler();
            }
        });
        handler.start();
    }

    public void runHandler(){
        while(!Thread.interrupted()){
            try {
                handlePacket(packetQueue.take());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        Logger.logMsg(Logger.INFO, "Handler interrupted");
    }

    public void handlePacket(Packet packet) {
        if (!handlers.containsKey(packet.getID())) {
            throw new IllegalArgumentException("No handler for packet " + packet.getID());
        }
        if(!listeners.get(packet.getClass()).isEmpty()){
            Iterator<PacketListener> it = listeners.get(packet.getClass()).iterator();
            while(it.hasNext())
                it.next().handlePacket(packet);
        }
        try {
            handlers.get(packet.getID()).invoke(this, packet);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Could not invoke handler for packet " + packet.getID(), e);
        }
    }

    public void queueForHandle(Packet packet){
        if(!this.packetQueue.offer(packet)){
            Logger.logMsg(Logger.ERROR, "Could not handler packet " + packet.getID());
        }
    }

    public void registerListener(Class<? extends Packet> packet, PacketListener listener){
        if(!listeners.containsKey(packet))
            throw new IllegalArgumentException("That packet cannot be listened for!");

        listeners.get(packet).add(listener);
    }

    public void unregisterListener(Class<? extends Packet> packet, PacketListener listener){
        if(!listeners.containsKey(packet))
            throw new IllegalArgumentException("That packet cannot be listened for!");

        listeners.get(packet).remove(listener);
    }

    public void stop(){
        this.handler.interrupt();
    }

}
