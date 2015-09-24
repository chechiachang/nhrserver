/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ccc.nhr.server;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Date;

/**
 *
 * @author davidchang
 */
public class CoordinatorDataService {

    private final Socket socket;
    private final DataInputStream dataInputStream;

    public CoordinatorDataService(Socket socket) throws IOException {
        this.socket = socket;
        this.dataInputStream = new DataInputStream(socket.getInputStream());
    }

    boolean isPrintout = false; //set true to debug

    String s = null;
    String[] scanner = {"0", "0", "0", "0"};
    String[] output = new String[33];

    static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
    static final String DB_URL = "jdbc:mysql://localhost:3306/nhr?connectTimeout=3000";
    //  Database credentials
    static final String USER = "nhr";
    static final String PASS = "25ac7375c1fd64eca8dd8cf309071c0d";
    private static final long serialVersionUID = 1L;

    Connection conn = null;
    PreparedStatement pstmt = null;
    ResultSet rs;

    public void getRequest() throws IOException {   //test method
        String str = null;
        System.out.print(new Date() + " -> ");
        while ((str = Integer.toHexString(dataInputStream.read())) != null) {
            if ("41".equals(str)) {
                System.out.println();
                System.out.print(new Date() + " -> ");
            }
            if (str.length() == 1) {
                str = "0" + str;
            }
            System.out.print(str + " ");
        }
    }

