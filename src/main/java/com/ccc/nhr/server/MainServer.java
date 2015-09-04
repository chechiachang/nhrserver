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

        //Command Service listen to Console
        CoordinatorCommandThread cct = new CoordinatorCommandThread(ccs);
        cct.start();
        //Client WerSocket Service
        ClientWebSocketThread cwst = new ClientWebSocketThread(clientPort, ccs);
        cwst.start();
        //
        CoordinatorDataThread cdt = new CoordinatorDataThread(socket);
        cdt.start();

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


