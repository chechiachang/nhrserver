/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ccc.nhr.server;

import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.java_websocket.WebSocket;
import org.java_websocket.framing.Framedata;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

/**
 *
 * @author davidchang
 */
public class ClientWebSocketService extends WebSocketServer {

    CoordinatorCommandService ccs;

    public void setCcs(CoordinatorCommandService ccs) {
        this.ccs = ccs;
    }

    public ClientWebSocketService(int port) throws UnknownHostException {
        super(new InetSocketAddress(port));
    }

    public ClientWebSocketService(InetSocketAddress address) {
        super(address);
    }

    @Override
    public void onOpen(WebSocket ws, ClientHandshake ch) {
        this.sendToAll("new connection: " + ch.getResourceDescriptor());
        System.out.println(ws.getRemoteSocketAddress().getAddress().getHostAddress() + " connected!");
    }

    @Override
    public void onClose(WebSocket ws, int i, String string, boolean bln) {
        this.sendToAll(ws + " has disconnected!");
        System.out.println(ws + " has disconnected!");
    }

    @Override
    public void onMessage(WebSocket ws, String message) {
        this.sendToAll(message);
        System.out.println(ws + ": " + message);
        try {
            ccs.sendCommand(message);
        } catch (Exception e) {

        }
    }

    public void onFragment(WebSocket conn, Framedata fragment) {
        System.out.println("received fragment: " + fragment);
    }

    @Override
    public void onError(WebSocket ws, Exception ex) {
        ex.printStackTrace();
        if (ws != null) {
            // some errors like port binding failed may not be assignable to a specific websocket
        }
    }

    /**
     * Sends <var>text</var> to all currently connected WebSocket clients.
     *
     * @param text The String to send across the network.
     * @throws InterruptedException When socket related I/O errors occur.
     */
    public void sendToAll(String text) {
        Collection<WebSocket> con = connections();
        synchronized (con) {
            for (WebSocket c : con) {
                c.send(text);
            }
        }
    }
}

class ClientWebSocketThread extends Thread {

    private int clientPort;
    private ClientWebSocketService cwss;
    private CoordinatorCommandService ccs;

    public ClientWebSocketThread(int clientPort, CoordinatorCommandService ccs) {
        this.clientPort = clientPort;
        this.ccs = ccs;
    }

    @Override
    public void run() {
        try {
            cwss = new ClientWebSocketService(clientPort);
            cwss.setCcs(ccs);
            cwss.start(); //start webSocket Service
            System.out.println("ClientWebSocketService started on port: " + clientPort);

        } catch (UnknownHostException ex) {
            Logger.getLogger(ClientWebSocketThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
