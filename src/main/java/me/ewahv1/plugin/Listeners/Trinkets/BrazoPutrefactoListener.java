package me.ewahv1.plugin.Listeners.Trinkets;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.io.BukkitObjectInputStream;

import com.google.gson.Gson;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.util.Map;
import java.util.Base64;

public class BrazoPutrefactoListener implements Listener {

    private final JavaPlugin plugin;
    private final TrinketDropManager trinketDropManager;

    public BrazoPutrefactoListener(JavaPlugin plugin) {
        this.plugin = plugin;
        this.trinketDropManager = new TrinketDropManager(plugin);
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntity().getType() == EntityType.ZOMBIE) {
            trinketDropManager.handleEntityDeath(event, "Zombie", "BrazoPutrefacto", Material.WARPED_FUNGUS_ON_A_STICK);
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (event.getEntity().getType() == EntityType.ZOMBIE && event.getDamager() instanceof Player) {
            Player player = (Player) event.getDamager();
            Inventory trinketBag = getTrinketBag(player);
            if (trinketBag == null) {
                return;
            }

            double additionalDamage = 0;

            for (ItemStack item : trinketBag.getContents()) {
                if (item != null && item.getType() == Material.WARPED_FUNGUS_ON_A_STICK) {
                    ItemMeta meta = item.getItemMeta();
                    if (meta != null) {
                        String displayName = meta.getDisplayName();
                        if (displayName.equals("Brazo Putrefacto")) {
                            additionalDamage = 1;
                            break;
                        } else if (displayName.equals("§6§lBrazo Putrefacto Dorado")) {
                            additionalDamage = 2;
                            break;
                        }
                    }
                }
            }

            if (additionalDamage > 0) {
                event.setDamage(event.getDamage() + additionalDamage);
            }
        }
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
