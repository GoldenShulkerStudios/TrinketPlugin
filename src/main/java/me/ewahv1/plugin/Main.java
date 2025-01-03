package me.ewahv1.plugin;

import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    @Override
    public void onEnable() {
        getLogger().info("==============================");
        getLogger().info("       TrinketsX Iniciado      ");
        getLogger().info("  Plugin desarrollado por ewahv1  ");
        getLogger().info("==============================");

        // Copiar archivos YML predeterminados
        new me.ewahv1.plugin.Utils.CopyYMLs(this).copyDefaultYMLs();

        // Registrar comandos
        getCommand("bolsa").setExecutor(new me.ewahv1.plugin.Commands.BolsaTrinketsCommand());

        // Registrar listeners
        getServer().getPluginManager().registerEvents(new me.ewahv1.plugin.Utils.BolsaListener(), this);
        getServer().getPluginManager().registerEvents(new me.ewahv1.plugin.Utils.GenerarBayaZidra(this), this);
        getServer().getPluginManager().registerEvents(new me.ewahv1.plugin.Listeners.Advancements.BayaZidra(this),
                this);

    }

    @Override
    public void onDisable() {
        getLogger().info("==============================");
        getLogger().info("       TrinketsX Deshabilitado    ");
        getLogger().info("==============================");
    }
}
