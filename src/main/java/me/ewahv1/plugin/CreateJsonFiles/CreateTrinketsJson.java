package me.ewahv1.plugin.CreateJsonFiles;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class CreateTrinketsJson {

        public static void createJson() {
                Map<String, Map<String, Map<String, Object>>> trinkets = new HashMap<>();

                Map<String, Map<String, Object>> zombieTrinkets = new HashMap<>();
                zombieTrinkets.put("BrazoPutrefacto",
                                createTrinket(90, "Brazo Putrefacto", "Tus ataques básicos realizan +1❤ a los zombies.",
                                                1, false,
                                                50, "Brazo Putrefacto Dorado",
                                                "Tus ataques básicos realizan +2❤ a los zombies.", 2, true));
                trinkets.put("Zombie", zombieTrinkets);

                Map<String, Map<String, Object>> phantomTrinkets = new HashMap<>();
                phantomTrinkets.put("MAD",
                                createTrinket(90, "Mochila Antigravedad Defectuosa",
                                                "El portador tendrá Slow Falling I durante 3 segundos de manera random.",
                                                5, false,
                                                50, "Mochila Antigravedad Defectuosa Dorado",
                                                "El portador tendrá Slow Falling II durante 6 segundos de manera random.",
                                                6, true));
                trinkets.put("Phantom", phantomTrinkets);

                Map<String, Map<String, Object>> skeletonTrinkets = new HashMap<>();
                skeletonTrinkets.put("Descalcificador",
                                createTrinket(90, "Descalcificador",
                                                "Tus ataques básicos realizan +1❤ a los esqueletos.", 3, false,
                                                50, "Descalcificador Dorado",
                                                "Tus ataques básicos realizan +2❤ a los esqueletos.", 4, true));
                trinkets.put("Esqueleto", skeletonTrinkets);

                Map<String, Map<String, Object>> ravagerTrinkets = new HashMap<>();
                ravagerTrinkets.put("Warmog",
                                createTrinket(90, "Warmog",
                                                "Aumenta tu salud máxima en +4❤.",
                                                7, false, 50, "Warmog Dorado",
                                                "Aumenta tu salud máxima en +8❤.",
                                                8, true));
                trinkets.put("Ravager", ravagerTrinkets);

                Map<String, Map<String, Object>> crafting = new HashMap<>();
                crafting.put("Trinket3",
                                createTrinket(80, "Trinket Normal 3", "Descripción normal del trinket 3.", 5, false,
                                                50, "Trinket Dorado 3", "Descripción dorado del trinket 3.", 6, true));
                crafting.put("Trinket5",
                                createTrinket(75, "Trinket Normal 5", "Descripción normal del trinket 5.", 11, false,
                                                45, "Trinket Dorado 5", "Descripción dorado del trinket 5.", 12, true));
                trinkets.put("Crafting", crafting);

                Map<String, Map<String, Object>> loot = new HashMap<>();
                loot.put("Trinket4",
                                createTrinket(75, "Trinket Normal 4", "Descripción normal del trinket 4.", 7, false,
                                                40, "Trinket Dorado 4", "Descripción dorado del trinket 4.", 8, true));
                loot.put("Trinket6",
                                createTrinket(70, "Trinket Normal 6", "Descripción normal del trinket 6.", 13, false,
                                                35, "Trinket Dorado 6", "Descripción dorado del trinket 6.", 14, true));
                trinkets.put("Loot", loot);

                Map<String, Map<String, Object>> advancement = new HashMap<>();
                advancement.put("Trinket1",
                                createTrinket(70, "Trinket Normal 1", "Descripción normal del trinket 1.", 1, false,
                                                30, "Trinket Dorado 1", "Descripción dorado del trinket 1.", 2, true));
                advancement.put("Trinket2",
                                createTrinket(60, "Trinket Normal 2", "Descripción normal del trinket 2.", 3, false,
                                                40, "Trinket Dorado 2", "Descripción dorado del trinket 2.", 4, true));
                advancement.put("Trinket7",
                                createTrinket(65, "Trinket Normal 7", "Descripción normal del trinket 7.", 15, false,
                                                35, "Trinket Dorado 7", "Descripción dorado del trinket 7.", 16, true));
                trinkets.put("Advancement", advancement);

                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                String json = gson.toJson(trinkets);

                File file = new File("plugins/TrinketPlugin/Trinkets.json");
                if (!file.exists()) {
                        file.getParentFile().mkdirs();
                }

                try (FileWriter writer = new FileWriter(file)) {
                        writer.write(json);
                        System.out.println("Trinkets.json has been created.");
                } catch (IOException e) {
                        e.printStackTrace();
                }
        }

        private static Map<String, Object> createTrinket(int porcentajeDrodeo, String nombreNormal,
                        String descripcionNormal, int customModelNormal, boolean encantadoNormal, int porcentajeDorado,
                        String nombreDorado, String descripcionDorado, int customModelDorado, boolean encantadoDorado) {
                Map<String, Object> trinket = new HashMap<>();
                trinket.put("porcentaje_de_dropeo", porcentajeDrodeo);
                trinket.put("nombre_normal", nombreNormal);
                trinket.put("descripcion_normal", descripcionNormal);
                trinket.put("custom_model_normal", customModelNormal);
                trinket.put("encantado_normal", encantadoNormal);
                trinket.put("porcentaje_de_dorado", porcentajeDorado);
                trinket.put("nombre_dorado", nombreDorado);
                trinket.put("descripcion_dorado", descripcionDorado);
                trinket.put("custom_model_dorado", customModelDorado);
                trinket.put("encantado_dorado", encantadoDorado);
                return trinket;
        }
}
