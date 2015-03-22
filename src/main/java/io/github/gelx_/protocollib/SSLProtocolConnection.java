package io.github.gelx_.protocollib;

import io.github.gelx_.protocollib.connection.ClientHandler;
import io.github.gelx_.protocollib.connection.SSLConnection;
import io.github.gelx_.protocollib.protocol.Protocol;

import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

@SuppressWarnings("unused")
public class SSLProtocolConnection extends ProtocolConnection {

    public SSLProtocolConnection(Protocol protocol, SocketAddress address, boolean server) throws IOException {
        super(protocol, address, server);
    }

    @Override
    protected void connect(Protocol protocol, SocketAddress address) throws IOException {

        if(isServer) {
            this.server = new SSLConnection(address, protocol, this);
        } else {
            InetAddress inetAddress = ((InetSocketAddress) address).getAddress();
            int port = ((InetSocketAddress) address).getPort();
            this.client = new ClientHandler(SSLSocketFactory.getDefault().createSocket(inetAddress, port), protocol, this);
        }
    }
}
