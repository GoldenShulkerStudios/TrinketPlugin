package me.ewahv1.plugin.Utils;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.entity.Player;

public class BolsaListener implements Listener {

  @EventHandler
  public void onInventoryClose(InventoryCloseEvent event) {
    if (event.getView().getTitle().equals(ChatColor.GOLD + "Bolsa de Trinkets")) {
      Player player = (Player) event.getPlayer();
      String playerUUID = player.getUniqueId().toString();

      Inventory bolsa = event.getInventory();

      // Guardar la Bolsa al cerrar el inventario
      GenerarUserBolsaData.savePlayerBolsa(playerUUID, bolsa);
      player.sendMessage(ChatColor.GREEN + "¡Bolsa de Trinkets guardada!");
    }
  }
}
