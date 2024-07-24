package me.ewahv1.plugin.Listeners.Trinkets;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Map;
import java.util.Random;

public class TrinketDropManager {

    private final JavaPlugin plugin;
    private final Random rand = new Random();

    public TrinketDropManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void handleEntityDeath(EntityDeathEvent event, String entityType, String trinketName, Material material) {
        if (event.getEntity().getKiller() != null) {
            Map<String, Map<String, Map<String, Object>>> trinkets = loadTrinkets();

            if (trinkets != null && trinkets.containsKey(entityType)
                    && trinkets.get(entityType).containsKey(trinketName)) {
                Map<String, Object> trinketData = trinkets.get(entityType).get(trinketName);

                int chance = rand.nextInt(100);
                if (chance < ((Double) trinketData.get("porcentaje_de_dropeo")).intValue()) {
                    int goldenChance = rand.nextInt(100);
                    ItemStack itemStack;
                    ItemMeta meta;

                    if (goldenChance < ((Double) trinketData.get("porcentaje_de_dorado")).intValue()) {
                        itemStack = new ItemStack(material, 1);
                        meta = itemStack.getItemMeta();
                        meta.setDisplayName("§6§l" + trinketData.get("nombre_dorado"));
                        meta.setLore(Arrays.asList((String) trinketData.get("descripcion_dorado")));
                        meta.setCustomModelData(((Double) trinketData.get("custom_model_dorado")).intValue());
                        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                        if ((Boolean) trinketData.get("encantado_dorado")) {
                            meta.addEnchant(Enchantment.UNBREAKING, 1, true);
                            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                        }
                        itemStack.setItemMeta(meta);
                    } else {
                        itemStack = new ItemStack(material, 1);
                        meta = itemStack.getItemMeta();
                        meta.setDisplayName((String) trinketData.get("nombre_normal"));
                        meta.setLore(Arrays.asList((String) trinketData.get("descripcion_normal")));
                        meta.setCustomModelData(((Double) trinketData.get("custom_model_normal")).intValue());
                        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                        if ((Boolean) trinketData.get("encantado_normal")) {
                            meta.addEnchant(Enchantment.UNBREAKING, 1, true);
                            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                        }
                        itemStack.setItemMeta(meta);
                    }
                    event.getDrops().add(itemStack);
                }
            }
        }
    }

    private Map<String, Map<String, Map<String, Object>>> loadTrinkets() {
        Gson gson = new Gson();
        File file = new File(plugin.getDataFolder(), "Trinkets.json");
        if (file.exists()) {
            try (FileReader reader = new FileReader(file)) {
                Type type = new TypeToken<Map<String, Map<String, Map<String, Object>>>>() {
                }.getType();
                return gson.fromJson(reader, type);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
