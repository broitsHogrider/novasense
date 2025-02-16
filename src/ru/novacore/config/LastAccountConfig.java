package ru.novacore.config;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Session;

import java.io.*;
import java.nio.file.Files;

public class LastAccountConfig {
    public static File lastAccountDir = new File(Minecraft.getInstance().gameDir, "\\novacore\\lastAccount.json");

    public void init() throws IOException {
        if (!lastAccountDir.exists()) {
            lastAccountDir.createNewFile();
        } else {
            readAlts();
        }
    }

    public void updateFile() {
        try {
            Files.write(lastAccountDir.toPath(), Minecraft.getInstance().session.getUsername().getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void readAlts() {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new DataInputStream(new FileInputStream(lastAccountDir.getAbsolutePath()))));
            String line;
            while ((line = reader.readLine()) != null) {
                Minecraft.getInstance().session = new Session(line, "", "", "mojang");
            }
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
