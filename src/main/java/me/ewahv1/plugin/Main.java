package me.ewahv1.plugin;

import me.ewahv1.plugin.Commands.BagOfTrinketsCommand;
import me.ewahv1.plugin.CreateJsonFiles.InitManage;
import me.ewahv1.plugin.Listeners.Items.BagOfTrinketsListener;
import org.bukkit.plugin.java.JavaPlugin;
import me.ewahv1.plugin.Listeners.Trinkets.TrinketManage;

public class Main extends JavaPlugin {

    @Override
    public void onEnable() {
        getLogger().info("TrinketPlugin has been enabled!");
        InitManage initManage = new InitManage();
        initManage.initialize();

        this.getCommand("bolsa").setExecutor(new BagOfTrinketsCommand(this));
        getServer().getPluginManager().registerEvents(new BagOfTrinketsListener(this), this);

        TrinketManage.registerEvents(this);
    }

    @Override
    public void onDisable() {
        getLogger().info("TrinketPlugin has been disabled!");
    }
}
