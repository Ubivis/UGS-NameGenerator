package com.ubivismedia.extensions;

import com.ubivis.ugs.api.ExtensionInterface;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.*;

public class DynamicNameGenerator implements ExtensionInterface {
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
        namePatterns.clear();

        File[] categoryFolders = namePatternFolder.listFiles(File::isDirectory);
        if (categoryFolders == null) return;

        for (File categoryFolder : categoryFolders) {
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

    public void reloadPatterns() {
        loadPatterns();
    }
}
