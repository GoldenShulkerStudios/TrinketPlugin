package me.ewahv1.plugin.CreateJsonFiles;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class CreateBagOfTrinketsConfigJson {

    public static void createJson() {
        BagOfTrinketsConfig config = new BagOfTrinketsConfig("Bolsa de Trinkets", 9);

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String json = gson.toJson(config);

        File file = new File("plugins/TrinketPlugin/BagOfTrinketsConfig.json");
        if (!file.exists()) {
            file.getParentFile().mkdirs();
        }

        try (FileWriter writer = new FileWriter(file)) {
            writer.write(json);
            System.out.println("BagOfTrinketsConfig.json has been created.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static class BagOfTrinketsConfig {
        private String nombre;
        private int slots;

        public BagOfTrinketsConfig(String nombre, int slots) {
            this.nombre = nombre;
            this.slots = slots;
        }

        public String getNombre() {
            return nombre;
        }

        public int getSlots() {
            return slots;
        }
    }
}
