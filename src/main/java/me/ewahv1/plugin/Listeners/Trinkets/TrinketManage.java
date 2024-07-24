package me.ewahv1.plugin.Listeners.Trinkets;

import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class TrinketManage {

    public static void registerEvents(JavaPlugin plugin) {
        PluginManager pm = plugin.getServer().getPluginManager();
        pm.registerEvents(new BrazoPutrefactoListener(plugin), plugin);
        pm.registerEvents(new DescalcificadorListener(plugin), plugin);
        pm.registerEvents(new MochilaAntigravedadDefectuosaListener(plugin), plugin);
        pm.registerEvents(new WarmogListener(plugin), plugin);
    }
}
