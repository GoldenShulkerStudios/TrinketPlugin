package me.ewahv1.plugin.Listeners.Items;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.*;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BagOfTrinketsListener implements Listener {

    private final JavaPlugin plugin;

    public BagOfTrinketsListener(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        String playerName = event.getPlayer().getName();
        List<BagOfTrinkets> bags = loadAllBags();
        boolean playerExists = false;
        for (BagOfTrinkets bag : bags) {
            if (bag.getNombre().equals(playerName)) {
                playerExists = true;
                break;
            }
        }
        if (!playerExists) {
            BagOfTrinkets newBag = new BagOfTrinkets(playerName, new HashMap<>());
            bags.add(newBag);
            saveAllBags(bags);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Inventory inventory = event.getInventory();
        if (event.getView().getTitle().equalsIgnoreCase("Bolsa de Trinkets")) {
            ItemStack item = event.getCurrentItem();
            if (item != null && item.getType() != Material.WARPED_FUNGUS_ON_A_STICK) {
                event.setCancelled(true);
                event.getWhoClicked().sendMessage(
                        "Solo puedes colocar items tipo warped fungus on a stick en la bolsa de trinkets.");
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        Inventory inventory = event.getInventory();
        if (event.getView().getTitle().equalsIgnoreCase("Bolsa de Trinkets")) {
            String playerName = event.getPlayer().getName();
            Map<Integer, String> trinkets = new HashMap<>();

            for (int i = 0; i < inventory.getSize(); i++) {
                ItemStack item = inventory.getItem(i);
                if (item != null) {
                    trinkets.put(i + 1, itemToBase64(item));
                }
            }

            BagOfTrinkets newBag = new BagOfTrinkets(playerName, trinkets);
            List<BagOfTrinkets> bags = loadAllBags();
            boolean updated = false;
            for (int i = 0; i < bags.size(); i++) {
                if (bags.get(i).getNombre().equals(playerName)) {
                    bags.set(i, newBag);
                    updated = true;
                    break;
                }
            }
            if (!updated) {
                bags.add(newBag);
            }
            saveAllBags(bags);
        }
    }

    private List<BagOfTrinkets> loadAllBags() {
        Gson gson = new Gson();
        File file = new File(plugin.getDataFolder(), "BagsOfTrinkets.json");
        if (file.exists()) {
            try (FileReader reader = new FileReader(file)) {
                BagOfTrinkets[] bagsArray = gson.fromJson(reader, BagOfTrinkets[].class);
                List<BagOfTrinkets> bags = new ArrayList<>();
                if (bagsArray != null) {
                    for (BagOfTrinkets bag : bagsArray) {
                        bags.add(bag);
                    }
                }
                return bags;
            } catch (IOException | com.google.gson.JsonSyntaxException e) {
                e.printStackTrace();
            }
        }
        return new ArrayList<>();
    }

    private void saveAllBags(List<BagOfTrinkets> bags) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        File file = new File(plugin.getDataFolder(), "BagsOfTrinkets.json");

        try (FileWriter writer = new FileWriter(file)) {
            writer.write(gson.toJson(bags));
            plugin.getLogger().info("BagsOfTrinkets.json has been updated.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String itemToBase64(ItemStack item) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);
            dataOutput.writeObject(item);
            dataOutput.close();
            return Base64.getEncoder().encodeToString(outputStream.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
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
