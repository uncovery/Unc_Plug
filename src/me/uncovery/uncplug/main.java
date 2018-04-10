package me.uncovery.uncplug;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import java.sql.*;

public class main extends JavaPlugin {
    
    static JavaPlugin thisPlugin;
    static Connection connection;

    @Override
    public void onEnable(){
        // config file setup
        
        thisPlugin = this;
        this.getConfig().options().copyDefaults(true);
        this.saveConfig();
        this.reloadConfig();

        // command to list all chunks that are loaded.
        getCommand("unc_chunks").setExecutor(new CommandListChunks());
        getCommand("unc_chunks_reset").setExecutor(new CommandListChunks());
        getCommand("unc_chunks_unload").setExecutor(new CommandUnloadChunks());
        getCommand("unc_itemlist").setExecutor(new materials());
        
        // start scheduler
        BukkitScheduler scheduler = Bukkit.getScheduler();
        scheduler.scheduleSyncRepeatingTask(this, new Runnable() {
            @Override
            public void run() {
                new Scheduler().runScheduleTask();
            }
        }, 0L, 20 * 4); // run every minute
    }
    
    public boolean openDBConnection() throws SQLException {
        // MySQL setup
        // make a general check on plugin start if the MySQL connection works
        try { //We use a try catch to avoid errors, hopefully we don't get any.
            Class.forName("com.mysql.jdbc.Driver"); //this accesses Driver in jdbc.
        } catch (ClassNotFoundException e) {
            System.err.println("jdbc driver unavailable!");
            return false;
        }
        
        String sql_server = thisPlugin.getConfig().getString("sql_server");
        String sql_port =  thisPlugin.getConfig().getString("sql_server");
        String sql_dbname =  thisPlugin.getConfig().getString("sql_dbname");
        String sql_username =  thisPlugin.getConfig().getString("sql_username");
        String sql_password =  thisPlugin.getConfig().getString("sql_password");
        
        try { //Another try catch to get any SQL errors (for example connections errors)
            String sqlUrl =  "jdbc:mysql://" + sql_server + ":" + sql_port + "/" + sql_dbname;

            connection = DriverManager.getConnection(sqlUrl, sql_username, sql_password );
            //with the method getConnection() from DriverManager, we're trying to set
            //the connection's url, username, password to the variables we made earlier and
            //trying to get a connection at the same time. JDBC allows us to do this.
        } catch (SQLException e) {
            //catching errors)
            //prints out SQLException errors to the console (if any)
            return false;
        }
        return true;
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

    @Override
    public void onDisable() {
        // invoke on disable to close the MySQL connection

    }
}
