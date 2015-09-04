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
public class DatabaseConnector {
    public final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
    public final String DB_URL = "jdbc:mysql://localhsot:3306/nhr?connectionTimeout=3000";
    public final String USER = "nhr";
    public final String PASS = "25ac7375c1fd64eca8dd8cf309071c0d";

    public String getJDBC_DRIVER() {
        return JDBC_DRIVER;
    }

    public String getDB_URL() {
        return DB_URL;
    }

    public String getUSER() {
        return USER;
    }

    public String getPASS() {
        return PASS;
    }
    
}
