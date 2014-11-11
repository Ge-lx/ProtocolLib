package io.github.gelx_.protocollib;

import com.sun.media.jfxmedia.logging.Logger;
import io.github.gelx_.protocollib.connection.Connection;
import io.github.gelx_.protocollib.protocol.Packet;
import io.github.gelx_.protocollib.protocol.PacketInfos;
import io.github.gelx_.protocollib.protocol.Protocol;

import java.net.SocketAddress;
import java.util.HashMap;
import java.util.concurrent.TimeoutException;

/**
 * Created by Gelx on 11.11.2014.
 */
public class ProtocolConnection {

    private Protocol protocol;
    private Connection connection;

    private HashMap<Class<? extends Packet>, PacketResponseListener> listeners = new HashMap<>();

    public ProtocolConnection(Protocol protocol, SocketAddress bindAddress){
        this.protocol = protocol;
        this.connection = new Connection(bindAddress, protocol);

        protocol.getPacketInfos();
        for(PacketInfos infos : protocol.getPacketInfos()){
            if(infos.hasResponse()){
                listeners.put(infos.getType(), new PacketResponseListener(infos.getType(), connection, protocol));
            }
        }
    }

    public void sendPacket(Packet p){
        connection.getConnection(p.getAddress()).queuePacketForWrite(p);
    }


    public Packet queryPacket(Packet p){
        try {
            return listeners.get(p.getClass()).sendAndGetResponse(p);
        } catch (TimeoutException e) {
            Logger.logMsg(Logger.WARNING, e.getMessage());
            return null;
        }
    }
}
