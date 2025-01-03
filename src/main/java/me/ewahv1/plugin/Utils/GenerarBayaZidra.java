package me.ewahv1.plugin.Utils;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class GenerarBayaZidra implements Listener {

    private final JavaPlugin plugin;
    private final UtilsYML trinketUnlocks;

    public GenerarBayaZidra(JavaPlugin plugin) {
        this.plugin = plugin;

        // Inicializa el archivo para almacenar desbloqueos
        this.trinketUnlocks = new UtilsYML(plugin.getDataFolder(), "trinket_unlocks.yml");

        Bukkit.getPluginManager().registerEvents(this, plugin);
        Bukkit.getLogger().info("\u00a7b[DEBUG] GenerarBayaZidra listener registrado correctamente.");
    }

    @EventHandler
    public void onPlayerAdvancementDone(PlayerAdvancementDoneEvent event) {
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();
        NamespacedKey advancementKey = event.getAdvancement().getKey();

        Bukkit.getLogger().info("§b[DEBUG] Evento detectado para el jugador: " + player.getName());
        Bukkit.getLogger().info("§b[DEBUG] Clave del avance: " + advancementKey);

        // Asegúrate de que la clave coincide exactamente o ignora el espacio de nombres
        if (advancementKey.toString().endsWith("baya_zidra")) { // Revisa el final de la clave
            if (!trinketUnlocks.hasUnlockedTrinket(playerUUID, "BayaZidra")) {
                Bukkit.getLogger().info("§a[DEBUG] Avance reconocido: " + advancementKey);
                trinketUnlocks.unlockTrinket(playerUUID, "BayaZidra");
                awardBayaZidraAchievement(player);
            } else {
                Bukkit.getLogger().info("§e[DEBUG] El jugador ya recibió el logro: " + player.getName());
            }
        } else {
            Bukkit.getLogger().info("§e[DEBUG] Avance no relevante para Baya Zidra: " + advancementKey);
        }
    }

    private void awardBayaZidraAchievement(Player player) {
        Bukkit.getLogger().info("§b[DEBUG] Comenzando el proceso de otorgar Baya Zidra.");

        File trinketsFile = new File(plugin.getDataFolder(), "trinkets.yml");
        if (!trinketsFile.exists()) {
            Bukkit.getLogger().severe("§c[DEBUG] El archivo trinkets.yml no se encontró.");
            return;
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(trinketsFile);

        String name = translateColorCodes(
                config.getString("Trinkets.Advancements.BayaZidra.name", "§f§lBaya Zidra"));
        List<String> lore = translateLore(config.getStringList("Trinkets.Advancements.BayaZidra.lore"));
        int customModelData = config.getInt("Trinkets.Advancements.BayaZidra.customModelData", 0);

        Bukkit.getLogger().info("§b[DEBUG] Datos cargados: Nombre: " + name + ", Modelo: " + customModelData);

        // Crear el item
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(lore);
            meta.setCustomModelData(customModelData);
            item.setItemMeta(meta);
        }

        // Dar el trinket al jugador
        // Dar el trinket al jugador
        if (player.getInventory().addItem(item).isEmpty()) {
            Bukkit.getLogger().info("§a[DEBUG] Trinket Baya Zidra añadido al inventario de " + player.getName());
        } else {
            Bukkit.getLogger().warning(
                    "§c[DEBUG] El inventario de " + player.getName() + " está lleno, el trinket no fue añadido.");
        }
        player.sendMessage("§a§lFelicidades! Has desbloqueado el logro Baya Zidra.");
        Bukkit.getLogger().info("§a[DEBUG] Trinket Baya Zidra otorgado correctamente a " + player.getName());
    }

    // Método para traducir códigos de color en cadenas individuales
    private String translateColorCodes(String input) {
        if (input == null)
            return "";
        return input.replace("&", "\u00a7");
    }

    // Método para traducir códigos de color en listas de cadenas
    private List<String> translateLore(List<String> lore) {
        List<String> translatedLore = new ArrayList<>();
        for (String line : lore) {
            translatedLore.add(translateColorCodes(line));
        }
        return translatedLore;
    }
}
