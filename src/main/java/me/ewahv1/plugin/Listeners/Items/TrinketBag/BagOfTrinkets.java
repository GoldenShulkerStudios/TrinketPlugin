package me.ewahv1.plugin.Listeners.Items.TrinketBag;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.UUID;

import me.ewahv1.plugin.Main;
import me.ewahv1.plugin.Database.DatabaseConnection;

public class BagOfTrinkets implements Listener {
    private static final String INVENTORY_TITLE = "Bolsa de Trinkets";
    private final JavaPlugin plugin;
    private final DatabaseConnection databaseConnection;
    private final HashMap<UUID, Inventory> playerInventories = new HashMap<>();

    public BagOfTrinkets(JavaPlugin plugin) {
        this.plugin = plugin;
        this.databaseConnection = ((Main) plugin).getDatabaseConnection();

        // Agregar una tarea periódica para guardar los inventarios de los jugadores
        new BukkitRunnable() {
            @Override
            public void run() {
                saveAllInventories();
            }
        }.runTaskTimerAsynchronously(plugin, 1200L, 1200L); // Guardar cada minuto (1200 ticks)
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID playerUuid = player.getUniqueId();
        String playerName = player.getName();

        new BukkitRunnable() {
            @Override
            public void run() {
                Inventory inventory = loadInventoryFromDatabase(playerUuid);
                if (inventory == null) {
                    inventory = Bukkit.createInventory(null, 9, INVENTORY_TITLE);
                    saveInventoryToDatabase(playerUuid, playerName, inventory);
                }
                playerInventories.put(playerUuid, inventory);
                Bukkit.getLogger().info("Inventario cargado en memoria para el jugador: " + playerUuid);
            }
        }.runTaskAsynchronously(plugin);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID playerUuid = player.getUniqueId();
        String playerName = player.getName();

        Inventory trinketInventory = playerInventories.get(playerUuid);
        if (trinketInventory != null) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    saveInventoryToDatabase(playerUuid, playerName, trinketInventory);
                    playerInventories.remove(playerUuid);
                    Bukkit.getLogger().info("Inventario guardado y removido de la memoria para el jugador: " + playerUuid);
                }
            }.runTaskAsynchronously(plugin);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getPlayer() instanceof Player) {
            Player player = (Player) event.getPlayer();
            UUID playerUuid = player.getUniqueId();
            String playerName = player.getName();

            if (event.getView().getTitle().equals(INVENTORY_TITLE)) {
                Inventory bag = event.getInventory();
                playerInventories.put(playerUuid, bag);
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        saveInventoryToDatabase(playerUuid, playerName, bag);
                        Bukkit.getLogger().info("Inventario guardado al cerrar la bolsa de trinkets para el jugador: " + playerUuid);
                    }
                }.runTaskAsynchronously(plugin);
            }
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getView().getTitle().equals(INVENTORY_TITLE)) {
            Player player = (Player) event.getWhoClicked();
            UUID playerUuid = player.getUniqueId();
            String playerName = player.getName();
            updateAndSaveInventory(playerUuid, playerName, event.getInventory());
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getView().getTitle().equals(INVENTORY_TITLE)) {
            Player player = (Player) event.getWhoClicked();
            UUID playerUuid = player.getUniqueId();
            String playerName = player.getName();
            updateAndSaveInventory(playerUuid, playerName, event.getInventory());
        }
    }

    private void updateAndSaveInventory(UUID playerUuid, String playerName, Inventory inventory) {
        playerInventories.put(playerUuid, inventory);
        new BukkitRunnable() {
            @Override
            public void run() {
                saveInventoryToDatabase(playerUuid, playerName, inventory);
                Bukkit.getLogger().info("Inventario actualizado y guardado para el jugador: " + playerUuid);
            }
        }.runTaskAsynchronously(plugin);
    }

    private String serializeInventory(Inventory inventory) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream)) {
            dataOutput.writeInt(inventory.getSize());

            for (int i = 0; i < inventory.getSize(); i++) {
                dataOutput.writeObject(inventory.getItem(i));
            }

            return Base64Coder.encodeLines(outputStream.toByteArray());
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo guardar el inventario.", e);
        }
    }

    private Inventory deserializeInventory(String inventoryString) {
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(inventoryString));
             BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream)) {
            Inventory inventory = Bukkit.getServer().createInventory(null, dataInput.readInt());

            for (int i = 0; i < inventory.getSize(); i++) {
                inventory.setItem(i, (ItemStack) dataInput.readObject());
            }

            return inventory;
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo cargar el inventario.", e);
        }
    }

    public void openTrinketBag(Player player) {
        UUID playerUuid = player.getUniqueId();
        String playerName = player.getName();

        new BukkitRunnable() {
            @Override
            public void run() {
                Inventory inventory = playerInventories.get(playerUuid);
                if (inventory == null) {
                    inventory = loadInventoryFromDatabase(playerUuid);
                    if (inventory == null) {
                        inventory = Bukkit.createInventory(null, 9, INVENTORY_TITLE);
                    }
                    playerInventories.put(playerUuid, inventory);
                }
                
                // Guardar inventario en la base de datos
                saveInventoryToDatabase(playerUuid, playerName, inventory);
                
                final Inventory finalInventory = inventory;
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        player.openInventory(finalInventory);
                    }
                }.runTask(plugin);
            }
        }.runTaskAsynchronously(plugin);
    }

    private void saveInventoryToDatabase(UUID playerUuid, String playerName, Inventory inventory) {
        try (Connection connection = databaseConnection.getConnection()) {
            PreparedStatement ps = connection.prepareStatement("INSERT INTO player_inventories (player_uuid, player_name, inventory) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE player_name = VALUES(player_name), inventory = VALUES(inventory)");
            ps.setString(1, playerUuid.toString());
            ps.setString(2, playerName);
            ps.setString(3, serializeInventory(inventory));
            ps.executeUpdate();
            Bukkit.getLogger().info("Inventario guardado para el jugador: " + playerUuid);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Inventory loadInventoryFromDatabase(UUID playerUuid) {
        try (Connection connection = databaseConnection.getConnection()) {
            PreparedStatement ps = connection.prepareStatement("SELECT inventory FROM player_inventories WHERE player_uuid = ?");
            ps.setString(1, playerUuid.toString());
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String inventoryString = rs.getString("inventory");
                Inventory inventory = deserializeInventory(inventoryString);
                Bukkit.getLogger().info("Inventario cargado para el jugador: " + playerUuid);
                return inventory;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        Bukkit.getLogger().info("No se encontró inventario en la base de datos para el jugador: " + playerUuid);
        return null;
    }

    private void saveAllInventories() {
        for (UUID playerUuid : playerInventories.keySet()) {
            Inventory inventory = playerInventories.get(playerUuid);
            if (inventory != null) {
                saveInventoryToDatabase(playerUuid, Bukkit.getPlayer(playerUuid).getName(), inventory);
            }
        }
        Bukkit.getLogger().info("Inventarios de todos los jugadores guardados periódicamente.");
    }
}
