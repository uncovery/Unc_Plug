package me.uncovery.uncplug;
import org.bukkit.plugin.java.JavaPlugin;
import java.sql.*;

public class main extends JavaPlugin {
    static Connection connection;

    @Override
    public void onEnable(){
        // config file setup
        this.getConfig().options().copyDefaults(true);
        this.saveConfig();
        this.reloadConfig();

        // command to list all chunks that are loaded.
        getCommand("unc_chunks").setExecutor(new CommandListChunks());
        getCommand("unc_chunks_reset").setExecutor(new CommandListChunks());
        getCommand("unc_chunks_unload").setExecutor(new CommandUnloadChunks());

        // make a general check on plugin start if the MySQL connection works
        try { //We use a try catch to avoid errors, hopefully we don't get any.
            Class.forName("com.mysql.jdbc.Driver"); //this accesses Driver in jdbc.
        } catch (ClassNotFoundException e) {
            System.err.println("jdbc driver unavailable!");
            return;
        }

        // MySQL setup
        String sqlUrl = "jdbc:mysql://" + this.getConfig().getString("sql_server") + ":" + this.getConfig().getString("sql_port") + "/" + this.getConfig().getString("sql_dbname");
        try { //Another try catch to get any SQL errors (for example connections errors)
            connection = DriverManager.getConnection(
                    sqlUrl,
                    this.getConfig().getString("sql_username"),
                    this.getConfig().getString("sql_password")
            );
            //with the method getConnection() from DriverManager, we're trying to set
            //the connection's url, username, password to the variables we made earlier and
            //trying to get a connection at the same time. JDBC allows us to do this.
        } catch (SQLException e) {
            //catching errors)
            //prints out SQLException errors to the console (if any)
        }

        /*
        // we run the chunk lister on plugin start to get a base case
        boolean check = new CommandListChunks().getLoadedChunks(getTPS());
        if (!check) {
            System.err.println("failed to write loaded chunks to DB!");
        } else {
            System.out.println("Wrote loaded chunks to DB on startup!");
        }
        */
    }

    @Override
    public void onDisable() {
        // invoke on disable to close the MySQL connection
        try { //using a try catch to catch connection errors (like wrong sql password...)
            if (connection != null && !connection.isClosed()){ //checking if connection isn't null to
                //avoid receiving a nullpointer
                connection.close(); //closing the connection field variable.
                System.err.println("jdbc driver connection closed!");
            }
        } catch(SQLException e) {

        }
    }

}
