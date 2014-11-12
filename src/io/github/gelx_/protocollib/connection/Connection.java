package io.github.gelx_.protocollib.connection;

import io.github.gelx_.protocollib.ProtocolConnection;
import io.github.gelx_.protocollib.protocol.Protocol;

import javax.net.ServerSocketFactory;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.HashMap;
import java.util.logging.Logger;

@SuppressWarnings("unused")
public class Connection{

    private static final Logger LOG = Logger.getLogger("Connection");

    private ServerSocket serverSocket;
    private Thread serverThread;
    private HashMap<SocketAddress, ClientHandler> clientHandlers = new HashMap<>();

    private Protocol protocol;
    private ProtocolConnection protocolConnection;

    public Connection(SocketAddress bindAddress, Protocol protocol, ProtocolConnection protocolConnection){
        this.protocol = protocol;
        this.protocolConnection = protocolConnection;

        try {
            serverSocket = ServerSocketFactory.getDefault().createServerSocket();
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

    public void runAcceptor(){
        while(!Thread.interrupted()){
            Socket clientSocket;
            try {
                clientSocket = serverSocket.accept();
                LOG.info("Client connected!");
            } catch (IOException e) {
                LOG.severe("Error while waiting for client-connection! " + e.getMessage());
                break;
            }
            clientHandlers.put(clientSocket.getRemoteSocketAddress(), new ClientHandler(clientSocket, protocol, protocolConnection));
        }
        try {
            serverSocket.close();
        } catch (IOException e) {
            LOG.severe("Error while closing serverSocket! " + e.getMessage());
        }
    }

    public void close(){
        serverThread.interrupt();//Also closes socket
        for(ClientHandler client : clientHandlers.values()){
            client.close();
        }
    }

    public ClientHandler getConnection(SocketAddress address){
        if(!clientHandlers.containsKey(address)){
            throw new IllegalArgumentException("No clientHandler for that address!");
        }
        return clientHandlers.get(address);
    }

    public HashMap<SocketAddress, ClientHandler> getConnections(){
        return this.clientHandlers;
    }
}