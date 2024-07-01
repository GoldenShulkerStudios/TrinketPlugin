package me.ewahv1.plugin.Commands;

import me.ewahv1.plugin.Listeners.Items.TrinketBag.BagOfTrinkets;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TrinketBagCommand implements CommandExecutor {

    private final BagOfTrinkets bagOfTrinkets;

    public TrinketBagCommand(BagOfTrinkets bagOfTrinkets) {
        this.bagOfTrinkets = bagOfTrinkets;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            bagOfTrinkets.openTrinketBag(player);
            return true;
        }
        sender.sendMessage("Este comando solo puede ser utilizado por jugadores.");
        return false;
    }
}
