package io.github.gelx_.protocollib;

import io.github.gelx_.protocollib.connection.ClientHandler;
import io.github.gelx_.protocollib.protocol.Packet;
import io.github.gelx_.protocollib.protocol.PacketListener;
import io.github.gelx_.protocollib.protocol.Protocol;

import java.util.concurrent.TimeoutException;

public class PacketResponseListener{

    private Protocol protocol;

    public PacketResponseListener(Class<? extends Packet> type, Protocol protocol){
        this.protocol = protocol;

        if(!protocol.getPacketInfos(type).hasResponse()){
            throw new IllegalArgumentException("Packet " + type.getSimpleName() + " has not response!");
        }
    }

    public Packet sendAndGetResponse(final Packet packet, ClientHandler client) throws TimeoutException {
        client.queuePacketForWrite(packet);

        final Packet[] response = new Packet[1];
        final Thread currentThread = Thread.currentThread();

        final Class<? extends Packet> responseType = protocol.getPacketInfos(packet.getClass()).getResponsePacket();

        PacketListener listener = new PacketListener() {
            public void handlePacket(Packet received) {
                response[0] = received;
                currentThread.interrupt();
            }
        };

        client.getHandler().registerListener(responseType, listener);

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            //nothing
        }
        client.getHandler().unregisterListener(responseType, listener);
        if(response[0] == null){
            throw new TimeoutException("Server did not respond in time!");
        }
        return response[0];
    }



}
