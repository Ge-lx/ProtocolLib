package io.github.gelx_.protocollib;

import com.sun.media.jfxmedia.logging.Logger;
import io.github.gelx_.protocollib.connection.ClientHandler;
import io.github.gelx_.protocollib.connection.Connection;
import io.github.gelx_.protocollib.protocol.Packet;
import io.github.gelx_.protocollib.protocol.PacketInfos;
import io.github.gelx_.protocollib.protocol.Protocol;

import javax.net.SocketFactory;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.HashMap;
import java.util.concurrent.TimeoutException;

@SuppressWarnings("unused")
public class ProtocolConnection {

    private Connection server;
    private ClientHandler client;
    private boolean isServer;

    private HashMap<Class<? extends Packet>, PacketResponseListener> listeners = new HashMap<>();

    public ProtocolConnection(Protocol protocol, SocketAddress address, boolean server) throws IOException {
        this.isServer = server;
        if(isServer) {
            this.server = new Connection(address, protocol);
        } else {
            InetAddress inetAddress = ((InetSocketAddress) address).getAddress();
            int port = ((InetSocketAddress) address).getPort();
            this.client = new ClientHandler(SocketFactory.getDefault().createSocket(inetAddress, port), protocol);
        }

        protocol.getPacketInfos();
        for(PacketInfos infos : protocol.getPacketInfos()){
            if(infos.hasResponse()){
                listeners.put(infos.getType(), new PacketResponseListener(infos.getType(), this.server, protocol));
            }
        }
    }

    public void sendPacket(Packet p){
        if(isServer) {
            server.getConnection(p.getAddress()).queuePacketForWrite(p);
        } else {
            client.queuePacketForWrite(p);
        }
    }


    public Packet queryPacket(Packet p){
        try {
            return listeners.get(p.getClass()).sendAndGetResponse(p);
        } catch (TimeoutException e) {
            Logger.logMsg(Logger.WARNING, e.getMessage());
            return null;
        }
    }

    public SocketAddress getRemoteAddress(){
        return isServer ? null : client.getRemoteAddress();
    }

    public SocketAddress[] getClientAddresse(){
        if(!isServer)
            return null;

        HashMap<SocketAddress, ClientHandler> clients = server.getConnections();
        return clients.keySet().toArray(new SocketAddress[clients.keySet().size()]);
    }
}
