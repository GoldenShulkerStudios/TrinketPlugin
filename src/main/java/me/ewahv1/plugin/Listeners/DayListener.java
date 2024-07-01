package me.ewahv1.plugin.Listeners;

import me.ewahv1.plugin.Database.DatabaseConnection;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DayListener {
    private int currentDay;
    private final DatabaseConnection databaseConnection;
    private final JavaPlugin plugin;

    public DayListener(DatabaseConnection databaseConnection, JavaPlugin plugin) {
        this.databaseConnection = databaseConnection;
        this.plugin = plugin;
    }

    public void getCurrentDayAsync(Callback callback) {
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    PreparedStatement ps = databaseConnection.getConnection().prepareStatement("SELECT `Dia Actual` FROM settings WHERE ID = 1");
                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) {
                        currentDay = rs.getInt("Dia Actual");
                    }
                    rs.close();
                    ps.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        callback.onQueryDone(currentDay);
                    }
                }.runTask(plugin);
            }
        }.runTaskAsynchronously(plugin);
    }

    public interface Callback {
        void onQueryDone(int currentDay);
    }
}
