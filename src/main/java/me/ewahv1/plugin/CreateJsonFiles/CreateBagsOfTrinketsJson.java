package me.ewahv1.plugin.CreateJsonFiles;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class CreateBagsOfTrinketsJson {

    public static void createJson() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String json = "[]"; // Inicializar como un array vacío

        File file = new File("plugins/TrinketPlugin/BagsOfTrinkets.json");
        if (!file.exists()) {
            file.getParentFile().mkdirs();
        }

        try (FileWriter writer = new FileWriter(file)) {
            writer.write(json);
            System.out.println("BagsOfTrinkets.json has been created.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
