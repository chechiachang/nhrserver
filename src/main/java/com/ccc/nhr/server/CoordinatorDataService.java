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

    boolean isPrintout = false;

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

    public void getRequest() throws IOException {
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

                //head scanner
                scanner[0] = scanner[1];
                scanner[1] = scanner[2];
                scanner[2] = scanner[3];
                scanner[3] = s;

                //refresh dataLength
                if (scanner[0].equals("41") && scanner[1].equals("54") && scanner[2].equals("2b")) {
                    dataLength = Integer.parseInt(s, 16);
                    count = 3;
                    output = new String[dataLength + 3];
                    output[0] = "41";
                    output[1] = "54";
                    output[2] = "2b";
                }

                //output
                if (s.length() == 1) {
                    s = "0" + s;
                }
                output[count] = s;
            //System.out.print(s + " ");

                /*
                 //if read head
                 if (count < 4) {

                 } else //if finished read frame type
                 if (count == 4) {

                 } else //if finished read device IEEE address
                 if (count == 12) {

                 } else //if finished read device short address
                 if (count == 14) {

                 } else //if finished read Source Endpoint
                 if (count == 15) {

                 } else //if finished read Destination Endpoint
                 if (count == 16) {

                 } else //if finished read Cluster ID
                 if (count == 18) {

                 } else //if finished read Profile ID
                 if (count == 20) {

                 } else //if finished read Recieve options
                 if (count == 21) {

                 } else //if finished read Frame Control byte
                 if (count == 22) {

                 } else //if finished read Transit sequence number
                 if (count == 23) {

                 } else //if finished read ZCL command
                 if (count == 24) {

                 } else //if finished read Attribute ID
                 if (count == 26) {

                 } else //if finished read data type
                 if (count == 27) {

                 } else //if finished read value
                 if (count == 29) {

                 } else //if finished read check byte
                 if (count == 30) {
                
                 }
                 */
                //if at the end of data
                if (end = (count == dataLength)) {
                    String out = "";
                    System.out.println(Arrays.toString(output));

                    //if is devices data 
                    if (output.length > 26) {
                        //switch Cluster ID
                        switch (output[17]) {
                            case "00":
                                switch (output[18]) {
                                    case "01":  //ZigBee Cluster Library power configuration cluster ID
                                        break;
                                }
                                break;
                            case "04":
                                switch (output[18]) {
                                    case "05":  //0405 ZigBee Cluster Library relative humidity measurement cluster ID
                                        String humid = String.valueOf(Integer.parseInt(output[29] + output[28], 16));

                                        pstmt = conn.prepareStatement("INSERT INTO `data` (`mac_cluster_id`, `short_mac`, `cluster_id`, `data`) VALUES (?,?,?,?) ON DUPLICATE KEY UPDATE data=VALUES(data)");
                                        pstmt.setString(1, returnOutput(5, 12) + "0405");
                                        pstmt.setString(2, returnOutput(13, 14));
                                        pstmt.setString(3, "0405");
                                        pstmt.setString(4, humid.substring(0, 2) + "." + humid.substring(2, 4));
                                        pstmt.executeUpdate();

                                        if (isPrintout) {
                                            out = "Mac Address: " + returnOutput(5, 12) + " Device Short Mac: " + returnOutput(13, 14) + " Cluster ID: " + "0405" + " Data: " + humid.substring(0, 2) + "." + humid.substring(2, 4) + " %";
                                            System.out.println(out);
                                        }
                                        break;
                                    case "02":  //ZigBee Cluster Library relative humidity measurement cluster ID 0x0405
                                        String temp = String.valueOf(Integer.parseInt(output[29] + output[28], 16));

                                        pstmt = conn.prepareStatement("INSERT INTO `data` (`mac_cluster_id`, `short_mac`, `cluster_id`, `data`) VALUES (?,?,?,?) ON DUPLICATE KEY UPDATE data=VALUES(data)");
                                        pstmt.setString(1, returnOutput(5, 12) + "0402");
                                        pstmt.setString(2, returnOutput(13, 14));
                                        pstmt.setString(3, "0402");
                                        pstmt.setString(4, temp.substring(0, 2) + "." + temp.substring(2, 4));
                                        pstmt.executeUpdate();

                                        if (isPrintout) {
                                            out = "Mac Address: " + returnOutput(5, 12) + " Device Short Mac: " + returnOutput(13, 14) + " Cluster ID: " + "0402" + " Data: " + temp.substring(0, 2) + "." + temp.substring(2, 4) + " Celcius";
                                            System.out.println(out);
                                        }
                                        break;
                                }
                                break;
                            case "05":
                                switch (output[18]) {
                                    case "00":  //0500 (ZCL_CLUSTER_ID_SS_IAS_ZONE)
                                        switch (output[26]) {
                                            case "03":
                                                pstmt = conn.prepareStatement("INSERT INTO `data` (`mac_cluster_id`, `short_mac`, `cluster_id`, `data`) VALUES (?,?,?,?) ON DUPLICATE KEY UPDATE data=VALUES(data)");
                                                pstmt.setString(1, returnOutput(5, 12) + "0500");
                                                pstmt.setString(2, returnOutput(13, 14));
                                                pstmt.setString(3, "0500");
                                                pstmt.setString(4, "on");
                                                pstmt.executeUpdate();
                                                if (isPrintout) {
                                                    System.out.println("Mac Address: " + returnOutput(5, 12) + " Device Short Mac: " + returnOutput(13, 14) + " Cluster ID: " + "0500" + " Data: " + "on");
                                                }
                                                break;
                                            case "00":
                                                pstmt = conn.prepareStatement("INSERT INTO `data` (`mac_cluster_id`, `short_mac`, `cluster_id`, `data`) VALUES (?,?,?,?) ON DUPLICATE KEY UPDATE data=VALUES(data)");
                                                pstmt.setString(1, returnOutput(5, 12) + "0500");
                                                pstmt.setString(2, returnOutput(13, 14));
                                                pstmt.setString(3, "0500");
                                                pstmt.setString(4, "off");
                                                pstmt.executeUpdate();
                                                if (isPrintout) {
                                                    System.out.println("Mac Address: " + returnOutput(5, 12) + " Device Short Mac: " + returnOutput(13, 14) + " Cluster ID: " + "0500" + " Data: " + "off");
                                                }
                                                break;
                                            case "08":
                                                pstmt = conn.prepareStatement("INSERT INTO `data` (`mac_cluster_id`, `short_mac`, `cluster_id`, `data`) VALUES (?,?,?,?) ON DUPLICATE KEY UPDATE data=VALUES(data)");
                                                pstmt.setString(1, returnOutput(5, 12) + "0500");
                                                pstmt.setString(2, returnOutput(13, 14));
                                                pstmt.setString(3, "0500");
                                                pstmt.setString(4, "on/low");
                                                pstmt.executeUpdate();
                                                if (isPrintout) {
                                                    System.out.println("Mac Address: " + returnOutput(5, 12) + " Device Short Mac: " + returnOutput(13, 14) + " Cluster ID: " + "0500" + " Data: " + "on / low battery");
                                                }
                                                break;
                                            case "0c":
                                                pstmt = conn.prepareStatement("INSERT INTO `data` (`mac_cluster_id`, `short_mac`, `cluster_id`, `data`) VALUES (?,?,?,?) ON DUPLICATE KEY UPDATE data=VALUES(data)");
                                                pstmt.setString(1, returnOutput(5, 12) + "0500");
                                                pstmt.setString(2, returnOutput(13, 14));
                                                pstmt.setString(3, "0500");
                                                pstmt.setString(4, "off/low");
                                                pstmt.executeUpdate();
                                                if (isPrintout) {
                                                    System.out.println("Mac Address: " + returnOutput(5, 12) + " Device Short Mac: " + returnOutput(13, 14) + " Cluster ID: " + "0500" + " Data: " + "off / low battery");
                                                }
                                                break;
                                        }
                                        break;
                                }
                                break;
                        }
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
