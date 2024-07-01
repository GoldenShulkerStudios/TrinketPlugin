package me.ewahv1.plugin.Listeners.Trinkets;

import me.ewahv1.plugin.Main;
import me.ewahv1.plugin.Database.DatabaseConnection;
import me.ewahv1.plugin.Listeners.Items.TrinketBag.BagOfTrinkets;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Random;
import java.util.UUID;

public class DescalcificadorListener implements Listener {
    private final Main plugin;
    private final DatabaseConnection databaseConnection;
    private final BagOfTrinkets bagOfTrinkets;

    public DescalcificadorListener(Main plugin, DatabaseConnection databaseConnection, BagOfTrinkets bagOfTrinkets) {
        this.plugin = plugin;
        this.databaseConnection = databaseConnection;
        this.bagOfTrinkets = bagOfTrinkets;
    }

    @EventHandler
    public void onSkeletonDeath(EntityDeathEvent event) {
        if (event.getEntity() instanceof Skeleton) {
            Skeleton skeleton = (Skeleton) event.getEntity();
            if (skeleton.getKiller() instanceof Player) {
                Random rand = new Random();
                int chance = rand.nextInt(100);
                if (chance < 90) {
                    int goldenChance = rand.nextInt(100);
                    ItemStack warpedFungusStick;
                    if (goldenChance < 50) {
                        warpedFungusStick = createTrinket("§6§lDescalcificador dorado", "§aTus ataques básicos realizan +2❤ a los Esqueletos", 4, Enchantment.DURABILITY, 1);
                        updateDatabaseAsync("UPDATE tri_count_settings SET CountGold = CountGold + 1 WHERE ID = 1");
                    } else {
                        warpedFungusStick = createTrinket("§a§lDescalcificador", "§aTus ataques básicos realizan +1❤ a los Esqueletos", 3, null, 0);
                        updateDatabaseAsync("UPDATE tri_count_settings SET CountNormal = CountNormal + 1 WHERE ID = 1");
                    }
                    event.getDrops().add(warpedFungusStick);
                }
            }
        }
    }

    private ItemStack createTrinket(String displayName, String lore, int customModelData, Enchantment enchantment, int enchantmentLevel) {
        ItemStack itemStack = new ItemStack(Material.WARPED_FUNGUS_ON_A_STICK, 1);
        ItemMeta meta = itemStack.getItemMeta();
        meta.setDisplayName(displayName);
        meta.setLore(Arrays.asList(lore, "§6§lSlot: B.O.T"));
        meta.setCustomModelData(customModelData);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        if (enchantment != null) {
            meta.addEnchant(enchantment, enchantmentLevel, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }
        itemStack.setItemMeta(meta);
        return itemStack;
    }

    private void updateDatabaseAsync(String query) {
        new BukkitRunnable() {
            @Override
            public void run() {
                try (Connection connection = databaseConnection.getConnection();
                     Statement statement = connection.createStatement()) {
                    statement.executeUpdate(query);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }.runTaskAsynchronously(plugin);
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Skeleton && event.getDamager() instanceof Player) {
            Player player = (Player) event.getDamager();
            UUID playerUuid = player.getUniqueId();

            new BukkitRunnable() {
                @Override
                public void run() {
                    Inventory bag = bagOfTrinkets.loadInventoryFromDatabase(playerUuid);
                    if (bag == null) {
                        Bukkit.getLogger().info("Inventario de la bolsa de trinkets no encontrado.");
                        return;
                    }

                    boolean trinketFound = false;
                    double additionalDamage = 0;

                    for (ItemStack item : bag.getContents()) {
                        if (item != null && item.getType() == Material.WARPED_FUNGUS_ON_A_STICK) {
                            String displayName = item.getItemMeta().getDisplayName();
                            Bukkit.getLogger().info("Encontrado item: " + displayName);
                            if ("§a§lDescalcificador".equals(displayName)) {
                                trinketFound = true;
                                additionalDamage = 1;
                                break;
                            } else if ("§6§lDescalcificador dorado".equals(displayName)) {
                                trinketFound = true;
                                additionalDamage = 2;
                                break;
                            }
                        }
                    }

                    if (trinketFound) {
                        double finalAdditionalDamage = additionalDamage;
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                event.setDamage(event.getDamage() + finalAdditionalDamage);
                                Bukkit.getLogger().info("Trinket encontrado y aplicado. Daño adicional: " + finalAdditionalDamage);
                            }
                        }.runTask(plugin);
                    } else {
                        Bukkit.getLogger().info("Trinket no encontrado en la bolsa de trinkets.");
                    }
                }
            }.runTaskAsynchronously(plugin);
        }
    }
}
