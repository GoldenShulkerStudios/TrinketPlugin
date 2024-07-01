package me.ewahv1.plugin.Listeners.Trinkets;

import me.ewahv1.plugin.Database.DatabaseConnection;
import me.ewahv1.plugin.Listeners.Items.TrinketBag.BagOfTrinkets;
import me.ewahv1.plugin.Main;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Phantom;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerMoveEvent;
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

public class MADListener implements Listener {

    private final Map<UUID, Long> cooldowns = new HashMap<>();
    private final DatabaseConnection databaseConnection;
    private final Main plugin;

    public MADListener(Main plugin, DatabaseConnection databaseConnection, BagOfTrinkets bagOfTrinkets) {
        this.databaseConnection = databaseConnection;
        this.plugin = plugin;
    }

    @EventHandler
    public void onPhantomDeath(EntityDeathEvent event) {
        if (event.getEntity() instanceof Phantom) {
            Phantom phantom = (Phantom) event.getEntity();
            if (phantom.getKiller() instanceof Player) {
                Random rand = new Random();
                int chance = rand.nextInt(100);
                if (chance < 90) {
                    int goldenChance = rand.nextInt(100);
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            try (Connection connection = databaseConnection.getConnection();
                                 Statement statement = connection.createStatement()) {
                                ItemStack defectiveGravityBackpack;
                                if (goldenChance < 50) {
                                    defectiveGravityBackpack = new ItemStack(Material.WARPED_FUNGUS_ON_A_STICK, 1);
                                    ItemMeta meta = defectiveGravityBackpack.getItemMeta();
                                    meta.setDisplayName("§6§lMochila Antigravedad Defectuosa Dorada");
                                    meta.setLore(Arrays.asList("§aEl portador tendrá Slow Falling II durante 6 segundos de manera aleatoria cada 1 segundo", "§6§lSlot: B.O.T"));
                                    meta.setCustomModelData(6);
                                    meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                                    meta.addEnchant(Enchantment.DURABILITY, 1, true);
                                    meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                                    defectiveGravityBackpack.setItemMeta(meta);

                                    statement.executeUpdate("UPDATE tri_count_settings SET CountGold = CountGold + 1 WHERE ID = 2");
                                } else {
                                    defectiveGravityBackpack = new ItemStack(Material.WARPED_FUNGUS_ON_A_STICK, 1);
                                    ItemMeta meta = defectiveGravityBackpack.getItemMeta();
                                    meta.setDisplayName("§a§lMochila Antigravedad Defectuosa");
                                    meta.setLore(Arrays.asList("§aEl portador tendrá Slow Falling I durante 3 segundos de manera aleatoria cada 2 segundos", "§6§lSlot: B.O.T"));
                                    meta.setCustomModelData(5);
                                    meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                                    defectiveGravityBackpack.setItemMeta(meta);

                                    statement.executeUpdate("UPDATE tri_count_settings SET CountNormal = CountNormal + 1 WHERE ID = 2");
                                }
                                new BukkitRunnable() {
                                    @Override
                                    public void run() {
                                        event.getDrops().add(defectiveGravityBackpack);
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
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        UUID playerUuid = player.getUniqueId();
        Long cooldownTime = cooldowns.get(playerUuid);

        if (cooldownTime != null && cooldownTime > System.currentTimeMillis()) {
            return;
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                Inventory bag = getBag(playerUuid);

                if (bag == null) {
                    return;
                }

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        for (ItemStack item : bag.getContents()) {
                            if (item != null && item.getType() == Material.WARPED_FUNGUS_ON_A_STICK) {
                                String displayName = item.getItemMeta().getDisplayName();
                                Random rand = new Random();

                                if (displayName.equals("§a§lMochila Antigravedad Defectuosa") && rand.nextInt(2) == 0) {
                                    player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 60, 0, true, false, true)); // 3 segundos de Slow Falling I
                                    cooldowns.put(playerUuid, System.currentTimeMillis() + 300000); // 5 minutos de cooldown
                                } else if (displayName.equals("§6§lMochila Antigravedad Defectuosa Dorada") && rand.nextInt(2) == 0) {
                                    player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 120, 1, true, false, true)); // 6 segundos de Slow Falling II
                                    cooldowns.put(playerUuid, System.currentTimeMillis() + 300000); // 5 minutos de cooldown
                                }
                                return;
                            }
                        }
                    }
                }.runTask(plugin);
            }
        }.runTaskAsynchronously(plugin);
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