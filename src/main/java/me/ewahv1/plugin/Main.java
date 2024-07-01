package me.ewahv1.plugin;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.ewahv1.plugin.Database.DatabaseConnection;
import me.ewahv1.plugin.Commands.TrinketBagCommand;
import me.ewahv1.plugin.Database.DatabaseConfig;
import me.ewahv1.plugin.Listeners.Items.TrinketBag.BagOfTrinkets;
import me.ewahv1.plugin.Listeners.Trinkets.*;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class Main extends JavaPlugin {

    private static Main instance;
    private DatabaseConnection databaseConnection;

    @Override
    public void onEnable() {
        instance = this;
        File configFile = new File(getDataFolder(), "db_config.json");
        DatabaseConfig config = loadOrCreateConfig(configFile);
        databaseConnection = new DatabaseConnection(config.getUrl(), config.getUsername(), config.getPassword());

        // Crear la instancia de BagOfTrinkets
        BagOfTrinkets bagOfTrinkets = new BagOfTrinkets(this);

        // Registrar eventos
        getServer().getPluginManager().registerEvents(bagOfTrinkets, this);
        getServer().getPluginManager().registerEvents(new DescalcificadorListener(this, databaseConnection, bagOfTrinkets), this);
        getServer().getPluginManager().registerEvents(new MADListener(this, databaseConnection, bagOfTrinkets), this);
        getServer().getPluginManager().registerEvents(new WarmogListener(this, databaseConnection, bagOfTrinkets), this);
        getServer().getPluginManager().registerEvents(new ZombArmListener(this, databaseConnection, bagOfTrinkets), this);

        // Registrar comando
        getCommand("bolsa").setExecutor(new TrinketBagCommand(bagOfTrinkets));
    }

    @Override
    public void onDisable() {
        databaseConnection.close();
    }

    public static Main getInstance() {
        return instance;
    }

    public DatabaseConnection getDatabaseConnection() {
        return databaseConnection;
    }

    private DatabaseConfig loadOrCreateConfig(File file) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            DatabaseConfig defaultConfig = new DatabaseConfig();
            defaultConfig.setUrl("jdbc:mysql://localhost:3306/TrinketPlugindb");
            defaultConfig.setUsername("root");
            defaultConfig.setPassword("root");
            try (FileWriter writer = new FileWriter(file)) {
                gson.toJson(defaultConfig, writer);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return defaultConfig;
        } else {
            try (FileReader reader = new FileReader(file)) {
                return gson.fromJson(reader, DatabaseConfig.class);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
    }
}
