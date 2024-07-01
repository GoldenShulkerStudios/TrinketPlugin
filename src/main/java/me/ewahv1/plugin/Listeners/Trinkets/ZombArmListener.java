package me.ewahv1.plugin.Listeners.Trinkets;

import me.ewahv1.plugin.Main;
import me.ewahv1.plugin.Database.DatabaseConnection;
import me.ewahv1.plugin.Listeners.Items.TrinketBag.BagOfTrinkets;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.ByteArrayInputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Random;
import java.util.UUID;

public class ZombArmListener implements Listener {

    private final DatabaseConnection databaseConnection;
    private final JavaPlugin plugin;

    public ZombArmListener(Main plugin, DatabaseConnection databaseConnection, BagOfTrinkets bagOfTrinkets) {
        this.databaseConnection = ((Main) plugin).getDatabaseConnection();
        this.plugin = plugin;
    }

    @EventHandler
    public void onZombieDeath(EntityDeathEvent event) {
        if (event.getEntity() instanceof Zombie) {
            Zombie zombie = (Zombie) event.getEntity();
            if (zombie.getKiller() instanceof Player) {
                Random rand = new Random();
                int chance = rand.nextInt(100);
                if (chance < 90) {
                    int goldenChance = rand.nextInt(100);
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            try (Connection connection = databaseConnection.getConnection();
                                 Statement statement = connection.createStatement()) {
                                ItemStack warpedFungusStick;
                                if (goldenChance < 50) {
                                    warpedFungusStick = new ItemStack(Material.WARPED_FUNGUS_ON_A_STICK, 1);
                                    ItemMeta meta = warpedFungusStick.getItemMeta();
                                    meta.setDisplayName("§6§lBrazo putrefacto dorado");
                                    meta.setLore(Arrays.asList("§aTus ataques básicos realizan +2❤ a los zombies", "§6§lSlot: Mano secundaria"));
                                    meta.setCustomModelData(2);
                                    meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                                    meta.addEnchant(Enchantment.DURABILITY, 1, true);
                                    meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                                    warpedFungusStick.setItemMeta(meta);
                                    statement.executeUpdate("UPDATE tri_count_settings SET CountGold = CountGold + 1 WHERE ID = 4");
                                } else {
                                    warpedFungusStick = new ItemStack(Material.WARPED_FUNGUS_ON_A_STICK, 1);
                                    ItemMeta meta = warpedFungusStick.getItemMeta();
                                    meta.setDisplayName("§a§lBrazo putrefacto");
                                    meta.setLore(Arrays.asList("§aTus ataques básicos realizan +1❤ a los zombies", "§6§lSlot: Mano secundaria"));
                                    meta.setCustomModelData(1);
                                    meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                                    warpedFungusStick.setItemMeta(meta);
                                    statement.executeUpdate("UPDATE tri_count_settings SET CountNormal = CountNormal + 1 WHERE ID = 4");
                                }
                                new BukkitRunnable() {
                                    @Override
                                    public void run() {
                                        event.getDrops().add(warpedFungusStick);
                                    }
                                }.runTask(plugin);
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }
                        }
                    }.runTaskAsynchronously(plugin);
                }
            }
        }
    }

    @EventHandler
    public void onPlayerDamageZombie(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player && event.getEntity() instanceof Zombie) {
            Player player = (Player) event.getDamager();
            UUID playerUuid = player.getUniqueId();
            new BukkitRunnable() {
                @Override
                public void run() {
                    Inventory bag = getBag(playerUuid);

                    if (bag != null) {
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                for (ItemStack item : bag.getContents()) {
                                    if (item != null && item.getType() == Material.WARPED_FUNGUS_ON_A_STICK) {
                                        String itemName = item.getItemMeta().getDisplayName();
                                        if (itemName.equals("§a§lBrazo putrefacto")) {
                                            event.setDamage(event.getDamage() + 2); // 1 heart (2 health points)
                                        } else if (itemName.equals("§6§lBrazo putrefacto dorado")) {
                                            event.setDamage(event.getDamage() + 4); // 2 hearts (4 health points)
                                        }
                                    }
                                }
                            }
                        }.runTask(plugin);
                    }
                }
            }.runTaskAsynchronously(plugin);
        }
    }

    private Inventory getBag(UUID playerUuid) {
        try (Connection connection = databaseConnection.getConnection()) {
            PreparedStatement ps = connection.prepareStatement("SELECT inventory FROM player_inventories WHERE player_uuid = ?");
            ps.setString(1, playerUuid.toString());
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String inventoryString = rs.getString("inventory");
                return deserializeInventory(inventoryString);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    private Inventory deserializeInventory(String inventoryString) {
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(inventoryString));
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
            Inventory inventory = org.bukkit.Bukkit.getServer().createInventory(null, dataInput.readInt());

            for (int i = 0; i < inventory.getSize(); i++) {
                inventory.setItem(i, (ItemStack) dataInput.readObject());
            }

            dataInput.close();
            return inventory;
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo cargar el inventario.", e);
        }
    }
}
