package ru.novacore.config;

import com.google.gson.*;
import net.minecraft.client.Minecraft;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ConfigStorage {

    public final Logger logger = Logger.getLogger(ConfigStorage.class.getName());

    public final File CONFIG_DIR = new File(Minecraft.getInstance().gameDir, "\\novacore\\configs");
    public final File AUTOCFG_DIR = new File(CONFIG_DIR, "autocfg.json");


    public void init() throws IOException {
        setupFolder();
    }

    public void setupFolder() {
        if (!CONFIG_DIR.exists()) {
            CONFIG_DIR.mkdirs();
        } else if (AUTOCFG_DIR.exists()) {
            loadConfiguration("autocfg");
            logger.log(Level.INFO, "Load system configuration..."); // Зеленый цвет для информации
        } else {
            logger.log(Level.INFO, "Creating system configuration...");
            try {
                AUTOCFG_DIR.createNewFile();
                logger.log(Level.INFO, "created");
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Failed to create system configuration file", e);
            }
        }
    }


    public boolean isEmpty() {
        return getConfigs().isEmpty();
    }

    public List<Config> getConfigs() {
        List<Config> configs = new ArrayList<>();
        File[] configFiles = CONFIG_DIR.listFiles();

        if (configFiles != null) {
            for (File configFile : configFiles) {
                if (configFile.isFile() && configFile.getName().endsWith(".json")) {
                    String configName = configFile.getName().replace(".json", "");
                    Config config = findConfig(configName);
                    if (config != null) {
                        configs.add(config);
                    }
                }
            }
        }

        return configs;
    }


    public void loadConfiguration(String configuration) {
        Config config = findConfig(configuration);

        if (config == null) return;

        try (FileReader reader = new FileReader(config.getFile())) {
            JsonParser parser = new JsonParser();
            JsonElement element = parser.parse(reader);

            if (element.isJsonObject()) {
                JsonObject object = element.getAsJsonObject();
                config.loadConfig(object);
            } else {
                logger.log(Level.WARNING, "Invalid JSON format: Expected JsonObject.");
            }
        } catch (FileNotFoundException e) {
            logger.log(Level.WARNING, "Configuration file not found: " + config.getFile(), e);
        } catch (IOException e) {
            logger.log(Level.WARNING, "Error reading configuration file: " + config.getFile(), e);
        } catch (JsonParseException parseException) {
            logger.log(Level.WARNING, "Invalid JSON content!", parseException);
        }
    }

    public void saveConfiguration(String configuration) {
        Config config = new Config(configuration);
        String contentPrettyPrint = new GsonBuilder().setPrettyPrinting().create().toJson(config.saveConfig());
        try {
            FileWriter writer = new FileWriter(config.getFile());
            writer.write(contentPrettyPrint);
            writer.close();
        } catch (IOException e) {
            logger.log(Level.WARNING, "File not found!", e);
        } catch (NullPointerException e) {
            logger.log(Level.WARNING, "Fatal Error in Config!", e);
        }
    }

    public Config findConfig(String configName) {
        if (configName == null) return null;
        if (new File(CONFIG_DIR, configName + ".json").exists())
            return new Config(configName);
        return null;
    }
}
