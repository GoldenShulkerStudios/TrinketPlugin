package me.ewahv1.plugin.Utils;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class CopyYMLs {

    private final JavaPlugin plugin;

    public CopyYMLs(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void copyFile(String fileName) {
        try {
            File pluginFolder = plugin.getDataFolder();
            if (!pluginFolder.exists()) {
                pluginFolder.mkdirs();
            }

            File targetFile = new File(pluginFolder, fileName);
            if (!targetFile.exists()) {
                InputStream inputStream = plugin.getResource(fileName);
                if (inputStream != null) {
                    Files.copy(inputStream, targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    plugin.getLogger().info("Archivo " + fileName + " copiado correctamente.");
                } else {
                    plugin.getLogger().severe("No se encontró el archivo " + fileName + " en los recursos del plugin.");
                }
            } else {
                plugin.getLogger().info("Archivo " + fileName + " ya existe, no se realizó ninguna copia.");
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Error al copiar el archivo " + fileName + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void copyDefaultYMLs() {
        copyFile("config.yml");
        copyFile("trinkets.yml");
    }
}
