package com.blendycat.prison.sql;

import com.blendycat.prison.QuadPrison;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Created by EvanMerz on 10/19/17.
 */
class DatabaseManager {

    /**
     * remember to close after done with database
     * @return database connection
     */
    static Connection getConnection() {
        String DB_CONN_STRING = "jdbc:mysql://" + QuadPrison.getDatabaseHost() + ":3306/"+
                QuadPrison.getDatabaseName();
        String USER_NAME = QuadPrison.getDatabaseUser();
        String PASSWORD = QuadPrison.getDatabasePassword();
        Connection result = null;

        try {
            result = DriverManager.getConnection(DB_CONN_STRING, USER_NAME, PASSWORD);
        }
        catch (SQLException ex){
            ex.printStackTrace();
        }
        return result;
    }
}
