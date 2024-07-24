package me.ewahv1.plugin.Listeners.Trinkets;

import com.google.gson.Gson;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.io.BukkitObjectInputStream;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Base64;
import java.util.UUID;

public class MochilaAntigravedadDefectuosaListener implements Listener {

    private final JavaPlugin plugin;
    private final Random rand = new Random();
    private final Map<UUID, Long> cooldowns = new HashMap<>();
    private final TrinketDropManager dropManager;

    public MochilaAntigravedadDefectuosaListener(JavaPlugin plugin) {
        this.plugin = plugin;
        this.dropManager = new TrinketDropManager(plugin);
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntity().getType() == EntityType.PHANTOM) {
            dropManager.handleEntityDeath(event, "Phantom", "MAD", Material.WARPED_FUNGUS_ON_A_STICK);
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Inventory trinketBag = getTrinketBag(player);
        if (trinketBag == null) {
            return;
        }

        UUID playerUUID = player.getUniqueId();
        long currentTime = System.currentTimeMillis();

        if (cooldowns.containsKey(playerUUID) && (currentTime - cooldowns.get(playerUUID)) < 10000) {
            return;
        }

        boolean hasTrinket = false;
        for (ItemStack item : trinketBag.getContents()) {
            if (item != null && item.getType() == Material.WARPED_FUNGUS_ON_A_STICK) {
                ItemMeta meta = item.getItemMeta();
                if (meta != null) {
                    String displayName = meta.getDisplayName();
                    if (displayName.equals("Mochila Antigravedad Defectuosa")
                            || displayName.equals("§6§lMochila Antigravedad Defectuosa Dorado")) {
                        hasTrinket = true;
                        applyRandomFallingEffect(player, displayName);
                        cooldowns.put(playerUUID, currentTime);
                        break;
                    }
                }
            }
        }
        if (!hasTrinket) {
            cooldowns.remove(playerUUID);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();
        if ("Bolsa de Trinkets".equals(event.getView().getTitle())) {
            UUID playerUUID = player.getUniqueId();
            cooldowns.remove(playerUUID);
        }
    }

    private void applyRandomFallingEffect(Player player, String displayName) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (displayName.equals("§6§lMochila Antigravedad Defectuosa Dorado")) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 120, 1)); // 6 seconds of
                                                                                                     // Slow Falling II
                } else {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 60, 0)); // 3 seconds of Slow
                                                                                                    // Falling I
                }
            }
        }.runTaskLater(plugin, rand.nextInt(6000) + 2000); // Random delay between 2 to 8 seconds
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
                inventory.setItem(entry.getKey() - 1, item); // Convertir de 1-based a 0-based
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
