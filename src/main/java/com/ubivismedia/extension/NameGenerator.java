package com.ugs.extensions;

import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Random;

public class NameGenerator {
    private final File namePatternFolder;
    private final Random random = new Random();

    public NameGenerator(File pluginFolder) {
        this.namePatternFolder = new File(pluginFolder, "names");
        if (!namePatternFolder.exists()) {
            namePatternFolder.mkdirs();
        }
    }

    public String generateName(String category, String gender) {
        File categoryFolder = new File(namePatternFolder, category);
        if (!categoryFolder.exists() || !categoryFolder.isDirectory()) {
            return "Unknown";
        }

        File configFile = new File(categoryFolder, gender.toLowerCase() + ".yml");
        if (!configFile.exists()) {
            return "Unknown";
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
        List<String> prefixes = config.getStringList("prefixes");
        List<String> middles = config.getStringList("middles");
        List<String> suffixes = config.getStringList("suffixes");

        String prefix = prefixes.isEmpty() ? "" : prefixes.get(random.nextInt(prefixes.size()));
        String middle = middles.isEmpty() ? "" : middles.get(random.nextInt(middles.size()));
        String suffix = suffixes.isEmpty() ? "" : suffixes.get(random.nextInt(suffixes.size()));

        return prefix + middle + suffix;
    }

    public void createDefaultPattern(String category) {
        File categoryFolder = new File(namePatternFolder, category);
        if (!categoryFolder.exists()) {
            categoryFolder.mkdirs();
        }

        File maleConfig = new File(categoryFolder, "male.yml");
        File femaleConfig = new File(categoryFolder, "female.yml");
        try {
            if (!maleConfig.exists()) {
                maleConfig.createNewFile();
                YamlConfiguration maleYaml = new YamlConfiguration();
                maleYaml.set("prefixes", List.of("A", "Be", "Ce"));
                maleYaml.set("middles", List.of("dar", "lor", "vin"));
                maleYaml.set("suffixes", List.of("ian", "os", "th"));
                maleYaml.save(maleConfig);
            }
            if (!femaleConfig.exists()) {
                femaleConfig.createNewFile();
                YamlConfiguration femaleYaml = new YamlConfiguration();
                femaleYaml.set("prefixes", List.of("El", "Li", "Sa"));
                femaleYaml.set("middles", List.of("la", "na", "re"));
                femaleYaml.set("suffixes", List.of("ia", "a", "elle"));
                femaleYaml.save(femaleConfig);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
