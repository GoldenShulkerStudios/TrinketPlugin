package me.ewahv1.plugin.Commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import me.ewahv1.plugin.Utils.GenerarUserBolsaData;

public class BolsaTrinketsCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Este comando solo puede ser utilizado por jugadores.");
            return true;
        }

        Player player = (Player) sender;
        String playerUUID = player.getUniqueId().toString();

        if (!GenerarUserBolsaData.doesPlayerBolsaExist(playerUUID)) {
            GenerarUserBolsaData.createPlayerBolsa(playerUUID);
            player.sendMessage(ChatColor.GREEN + "Se ha generado tu Bolsa de Trinkets.");
        }

        Inventory bolsa = Bukkit.createInventory(player, 9, ChatColor.GOLD + "Bolsa de Trinkets");

        GenerarUserBolsaData.loadPlayerBolsa(playerUUID, bolsa);

        player.openInventory(bolsa);
        return true;
    }
}
