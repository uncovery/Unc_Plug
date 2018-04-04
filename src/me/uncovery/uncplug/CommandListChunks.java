package me.uncovery.uncplug;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.Chunk;
import net.minecraft.server.v1_12_R1.MinecraftServer;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import java.sql.*;
import static me.uncovery.uncplug.main.connection;


/*
Command structure to write list of loaded chunks to database
*/
public class CommandListChunks implements CommandExecutor   {
    @Override

    // what do we execute when the command is run?
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (label.equals("unc_chunks_reset")) {
            try {
                // this always throws an error, we should use a different function to do this
                resetChunkDB();
                System.out.println("DB Reset!");
                return true;
            } catch (SQLException ex) {
                return false;
            }
        }

        boolean check_exec = false;
        try {
            check_exec = getLoadedChunks();
        } catch (SQLException ex) {
            Logger.getLogger(CommandListChunks.class.getName()).log(Level.SEVERE, null, ex);
        }
        return check_exec;
    }

    /**
     * gets the current minute TPS as double
     * @return double
     */
    public double getTPS() {
        double[] tps = MinecraftServer.getServer().recentTps;
        if (tps[0] > 20) {
            return 20;
        }
        return tps[0];
    }

    /**
     * get a list of all loaded chunks per world, write them to database with TPS information
     *
     * @return
     * @throws java.sql.SQLException
     */
    public boolean getLoadedChunks() throws SQLException {
        // get current TPS
        double tps = getTPS();

        connection = new mySQL().openDBConnection();

        // status message for other command line arguments
        System.out.println("Collecting loaded chunks @ TPS of " + tps);

        // Loops through each world
        for (World currentWorld : Bukkit.getWorlds()) {

            // define the arraylist that holds all the Chunk insert IDs
            ArrayList<String> ChunkIDs = new ArrayList<>();

            // get the world name to a string
            String worldname = currentWorld.getName();
            System.out.println(" Processing chunks in " + worldname + "...");

            // start a counter so we can count the loaded chunks per world
            int i = 0;
            // Loops through each of the world's chunks
            for (Chunk currentChunk : currentWorld.getLoadedChunks()) {
                // Gets the coordinates for each chunk
                int xCoord = currentChunk.getX();
                int zCoord = currentChunk.getZ();

                // skip spawn chunks
                if (xCoord > 7 || zCoord > 7) {
                    try {
                        // write the chunk data and command line argument (should be TPS, ideally) to the DB
                        long insert_id = writeChunkData(worldname, xCoord, zCoord);
                        // it seems there is some bug and a wrong/zero insert ID is returned, so check if that is the case
                        if (insert_id < 1) {
                            System.out.println("writeChunkData Error: ChunkID is zero");
                            return false;
                        }
                        // we make a string here already for the SQL so that we can easily join them later
                        ChunkIDs.add("(" + insert_id + "," + tps + ")");
                        i++; // count chunks
                    } catch (SQLException ex) {
                        System.out.println("writeChunkData Error in getLoadedChunks: " + ex);
                        return false;
                    }
                }
            }

            int itemCount = ChunkIDs.size();
            System.out.println("Processing : " + itemCount + " Chunk IDs");

            int j = 0;
            int k = 0;
            // we only add if there are elements
            if (itemCount == 0) {
                System.out.println("No loaded chunks in " + worldname);
            } else {
                for (j = 50; j<=itemCount; j+= 50){
                    if (j > itemCount) {
                        j = itemCount;
                    }
                    System.out.println("listing ChunkIDs from " + k + " to " + j);
                    List<String> ListPiece = ChunkIDs.subList(k, j);
                    try {
                        writeChunkEvent(ListPiece);
                    } catch (SQLException ex) {
                        System.out.println("writeChunkData Error in getLoadedChunks: " + ex);
                        return false;
                    }
                    k = j;
                }
            }
        }
        System.out.println("Done writing to database!");

        new mySQL().closeDBConnection();
        return true;
    }

    // thisis only the SQL to insert new chunks, get the chunk ID and then write the actual status to the events
    public long writeChunkData(String world, int xCoord, int zCoord) throws SQLException {
        // find the key
        long InsertID = 0;
        String search_sql = "SELECT chunk_id FROM minecraft_log.lag_chunks WHERE world='" + world + "' AND x_coord=" + xCoord + " AND z_coord="+ zCoord;
        System.out.println("search SQL:" + search_sql);
        
        
        try (
            PreparedStatement statement = connection.prepareStatement(search_sql);
            ResultSet results = statement.executeQuery();
        ) {
            if (!results.isBeforeFirst() ) {
                String sql = "INSERT INTO minecraft_log.lag_chunks SET world='" + world + "', x_coord="+xCoord+", z_coord="+ zCoord;
                System.out.println("insert SQL:" + sql);
                InsertID = executeSQLUpdate(sql, true);
                return InsertID;
            } else {
                while (results.next()) {
                    InsertID = results.getLong("chunk_id");
                    System.out.println("chunk id:" + InsertID);
                    return InsertID;
                }
            }
        } catch (SQLException S) {
            System.out.println("writeChunkData Error Error searching ChunkID" + S);
            return 0;
        }

        return 0;
    }

    // write the event to the DB. The timestamp is automatic in SQL
    public boolean writeChunkEvent(List<String> ChunkIDs) throws SQLException {
        // take the SQL statement and the add the string of (a,b) with a comma inbetween
        // like this we need only one statement for the whole world
        // we concatenate the strings built bove with string.join
        String sql = "INSERT INTO minecraft_log.lag_events (chunk_id, tps) VALUES " + String.join(",", ChunkIDs);
        long check = executeSQLUpdate(sql, true);
        if (check == -1) {
            return false;
        }
        return true;
    }

    // function to reset the DB
    public void resetChunkDB() throws SQLException {
        String sql1 = "TRUNCATE TABLE minecraft_log.lag_chunks";
        executeSQLUpdate(sql1, false);

        String sql2 = "TRUNCATE TABLE minecraft_log.lag_events";
        executeSQLUpdate(sql2, false);
    }

    // abstraction to write SQL data to the DB
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
            System.out.println("DB Error in executeSQLUpdate: " + S);
            return -1;
        }
        return 0;
    }
}