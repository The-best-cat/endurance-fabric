package net.theblackcat.endurance.config;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;

import java.io.*;
import java.nio.charset.StandardCharsets;

@Environment(EnvType.CLIENT)
public class EnduranceConfig {
    private boolean showVignette = true;

    private static EnduranceConfig instance;

    public EnduranceConfig() throws IOException {
        if (GetFile().createNewFile()) {
            Save();
        } else {
            Read();
        }
    }

    public boolean ShouldShowVignette() {
        return showVignette;
    }

    public void SetShowVignette(boolean show) {
        showVignette = show;

    }

    public static EnduranceConfig Instance() throws IOException {
        if (instance == null) {
            instance = new EnduranceConfig();
        }
        return instance;
    }

    private static File GetFile() {
        return FabricLoader.getInstance().getConfigDir().resolve("endurance-config.txt").toFile();
    }

    private void Save() {
        try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(GetFile()), StandardCharsets.UTF_8))) {
            writer.write(String.valueOf(showVignette));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void Read() {
        try (BufferedReader reader = new BufferedReader(new FileReader(GetFile()))) {
            showVignette = Boolean.parseBoolean(reader.readLine());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
