package me.ewahv1.plugin.Utils;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GenerarUserBolsaData {

    private static final File DATA_FILE = new File(Bukkit.getPluginManager().getPlugin("TrinketsX").getDataFolder(),
            "BolsaDeTrinkets.yml");

    public static boolean doesPlayerBolsaExist(String uuid) {
        if (!DATA_FILE.exists()) {
            return false;
        }
        FileConfiguration config = YamlConfiguration.loadConfiguration(DATA_FILE);
        return config.isSet("Bolsas." + uuid);
    }

    public static void createPlayerBolsa(String uuid) {
        if (!DATA_FILE.exists()) {
            try {
                DATA_FILE.getParentFile().mkdirs();
                DATA_FILE.createNewFile();
            } catch (IOException e) {
                Bukkit.getLogger().severe("No se pudo crear el archivo BolsaDeTrinkets.yml");
                e.printStackTrace();
                return;
            }
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(DATA_FILE);
        config.createSection("Bolsas." + uuid + ".trinkets");

        try {
            config.save(DATA_FILE);
        } catch (IOException e) {
            Bukkit.getLogger().severe("Error al guardar el archivo BolsaDeTrinkets.yml para " + uuid);
            e.printStackTrace();
        }
    }

    public static void loadPlayerBolsa(String uuid, Inventory inventory) {
        if (!DATA_FILE.exists()) {
            return;
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(DATA_FILE);
        List<Map<String, Object>> trinkets = (List<Map<String, Object>>) config.getList("Bolsas." + uuid + ".trinkets");

        if (trinkets != null) {
            for (Map<String, Object> trinketData : trinkets) {
                int slot = (int) trinketData.get("slot");
                ItemStack item = ItemStack.deserialize((Map<String, Object>) trinketData.get("item"));
                inventory.setItem(slot, item);
            }
        }
    }

    public static void savePlayerBolsa(String uuid, Inventory inventory) {
        if (!DATA_FILE.exists()) {
            return;
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(DATA_FILE);
        List<Map<String, Object>> trinkets = new ArrayList<>();

        for (int i = 0; i < inventory.getSize(); i++) {
            ItemStack item = inventory.getItem(i);
            if (item != null) {
                Map<String, Object> trinketData = new HashMap<>();
                trinketData.put("slot", i);
                trinketData.put("item", item.serialize());
                trinkets.add(trinketData);
            }
        }

        config.set("Bolsas." + uuid + ".trinkets", trinkets);

        try {
            config.save(DATA_FILE);
            Bukkit.getLogger().info("Bolsa guardada para " + uuid);
        } catch (IOException e) {
            Bukkit.getLogger().severe("Error al guardar el archivo BolsaDeTrinkets.yml para " + uuid);
            e.printStackTrace();
        }
    }
}
