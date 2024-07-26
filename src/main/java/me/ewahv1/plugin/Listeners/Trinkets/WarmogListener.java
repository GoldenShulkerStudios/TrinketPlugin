package me.ewahv1.plugin.Listeners.Trinkets;

import com.google.gson.Gson;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.io.BukkitObjectInputStream;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.Base64;

public class WarmogListener implements Listener {

    private final JavaPlugin plugin;
    private final Map<UUID, AttributeModifier> healthModifiers = new HashMap<>();
    private final TrinketDropManager dropManager;

    public WarmogListener(JavaPlugin plugin) {
        this.plugin = plugin;
        this.dropManager = new TrinketDropManager(plugin);
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntity().getType() == EntityType.RAVAGER) {
            dropManager.handleEntityDeath(event, "Ravager", "Warmog", Material.WARPED_FUNGUS_ON_A_STICK);
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        resetPlayerHealth(event.getPlayer());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        resetPlayerHealth(event.getPlayer());
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();
        if ("Bolsa de Trinkets".equals(event.getView().getTitle())) {
            updatePlayerHealth(player);
        }
    }

    private void updatePlayerHealth(Player player) {
        Inventory trinketBag = getTrinketBag(player);
        if (trinketBag == null) {
            resetPlayerHealth(player);
            return;
        }

        double additionalHealth = 0;

        for (ItemStack item : trinketBag.getContents()) {
            if (item != null && item.getType() == Material.WARPED_FUNGUS_ON_A_STICK) {
                ItemMeta meta = item.getItemMeta();
                if (meta != null) {
                    String displayName = meta.getDisplayName();
                    if (displayName.equals("Warmog")) {
                        additionalHealth = 8;
                        break;
                    } else if (displayName.equals("§6§lWarmog Dorado")) {
                        additionalHealth = 16;
                        break;
                    }
                }
            }
        }

        applyHealthModifier(player, additionalHealth);
    }

    private void applyHealthModifier(Player player, double additionalHealth) {
        UUID playerUUID = player.getUniqueId();
        AttributeInstance healthAttribute = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);

        if (healthModifiers.containsKey(playerUUID)) {
            healthAttribute.removeModifier(healthModifiers.get(playerUUID));
            healthModifiers.remove(playerUUID);
        }

        if (additionalHealth > 0) {
            AttributeModifier modifier = new AttributeModifier(UUID.randomUUID(), "trinket_health", additionalHealth,
                    AttributeModifier.Operation.ADD_NUMBER);
            healthAttribute.addModifier(modifier);
            healthModifiers.put(playerUUID, modifier);
        }
    }

    private void resetPlayerHealth(Player player) {
        UUID playerUUID = player.getUniqueId();
        AttributeInstance healthAttribute = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);

        if (healthModifiers.containsKey(playerUUID)) {
            healthAttribute.removeModifier(healthModifiers.get(playerUUID));
            healthModifiers.remove(playerUUID);
        }

        healthAttribute.setBaseValue(20.0);
    }

    private Inventory getTrinketBag(Player player) {
        File file = new File(plugin.getDataFolder(), "BagsOfTrinkets.json");
        if (!file.exists()) {
            return null;
        }

        Gson gson = new Gson();
        try (FileReader reader = new FileReader(file)) {
            BagOfTrinkets[] bagsArray = gson.fromJson(reader, BagOfTrinkets[].class);
            for (BagOfTrinkets bag : bagsArray) {
                if (bag.getNombre().equals(player.getName())) {
                    return deserializeInventory(bag.getInventario());
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        return null;
    }

    private Inventory deserializeInventory(Map<Integer, String> serializedInventory)
            throws IOException, ClassNotFoundException {
        Inventory inventory = plugin.getServer().createInventory(null, 54, "Bolsa de Trinkets");
        for (Map.Entry<Integer, String> entry : serializedInventory.entrySet()) {
            byte[] data = Base64.getDecoder().decode(entry.getValue());
            try (BukkitObjectInputStream dataInput = new BukkitObjectInputStream(new ByteArrayInputStream(data))) {
                ItemStack item = (ItemStack) dataInput.readObject();
                inventory.setItem(entry.getKey() - 1, item);
            }
        }
        return inventory;
    }

    private static class BagOfTrinkets {
        private final String nombre;
        private final Map<Integer, String> inventario;

        public BagOfTrinkets(String nombre, Map<Integer, String> inventario) {
            this.nombre = nombre;
            this.inventario = inventario;
        }

        public String getNombre() {
            return nombre;
        }

        public Map<Integer, String> getInventario() {
            return inventario;
        }
    }
}
