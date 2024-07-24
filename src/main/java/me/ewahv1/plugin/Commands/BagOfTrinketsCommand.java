package me.ewahv1.plugin.Commands;

import com.google.gson.Gson;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.io.BukkitObjectInputStream;

import java.io.*;
import java.util.Base64;
import java.util.Map;

public class BagOfTrinketsCommand implements CommandExecutor {

    private final JavaPlugin plugin;

    public BagOfTrinketsCommand(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            loadConfig(player);
        } else {
            sender.sendMessage("Este comando solo puede ser ejecutado por un jugador.");
        }
        return true;
    }

    private void loadConfig(Player player) {
        Gson gson = new Gson();
        File configFile = new File(plugin.getDataFolder(), "BagOfTrinketsConfig.json");
        if (configFile.exists()) {
            try (FileReader reader = new FileReader(configFile)) {
                BagOfTrinketsConfig config = gson.fromJson(reader, BagOfTrinketsConfig.class);
                int slots = config.getSlots();
                String title = ChatColor.translateAlternateColorCodes('&', config.getNombre());

                File file = new File(plugin.getDataFolder(), "BagsOfTrinkets.json");
                if (file.exists()) {
                    try (FileReader bagsReader = new FileReader(file)) {
                        BagOfTrinkets[] bagsArray = gson.fromJson(bagsReader, BagOfTrinkets[].class);
                        if (bagsArray != null) {
                            for (BagOfTrinkets bag : bagsArray) {
                                if (bag.getNombre().equals(player.getName())) {
                                    Inventory inventory = Bukkit.createInventory(player, slots, title);
                                    for (Map.Entry<Integer, String> entry : bag.getInventario().entrySet()) {
                                        ItemStack item = itemFromBase64(entry.getValue());
                                        inventory.setItem(entry.getKey() - 1, item);
                                    }
                                    player.openInventory(inventory);
                                    player.sendMessage(ChatColor.GREEN + "Se ha abierto la bolsa de trinkets.");
                                    return;
                                }
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                createNewInventory(player, slots, title);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            createNewInventory(player, 54, "Bolsa de Trinkets");
        }
    }

    private void createNewInventory(Player player, int slots, String title) {
        Inventory inventory = Bukkit.createInventory(player, slots, title);
        player.openInventory(inventory);
        player.sendMessage(ChatColor.GREEN + "Se ha abierto una nueva bolsa de trinkets.");
    }

    private ItemStack itemFromBase64(String data) {
        try {
            byte[] itemData = Base64.getDecoder().decode(data);
            ByteArrayInputStream inputStream = new ByteArrayInputStream(itemData);
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
            return (ItemStack) dataInput.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static class BagOfTrinketsConfig {
        private String nombre;
        private int slots;

        public String getNombre() {
            return nombre;
        }

        public int getSlots() {
            return slots;
        }
    }

    private static class BagOfTrinkets {
        private String nombre;
        private Map<Integer, String> inventario;

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
