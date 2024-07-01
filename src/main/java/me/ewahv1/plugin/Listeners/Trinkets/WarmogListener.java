package me.ewahv1.plugin.Listeners.Trinkets;

import me.ewahv1.plugin.Database.DatabaseConnection;
import me.ewahv1.plugin.Listeners.Items.TrinketBag.BagOfTrinkets;
import me.ewahv1.plugin.Main;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.entity.Ravager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
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
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class WarmogListener implements Listener {
    private final Map<UUID, Long> lastDamaged = new HashMap<>();
    private final Main plugin;
    private final BagOfTrinkets bagOfTrinkets;
    private final DatabaseConnection databaseConnection;

    public WarmogListener(Main plugin, DatabaseConnection databaseConnection, BagOfTrinkets bagOfTrinkets) {
        this.plugin = plugin;
        this.databaseConnection = plugin.getDatabaseConnection();
        this.bagOfTrinkets = new BagOfTrinkets(plugin); // Crear BagOfTrinkets usando el plugin
    }

    @EventHandler
    public void onRavagerDeath(EntityDeathEvent event) {
        if (event.getEntity() instanceof Ravager) {
            Ravager ravager = (Ravager) event.getEntity();
            if (ravager.getKiller() instanceof Player) {
                Random rand = new Random();
                int chance = rand.nextInt(100);
                if (chance < 90) {
                    int goldenChance = rand.nextInt(100);
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            try (Connection connection = databaseConnection.getConnection();
                                 Statement statement = connection.createStatement()) {
                                ItemStack warmogArmor;
                                if (goldenChance < 50) {
                                    warmogArmor = new ItemStack(Material.WARPED_FUNGUS_ON_A_STICK, 1);
                                    ItemMeta meta = warmogArmor.getItemMeta();
                                    meta.setDisplayName("§6§lArmadura de Warmog dorada");
                                    meta.setLore(Arrays.asList("§aAumenta tu salud máxima en +4❤", "§6§lSlot: Mano secundaria"));
                                    meta.setCustomModelData(8);
                                    meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                                    meta.addEnchant(Enchantment.DURABILITY, 1, true);
                                    meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                                    warmogArmor.setItemMeta(meta);

                                    statement.executeUpdate("UPDATE tri_count_settings SET CountGold = CountGold + 1 WHERE ID = 3");
                                } else {
                                    warmogArmor = new ItemStack(Material.WARPED_FUNGUS_ON_A_STICK, 1);
                                    ItemMeta meta = warmogArmor.getItemMeta();
                                    meta.setDisplayName("§a§lArmadura de Warmog");
                                    meta.setLore(Arrays.asList("§aAumenta tu salud máxima en +2❤", "§6§lSlot: Mano secundaria"));
                                    meta.setCustomModelData(7);
                                    meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                                    warmogArmor.setItemMeta(meta);

                                    statement.executeUpdate("UPDATE tri_count_settings SET CountNormal = CountNormal + 1 WHERE ID = 3");
                                }
                                new BukkitRunnable() {
                                    @Override
                                    public void run() {
                                        event.getDrops().add(warmogArmor);
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
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            UUID playerUuid = player.getUniqueId();
            if (player.getHealth() <= player.getMaxHealth() / 2) { // Check if player's health is less than or equal to half
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
                                            long currentTime = System.currentTimeMillis();
                                            long lastTimeDamaged = lastDamaged.getOrDefault(playerUuid, 0L);
                                            if (currentTime - lastTimeDamaged > 6000) { // 6 seconds cooldown
                                                applyRegenerationEffect(player, item);
                                                lastDamaged.put(playerUuid, currentTime);
                                                break;
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
    }

    private void applyRegenerationEffect(Player player, ItemStack item) {
        if (item.getType() == Material.WARPED_FUNGUS_ON_A_STICK) {
            if (item.getItemMeta().getDisplayName().equals("§a§lArmadura de Warmog")) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 40, 0, true, false, true)); // 2 seconds of regeneration 1
            } else if (item.getItemMeta().getDisplayName().equals("§6§lArmadura de Warmog dorada")) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 80, 1, true, false, true)); // 4 seconds of regeneration 2
            }
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
            Inventory inventory = Bukkit.getServer().createInventory(null, dataInput.readInt());

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
