/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.uncovery.uncplug;

import java.sql.DriverManager;
import java.sql.SQLException;
import static me.uncovery.uncplug.main.thisPlugin;
import java.sql.*;
import static me.uncovery.uncplug.main.connection;

/**
 *
 * @author spiesol01
 */
public class mySQL {
    public Connection openDBConnection() throws SQLException {
        
        // MySQL setup
        // make a general check on plugin start if the MySQL connection works
        try { //We use a try catch to avoid errors, hopefully we don't get any.
            Class.forName("com.mysql.jdbc.Driver"); //this accesses Driver in jdbc.
        } catch (ClassNotFoundException e) {
            System.err.println("jdbc driver unavailable!");
        }
        
        String sql_server = thisPlugin.getConfig().getString("sql_server");
        String sql_port =  thisPlugin.getConfig().getString("sql_server");
        String sql_dbname =  thisPlugin.getConfig().getString("sql_dbname");
        String sql_username =  thisPlugin.getConfig().getString("sql_username");
        String sql_password =  thisPlugin.getConfig().getString("sql_password");
        
        try { //Another try catch to get any SQL errors (for example connections errors)
            String sqlUrl =  "jdbc:mysql://" + sql_server + ":" + sql_port + "/" + sql_dbname;
            connection = DriverManager.getConnection(sqlUrl, sql_username, sql_password );
        } catch (SQLException e) {
            //catching errors)
            //prints out SQLException errors to the console (if any)
        }
        return connection;
    }

    public boolean closeDBConnection() throws SQLException {
        try { //using a try catch to catch connection errors (like wrong sql password...)
            if (connection != null && !connection.isClosed()){ //checking if connection isn't null to
                //avoid receiving a nullpointer
                connection.close(); //closing the connection field variable.
                System.err.println("jdbc driver connection closed!");
            }
        } catch(SQLException e) {
            return false;
        }
        return true;
    }
}
