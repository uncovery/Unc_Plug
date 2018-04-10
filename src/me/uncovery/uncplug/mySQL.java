/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.uncovery.uncplug;

import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import static me.uncovery.uncplug.main.thisPlugin;
import static me.uncovery.uncplug.main.connection;

/**
 *
 * @author spiesol01
 */
public class mySQL {
    public boolean openDBConnection() throws SQLException {
        
        // MySQL setup
        // make a general check on plugin start if the MySQL connection works
        try { //We use a try catch to avoid errors, hopefully we don't get any.
            Class.forName("com.mysql.jdbc.Driver"); //this accesses Driver in jdbc.
        } catch (ClassNotFoundException e) {
            System.err.println("jdbc driver unavailable:" + e);
        }
        
        String sql_server = thisPlugin.getConfig().getString("sql_server");
        String sql_port =  thisPlugin.getConfig().getString("sql_port");
        String sql_databasename =  thisPlugin.getConfig().getString("sql_databasename");
        String sql_username =  thisPlugin.getConfig().getString("sql_username");
        String sql_password =  thisPlugin.getConfig().getString("sql_password");
        
        String sqlUrl =  "jdbc:mysql://" + sql_server + ":" + sql_port + "/" + sql_databasename;
        
        try { //Another try catch to get any SQL errors (for example connections errors)
            connection = DriverManager.getConnection(sqlUrl, sql_username, sql_password);
            System.out.println("jdbc connection established!");
            return true;
        } catch (SQLException e) {
            //catching errors)
            System.err.println("Error establishing connection:" + sqlUrl +  " with error " + e);
            return false;
        }
    }

    public boolean closeDBConnection() throws SQLException {
        try { //using a try catch to catch connection errors (like wrong sql password...)
            if (connection != null && !connection.isClosed()){ //checking if connection isn't null to
                //avoid receiving a nullpointer
                connection.close(); //closing the connection field variable.
                System.err.println("jdbc connection closed!");
            }
        } catch(SQLException e) {
            return false;
        }
        return true;
    }
    
    public long executeSQLUpdate(String sql, boolean getInsertID) throws SQLException {
        try (
            PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        ) {
            statement.executeUpdate();
            if (getInsertID) {
                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        return generatedKeys.getLong(1);
                    } else {
                        throw new SQLException("SQL Statement did not create insert ID: " + sql);
                    }
                }
            }
        } catch (SQLException S) {
            System.err.println("DB Error in executeSQLUpdate: " + S);
            return -1;
        }
        return 0;
    }    
}
