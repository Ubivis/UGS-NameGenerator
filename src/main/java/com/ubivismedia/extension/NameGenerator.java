package com.ubivismedia.extensions;

import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class DynamicNameGenerator {
    private final File namePatternFolder;
    private final Map<String, Map<String, YamlConfiguration>> namePatterns = new HashMap<>();
    private final Random random = new Random();

    public DynamicNameGenerator(File pluginFolder) {
        this.namePatternFolder = new File(pluginFolder, "NameGenerator");
        if (!namePatternFolder.exists()) {
            namePatternFolder.mkdirs();
        }
        loadPatterns();
    }

    private void loadPatterns() {
        namePatterns.clear(); // Leert die Map und lädt neu

        for (File categoryFolder : Objects.requireNonNull(namePatternFolder.listFiles(File::isDirectory))) {
            String category = categoryFolder.getName();
            Map<String, YamlConfiguration> genderFiles = new HashMap<>();

            File maleFile = new File(categoryFolder, "male.yml");
            File femaleFile = new File(categoryFolder, "female.yml");

            if (maleFile.exists()) {
                genderFiles.put("male", YamlConfiguration.loadConfiguration(maleFile));
            }
            if (femaleFile.exists()) {
                genderFiles.put("female", YamlConfiguration.loadConfiguration(femaleFile));
            }

            if (!genderFiles.isEmpty()) {
                namePatterns.put(category, genderFiles);
            }
        }
        System.out.println("[UGS-NameGenerator] " + namePatterns.size() + " Kategorien erfolgreich geladen!");
    }

    public String generateName(String category, String gender) {
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

    public void reloadPatterns() {
        loadPatterns();
    }
}
