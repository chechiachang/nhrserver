/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ccc.nhr.server;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.DatatypeConverter;

/**
 *
 * @author davidchang
 */
public class CoordinatorCommandService {

    private final Socket socket;
    private final OutputStream socketOutputStream;
    private final Command command;

    public CoordinatorCommandService(Socket socket) throws IOException {
        this.socket = socket;
        socketOutputStream = socket.getOutputStream();
        command = new Command();
    }

    //forward command directly
    public void sendCommand(String request) throws IOException {
        String temp = command.getCommand(request);
        byte[] cmd = hexStringToByteArray(temp.replace(" ", ""));
        socketOutputStream.write(cmd);
        socketOutputStream.flush();
        System.out.print("Client cmd : " + DatatypeConverter.printHexBinary(cmd));
        System.out.println();
    }

    public byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
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
