package io.github.gelx_.protocollib.connection;

import io.github.gelx_.protocollib.ProtocolConnection;
import io.github.gelx_.protocollib.protocol.Protocol;

import javax.net.ssl.SSLServerSocketFactory;
import java.io.IOException;
import java.net.SocketAddress;

public class SSLConnection extends Connection {

    public SSLConnection(SocketAddress bindAddress, Protocol protocol, ProtocolConnection protocolConnection) {
        super(bindAddress, protocol, protocolConnection);

        this.protocol = protocol;
        this.protocolConnection = protocolConnection;

        try {
            serverSocket = SSLServerSocketFactory.getDefault().createServerSocket();
        } catch (IOException e) {
            LOG.severe("Could not create serverSocket! " + e.getMessage());
            throw new RuntimeException(e);
        }
        try {
            serverSocket.bind(bindAddress);
        } catch (IOException e) {
            LOG.severe("Could not bind serverSocket! " + e.getMessage());
            throw new RuntimeException(e);
        }

        serverThread = new Thread( new Runnable(){
            public void run(){
                runAcceptor();
            }
        } );
        serverThread.start();
    }
}
