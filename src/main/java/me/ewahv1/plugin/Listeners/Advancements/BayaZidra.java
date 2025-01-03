package me.ewahv1.plugin.Listeners.Advancements;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import me.ewahv1.plugin.Utils.GenerarUserBolsaData;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

import java.io.File;
import java.util.List;
import java.util.Map;

public class BayaZidra implements Listener {

  private final JavaPlugin plugin;

  public BayaZidra(JavaPlugin plugin) {
    this.plugin = plugin;
    Bukkit.getPluginManager().registerEvents(this, plugin);
    plugin.getLogger().info(ChatColor.GREEN + "[DEBUG] Listener de Baya Zidra registrado correctamente.");
  }

  @EventHandler
  public void onPlayerDamage(EntityDamageEvent event) {
    if (!(event.getEntity() instanceof Player)) {
      return;
    }

    Player player = (Player) event.getEntity();
    double health = player.getHealth();
    double maxHealth = player.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getDefaultValue();

    // Verificar si la vida del jugador está por debajo de 3 corazones (6 puntos de
    // vida)
    if (health >= 6) {
      return;
    }

    String playerUUID = player.getUniqueId().toString();
    Inventory bolsa = Bukkit.createInventory(null, 9, ChatColor.GOLD + "Bolsa de Trinkets");
    GenerarUserBolsaData.loadPlayerBolsa(playerUUID, bolsa);

    // Cargar datos de trinkets.yml
    File trinketsFile = new File(plugin.getDataFolder(), "trinkets.yml");
    if (!trinketsFile.exists()) {
      plugin.getLogger().severe("[ERROR] El archivo trinkets.yml no existe.");
      return;
    }

    FileConfiguration config = YamlConfiguration.loadConfiguration(trinketsFile);
    Map<String, Object> bayaZidraData = config.getConfigurationSection("Trinkets.Advancements.BayaZidra")
        .getValues(false);

    if (bayaZidraData == null) {
      plugin.getLogger().severe("[ERROR] No se encontró la configuración de Baya Zidra en trinkets.yml.");
      return;
    }

    boolean consumable = (int) bayaZidraData.getOrDefault("consumable", 0) > 0;

    // Buscar la Baya Zidra en la bolsa
    for (int i = 0; i < bolsa.getSize(); i++) {
      ItemStack item = bolsa.getItem(i);
      if (item != null && item.hasItemMeta() &&
          item.getItemMeta().getDisplayName().equals(ChatColor.translateAlternateColorCodes('&', "&2BayaZidra"))) {
        // Curar al jugador 2.5 corazones (5 puntos de vida)
        double healAmount = Math.min(health + 5, maxHealth);
        player.setHealth(healAmount);

        // Enviar el mensaje en el ActionBar
        sendActionBar(player, ChatColor.GREEN + "La Baya Zidra te ha curado 2.5 corazones!");

        plugin.getLogger().info("[DEBUG] El jugador " + player.getName() + " fue curado por la Baya Zidra.");

        // Eliminar la Baya Zidra si es consumible
        if (consumable) {
          bolsa.setItem(i, null); // Eliminar el ítem de la bolsa
          GenerarUserBolsaData.savePlayerBolsa(playerUUID, bolsa); // Guardar la bolsa sin el ítem
          plugin.getLogger().info("[DEBUG] La Baya Zidra fue consumida y eliminada.");
        }
        break;
      }
    }
  }

  private void sendActionBar(Player player, String message) {
    player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(message));
  }
}
