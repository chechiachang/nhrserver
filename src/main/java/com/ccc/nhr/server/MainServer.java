/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ccc.nhr.server;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.sql.SQLException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author davidchang
 */
public class MainServer {

    public static void main(String args[]) throws IOException, SQLException, ClassNotFoundException {

        final String host = "192.168.16.146";
        final int coordinatorPort = 10010;
        final int clientPort = 10020;

        Socket socket = new Socket(host, coordinatorPort);
        System.out.println("Creating socket to '" + host + "' on port " + coordinatorPort);

        //public service
        CoordinatorCommandService ccs = new CoordinatorCommandService(socket);

        CoordinatorDataThread cdt = new CoordinatorDataThread(socket);
        cdt.start();

        //Command Service listen to Console
        CoordinatorCommandThread cct = new CoordinatorCommandThread(ccs);
        cct.start();
        //Client WerSocket Service
        ClientWebSocketThread cwst = new ClientWebSocketThread(clientPort);
        cwst.start();
    }
}

class CoordinatorDataThread extends Thread {

    private CoordinatorDataService cds;

    public CoordinatorDataThread(Socket socket) throws IOException {
        this.cds = new CoordinatorDataService(socket);
    }

    @Override
    public void run() {
        try {
            cds.getScannerRequest();
            //nds.getRequest();
        } catch (IOException | SQLException | ClassNotFoundException ex) {
            Logger.getLogger(CoordinatorDataThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}

class ClientWebSocketThread extends Thread {

    private int clientPort;
    private ClientWebSocketService cwss;

    public ClientWebSocketThread(int clientPort) {
        this.clientPort = clientPort;
    }

    @Override
    public void run() {
        try {
            cwss = new ClientWebSocketService(clientPort);
            cwss.start();
            System.out.println("ClientWebSocketService started on port: " + clientPort);

        } catch (UnknownHostException ex) {
            Logger.getLogger(ClientWebSocketThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}

class CoordinatorCommandThread extends Thread {

    private CoordinatorCommandService ccs;

    public CoordinatorCommandThread(CoordinatorCommandService ccs) {
        this.ccs = ccs;
    }

    @Override
    public void run() {
        try {
            Scanner scanner = new Scanner(System.in);
            while (true) {
                ccs.sendCommand(scanner.next());
            }
        } catch (IOException ex) {
            Logger.getLogger(CoordinatorCommandThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