    public void getScannerRequest() throws IOException, SQLException, ClassNotFoundException {
        if (isPrintout) {
            System.out.print(new Date() + " -> ");
        }
        int dataLength = 10;   //any number larger than 5 
        int count = 0;
        boolean end = false;

        try {
            Class.forName(JDBC_DRIVER);
            conn = DriverManager.getConnection(DB_URL, USER, PASS);

            while ((s = Integer.toHexString(dataInputStream.read())) != null) {
                //
                if (isPrintout && end == true) {
                    System.out.println();
                    System.out.print(new Date() + " -> ");
                }

                /*
                 * head scanner
                 * each input byte sequently put into scanner
                 * if scanner get 'A' 'T' '+'
                 * next byte will be total packet length
                 */
                scanner[0] = scanner[1];
                scanner[1] = scanner[2];
                scanner[2] = scanner[3];
                scanner[3] = s; //get data length(hex)

                //refresh dataLength
                if (scanner[0].equals("41") && scanner[1].equals("54") && scanner[2].equals("2b")) {
                    dataLength = Integer.parseInt(s, 16);   //parse data length to decimal
                    count = 3;
                    output = new String[dataLength + 3];
                    output[0] = "41";
                    output[1] = "54";
                    output[2] = "2b";
                }

                if (s.length() == 1) {  // if packet has less than 10 bytes, add "0" to data length byte
                    s = "0" + s;
                }
                output[count] = s;
                //System.out.print(s + " ");
                if (end = (count == dataLength)) {  //at the end of data, println output
                    String out;
                    System.out.println(Arrays.toString(output));

                    //Save data into database
                    if (output.length > 35) {   // Cluster Library device power status reply, length = 0x21 = 33, +2 null = 35
                        switch (output[17] + output[18]) {  //switch Cluster ID
                            case "0001":    //ZigBee Cluster Library power config battery voltage ID 0x0405
                                String voltage = String.valueOf((double) Integer.parseInt(output[28], 16) / 10);
                                String battery = "00".equals(output[32]) ? "0" : "1";
                                pstmt = conn.prepareStatement("UPDATE `data` SET `voltage` = ?, `battery` = ? WHERE `mac_cluster_id` = ? AND `short_mac` = ? ");
                                pstmt.setString(1, voltage);
                                pstmt.setString(2, battery);
                                pstmt.setString(3, returnOutput(5, 12));
                                pstmt.setString(4, returnOutput(13, 14));
                                pstmt.executeUpdate();
                                if (isPrintout) {
                                    out = "Mac Address: " + returnOutput(5, 12) + " Device Short Mac: " + returnOutput(13, 14) + " Cluster ID: " + "0001" + " Data: " + voltage + " V";
                                    System.out.println(out);
                                }
                                break;
                        }   //switch
                    } else if (output.length > 31) {   // Cluster Library device reply data, length = 0x1e = 30, +2 null = 32
                        String data = String.valueOf(Integer.parseInt(output[29] + output[28], 16));
                        switch (output[17] + output[18]) {  //switch Cluster ID
                            case "0001":    //ZigBee Cluster Library power configuration cluster ID
                                break;
                            case "0405":    //0405 ZigBee Cluster Library relative humidity measurement cluster ID
                                pstmt = conn.prepareStatement("INSERT INTO `data` (`mac_cluster_id`, `short_mac`, `data`) VALUES (?,?,?) ON DUPLICATE KEY UPDATE data=VALUES(data)");
                                pstmt.setString(1, returnOutput(5, 12));
                                pstmt.setString(2, returnOutput(13, 14));
                                pstmt.setString(3, data.substring(0, 2) + "." + data.substring(2, 4));
                                pstmt.executeUpdate();
                                if (isPrintout) {
                                    out = "Mac Address: " + returnOutput(5, 12) + " Device Short Mac: " + returnOutput(13, 14) + " Cluster ID: " + "0405" + " Data: " + data.substring(0, 2) + "." + data.substring(2, 4) + " %";
                                    System.out.println(out);
                                }
                                break;
                            case "0402":    //ZigBee Cluster Library relative humidity measurement cluster ID 0x0405
                                pstmt = conn.prepareStatement("INSERT INTO `data` (`mac_cluster_id`, `short_mac`, `data2`) VALUES (?,?,?) ON DUPLICATE KEY UPDATE data=VALUES(data2)");
                                pstmt.setString(1, returnOutput(5, 12));
                                pstmt.setString(2, returnOutput(13, 14));
                                pstmt.setString(3, data.substring(0, 2) + "." + data.substring(2, 4));
                                pstmt.executeUpdate();
                                if (isPrintout) {
                                    out = "Mac Address: " + returnOutput(5, 12) + " Device Short Mac: " + returnOutput(13, 14) + " Cluster ID: " + "0402" + " Data: " + data.substring(0, 2) + "." + data.substring(2, 4) + " %";
                                    System.out.println(out);
                                }
                                break;
                        }
                    } else if (output.length > 28) {    //data , length = 0x1e = 30, +2 null = 32
                        String IASCmd = "";
                        switch (output[17] + output[18]) {
                            case "0500":    //0500 (ZCL_CLUSTER_ID_SS_IAS_ZONE)
                                switch (output[26]) {
                                    case "03":  //on
                                        IASCmd = "on";
                                        break;
                                    case "00":  //off
                                        IASCmd = "off";
                                        break;
                                    case "08":  //on & low battery
                                        IASCmd = "on/low";
                                        break;
                                    case "0c":  //off & low battery
                                        IASCmd = "off/low";
                                        break;
                                }
                                pstmt = conn.prepareStatement("INSERT INTO `data` (`mac_cluster_id`, `short_mac`, `data`) VALUES (?,?,?) ON DUPLICATE KEY UPDATE data=VALUES(data)");
                                pstmt.setString(1, returnOutput(5, 12));
                                pstmt.setString(2, returnOutput(13, 14));
                                pstmt.setString(3, IASCmd);
                                pstmt.executeUpdate();
                                if (isPrintout) {
                                    System.out.println("Mac Address: " + returnOutput(5, 12) + " Device Short Mac: " + returnOutput(13, 14) + " Cluster ID: " + "0500" + " Data: " + "on");
                                }
                                break;
                        }
                    } else if (output.length > 21) {   // Local Command query reply packet [19+2]

                    }
                    count = 0;

                }
                //move on count
                count++;
            }
        } catch (SQLException e) {

        } finally {
            try {
                if (pstmt != null) {
                    pstmt.close();
                }
            } catch (SQLException se2) {
                se2.printStackTrace();
            }// nothing we can do
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException se) {
                se.printStackTrace();
            }//end finally try
        }
    }

    String returnOutput(int startIndex, int endIndex) {
        String text = "";
        for (int i = startIndex; i < endIndex + 1; i++) {
            text = text + output[i];
        }
        return text;
    }
}
