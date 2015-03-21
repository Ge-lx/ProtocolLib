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

    protected Connection server;
    protected ClientHandler client;
    protected boolean isServer;

    private HashMap<Class<? extends Packet>, PacketResponseListener> listeners = new HashMap<>();

    public ProtocolConnection(Protocol protocol, SocketAddress address, boolean server) throws IOException {
        this.isServer = server;
        protocol.getPacketInfos();
        for(PacketInfos infos : protocol.getPacketInfos()) {
            if (infos.hasResponse()) {
                listeners.put(infos.getType(), new PacketResponseListener(infos.getType(), protocol));
            }
        }
        this.connect(protocol, address);
    }

    protected void connect(Protocol protocol, SocketAddress address) throws IOException {
        if(isServer) {
            this.server = new Connection(address, protocol, this);
        } else {
            InetAddress inetAddress = ((InetSocketAddress) address).getAddress();
            int port = ((InetSocketAddress) address).getPort();
            this.client = new ClientHandler(SocketFactory.getDefault().createSocket(inetAddress, port), protocol, this);
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
            return listeners.get(p.getClass()).sendAndGetResponse(p, isServer ? server.getConnection(p.getAddress()) : client);
        } catch (TimeoutException e) {
            Logger.logMsg(Logger.WARNING, e.getMessage());
            return null;
        }
    }

    public SocketAddress getRemoteAddress(){
        return isServer ? null : client.getRemoteAddress();
    }

    public SocketAddress[] getClientAddresses(){
        if(!isServer)
            return null;

        HashMap<SocketAddress, ClientHandler> clients = server.getConnections();
        return clients.keySet().toArray(new SocketAddress[clients.keySet().size()]);
    }
}
