package com.ubivismedia.extensions;

import com.ubivismedia.extension.ExtensionInterface;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.jar.JarFile;
import java.util.jar.JarEntry;
import java.io.*;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;

public class NameGenerator implements ExtensionInterface {
    private final File namePatternFolder;
    private final Map<String, Map<String, YamlConfiguration>> namePatterns = new HashMap<>();
    private final Random random = new Random();

    public NameGenerator() {
        this(new File("plugins/UGS"));
    }
    public NameGenerator(File pluginFolder) {
        System.out.println("[UGS-NameGenerator] KONSTRUKTOR AUFGERUFEN!");

        // Test: Prüfen, ob ExtensionInterface zur Laufzeit vorhanden ist
        try {
            Class.forName("com.ubivismedia.extension.ExtensionInterface");
            System.out.println("[UGS-NameGenerator] ExtensionInterface erfolgreich gefunden!");
        } catch (ClassNotFoundException e) {
            System.out.println("[UGS-NameGenerator] FEHLER: ExtensionInterface nicht gefunden!");
        }

        this.namePatternFolder = new File(pluginFolder, "NameGenerator");
        if (!namePatternFolder.exists()) {
            namePatternFolder.mkdirs();
        }
        extractDefaultPatterns();
        loadPatterns();
    }



    private void loadPatterns() {
        namePatterns.clear();

        // Prüfen, ob der NameGenerator-Ordner existiert
        if (!namePatternFolder.exists()) {
            System.out.println("[UGS-NameGenerator] ❌ FEHLER: Der NameGenerator-Ordner existiert nicht!");
            return;
        }

        System.out.println("[UGS-NameGenerator] 📂 Lade Namenskategorien aus: " + namePatternFolder.getAbsolutePath());

        // Starte rekursive Suche
        scanCategoryFolder(namePatternFolder, "");

        System.out.println("[UGS-NameGenerator] 🎉 " + namePatterns.size() + " Kategorien erfolgreich geladen!");
    }

    /**
     * Durchsucht das angegebene Verzeichnis rekursiv nach Namenskategorien.
     * @param folder Das aktuelle Verzeichnis, das gescannt wird.
     * @param categoryPath Der relative Kategoriename (z. B. "Fantasy/Dwarves").
     */
    private void scanCategoryFolder(File folder, String categoryPath) {
        File[] subFiles = folder.listFiles();
        if (subFiles == null) return;

        for (File file : subFiles) {
            if (file.isDirectory()) {
                // Unterverzeichnis -> Weiter in die Tiefe gehen
                String newCategoryPath = categoryPath.isEmpty() ? file.getName() : categoryPath + "/" + file.getName();
                scanCategoryFolder(file, newCategoryPath);
            } else {
                // Prüfen, ob es eine der gesuchten YML-Dateien ist
                if (file.getName().equals("male.yml") || file.getName().equals("female.yml")) {
                    // Kategorie registrieren, falls noch nicht vorhanden
                    namePatterns.putIfAbsent(categoryPath, new HashMap<>());

                    // YML-Datei laden und zuordnen
                    String gender = file.getName().replace(".yml", "");
                    namePatterns.get(categoryPath).put(gender, YamlConfiguration.loadConfiguration(file));

                    System.out.println("[UGS-NameGenerator] ✅ " + gender + " Namen geladen für Kategorie: " + categoryPath);
                }
            }
        }
    }



    @Override
    public String getName() {
        return "NameGenerator";
    }
    
    @Override
    public String getDescription() {
        return "Returns a randomly generated Name";
    }
    
    @Override
    public String execute(String[] params) {
        if (params.length < 2) {
            return "Error: State category and gender.";
        }

        String category = params[0];
        String gender = params[1];

        if (!namePatterns.containsKey(category) || !namePatterns.get(category).containsKey(gender)) {
            return "Unknown";
        }

        YamlConfiguration config = namePatterns.get(category).get(gender);
        List<String> prefixes = config.getStringList("prefixes");
        List<String> middles = config.getStringList("middles");
        List<String> suffixes = config.getStringList("suffixes");

        String prefix = prefixes.isEmpty() ? "" : prefixes.get(random.nextInt(prefixes.size()));
        String middle = middles.isEmpty() ? "" : middles.get(random.nextInt(middles.size()));
        String suffix = suffixes.isEmpty() ? "" : suffixes.get(random.nextInt(suffixes.size()));

        return prefix + middle + suffix;
    }

    private void extractDefaultPatterns() {
        String[] resources = {
                "NameGenerator/Fantasy/Dwarves/male.yml", "NameGenerator/Fantasy/Dwarves/female.yml",
                "NameGenerator/Fantasy/Elves/male.yml", "NameGenerator/Fantasy/Elves/female.yml",
                "NameGenerator/Fantasy/Orks/male.yml", "NameGenerator/Fantasy/Orks/female.yml",
                "NameGenerator/Human/European/German/male.yml", "NameGenerator/Human/European/German/female.yml",
                "NameGenerator/Human/European/Scandinavian/male.yml", "NameGenerator/Human/European/Scandinavian/female.yml"
        };

        for (String resource : resources) {
            File destFile = new File(namePatternFolder.getParentFile(), resource);
            if (!destFile.exists()) {
                saveResource(resource, destFile);
            }
        }
    }

    private void saveResource(String resourcePath, File destFile) {
        System.out.println("[UGS-NameGenerator] Versuche Ressource aus JAR zu extrahieren: " + resourcePath);

        // Pfad zur aktuellen Extension-JAR ermitteln
        File jarFile = new File(getClass().getProtectionDomain().getCodeSource().getLocation().getPath());

        try (JarFile jar = new JarFile(jarFile)) {
            JarEntry entry = jar.getJarEntry(resourcePath);

            if (entry == null) {
                System.out.println("[UGS-NameGenerator] ❌ FEHLER: Ressource nicht gefunden in der JAR -> " + resourcePath);
                return;
            }

            // Zielordner erstellen
            destFile.getParentFile().mkdirs();

            // Datei aus der JAR extrahieren
            try (InputStream in = jar.getInputStream(entry);
                 OutputStream out = new FileOutputStream(destFile)) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
            }

            System.out.println("[UGS-NameGenerator] ✅ Datei extrahiert: " + resourcePath);

        } catch (Exception e) {
            System.out.println("[UGS-NameGenerator] ❌ Fehler beim Extrahieren von " + resourcePath + ": " + e.getMessage());
        }
    }

    public void reloadPatterns() {
        loadPatterns();
    }


}
