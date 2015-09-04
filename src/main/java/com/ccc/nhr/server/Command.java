/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ccc.nhr.server;

/**
 *
 * @author davidchang
 */
public class Command {

    //encode command string to hex string
    public String asciiToHex(String ascii) {
        char[] chars = ascii.toCharArray();
        String hex = "";
        for (int i = 0; i < chars.length; i++) {
            hex += " " + (Integer.toHexString((int) chars[i]));
        }
        return hex;
    }

    //calculate last checkbaye
    public String calCheckbyte(String sHex) {
        String replace = sHex.replace(",", "").replace(" ", "");
        int decimalSum = 0;
        for (int i = 0; i < replace.length() / 2; i++) {
            decimalSum += Integer.parseInt(replace.substring(2 * i, 2 * (i + 1)), 16);
        }
        int balance = 255 - decimalSum;
        while (balance < 0) {
            balance += 256;
        }
        return (balance < 16 ? "0" : "") + Integer.toHexString(balance);
    }

    public String getCommand(String request) {
        switch (request) {
            case "query"://DF query the device list under coordinator
                return this.createCommand("DF");
            case "channel"://RC read coordinator operation channel
                return this.createCommand("RC");
            case "operationid"://RI read coordinator operation pain id
                return this.createCommand("RI");
            case "extendid"://RE read coordinator extend id
                return this.createCommand("RE");
            case "mac"://RA read coordinator IEEE address
                return this.createCommand("RA");
            case "sirenon":
                String mac = "00 12 4b 00 05 a7 bb b8";
                String shortMac = "d0 b5";
                String clusterId = "05 02";
                String cmd = "00 10";
                String duration = "00 01";
                return this.createCommand(mac, shortMac, clusterId, cmd, duration);
            default:
                return "";
        }
    }

    //Coordinator Local command
    public String createCommand(String cmd) {
        String out = "41 54 2b" //head AT+
                + " 07" //length 07
                + " 08" //frame 08
                + asciiToHex(cmd.toUpperCase()); // cmd A A
        return out + " " + this.calCheckbyte(out); //add last checkbyte
    }

    //ZCL command
    //Cluster command ex.A10 Siren
    public String createCommand(String mac, String shortMac, String clusterId, String cmd, String duration) {
        String out = "41 54 2b" //head AT+, 3 bytes
                + " 18" //langth 18 for A10 siren
                + " 12" //Cluster frame
                + " " + mac // 8 bytes
                + " " + shortMac // 2 bytes
                + " 02" //Source 
                + " 02" //Destination, 02 for Siren
                + " " + clusterId // 2 bytes
                + " 01 04" //Profile ID, 2 bytes
                + " 00" //reserved
                + " " + cmd //on/off cluster command, 2 bytes, 00 10 /00 00
                + " " + duration; // A10 siren alarm duration, 2 bytes, 0x0001 = 1sec

        return out + " " + this.calCheckbyte(out);
    }
/*
    public static void main(String[] args) {
        Command command = new Command();
        System.out.println(command.createCommand("rc"));
        System.out.println(command.createCommand("ra"));
        System.out.println(command.createCommand("ri"));
        System.out.println(command.createCommand("00 12 4b 00 05 a7 bb b8", "d0 b5", "05 02", "00 10", "00 01"));

    }
*/
}
