package ru.novacore.ui.altmanager;

import net.minecraft.client.Minecraft;
import ru.novacore.NovaCore;

import java.io.*;
import java.nio.file.Files;

public class AltConfig {

    public static final File file = new File(Minecraft.getInstance().gameDir, "\\novacore\\altManager.json");

    public void init() throws IOException {
        if (!file.exists()) {
            file.createNewFile();
        } else {
            readAlts();
        }
    }

    public static void updateFile() {
        try {
            StringBuilder builder = new StringBuilder();
            for (Account alt : NovaCore.getInstance().getAccount().accounts) {
                    builder.append(alt.accountName + ":" + alt.dateAdded).append("\n");
            }
            Files.write(file.toPath(), builder.toString().getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readAlts() {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new DataInputStream(new FileInputStream(file.getAbsolutePath()))));
            String line;

            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":");
                String username = parts[0];
                NovaCore.getInstance().getAccount().accounts.add(new Account(username, Long.valueOf(parts[1])));
            }

            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}