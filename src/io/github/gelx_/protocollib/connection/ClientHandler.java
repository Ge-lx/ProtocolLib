package io.github.gelx_.protocollib.connection;

import io.github.gelx_.protocollib.ProtocolConnection;
import io.github.gelx_.protocollib.protocol.Packet;
import io.github.gelx_.protocollib.protocol.PacketHandler;
import io.github.gelx_.protocollib.protocol.Protocol;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

public class ClientHandler {

    private static final Logger LOG = Logger.getLogger("ClientHandler");

    private Socket socket;
    private PacketHandler handler;
    private Protocol protocol;

    private Thread recvThread, sendThread;

    private BlockingQueue<Packet> writeQueue = new LinkedBlockingQueue<>(50);

    public ClientHandler(Socket socket, Protocol protocol, ProtocolConnection connection){
        this.protocol = protocol;
        this.handler = protocol.getNewHandler(connection);
        this.socket = socket;

        recvThread = new Thread(new Runnable(){
            public void run(){ runRecv(); }
        });
        sendThread = new Thread(new Runnable(){
            public void run(){ runSend(); }
        });
        recvThread.start();
        sendThread.start();
    }

    public void queuePacketForWrite(Packet packet){
        if(!writeQueue.offer(packet))
            LOG.severe("Could not write packet! Queue overflow!");
    }

    public void runRecv(){
        DataInputStream inputStream;
        try {
            inputStream = new DataInputStream(socket.getInputStream());
        } catch (IOException e) {
            LOG.severe("Could not get inputstream! " + e.getMessage());
            return;
        }
        while(!Thread.interrupted()){
            try {
                short packetID = inputStream.readShort();
                int dataLength = inputStream.readInt();
                byte[] data = new byte[dataLength];
                inputStream.readFully(data);
                handler.queueForHandle(protocol.unpackPacket(socket.getRemoteSocketAddress(), socket.getLocalSocketAddress(), packetID, (data)));
            } catch (EOFException e){
                LOG.info("Connection with " + ((InetSocketAddress)socket.getRemoteSocketAddress()).getHostString() + " closed by remote host!") ;
                Thread.currentThread().interrupt();
            } catch (IOException e) {
                LOG.severe("Error while reading packet! " + e.getMessage());
            }
        }
        try {
            sendThread.interrupt();
            socket.close();
        } catch (IOException e) {
            LOG.severe("Error while closing socket! " + e.getMessage());
        }
    }

    public void runSend(){
        DataOutputStream outputStream;
        try {
            outputStream = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            LOG.severe("Could not get outputStream! " + e.getMessage());
            return;
        }
        while(!Thread.interrupted()){
            try {
                Packet packet = writeQueue.take();
                outputStream.write(protocol.packPacket(packet).array());
            } catch (InterruptedException e) {
                LOG.info("Sending thread interrupted!");
                Thread.currentThread().interrupt();
            } catch (IOException e) {
                LOG.severe("Could not write packet! " + e.getMessage());
            }
        }
    }

    public void close(){
        handler.stop();
        recvThread.interrupt();//Also closes socket and interrupts sender
    }

    public PacketHandler getHandler(){
        return handler;
    }

    public SocketAddress getRemoteAddress(){
        return socket.getRemoteSocketAddress();
    }
}
