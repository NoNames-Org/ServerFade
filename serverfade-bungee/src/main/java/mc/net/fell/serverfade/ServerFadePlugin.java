package mc.net.fell.serverfade;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.logging.Level;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

public final class ServerFadePlugin extends Plugin {

    private static final String CONFIG = "config.yml";

    @Override
    public void onEnable() {
        FadeSettings settings = loadSettings();
        FadeListener listener = new FadeListener(this, settings);
        getProxy().getPluginManager().registerListener(this, listener);
        getProxy().getPluginManager().registerCommand(this, new ServerFadeCommand(this, listener));
        getLogger().info("Затемнение " + settings.fadeInTicks() + " тиков, проявление " + settings.revealTicks()
                + " тиков");
    }

    FadeSettings loadSettings() {
        File file = new File(getDataFolder(), CONFIG);
        try {
            if (!file.exists()) {
                saveDefaultConfig(file);
            }
            return FadeSettings.load(ConfigurationProvider.getProvider(YamlConfiguration.class).load(file));
        } catch (IOException e) {
            getLogger().log(Level.WARNING, "Не удалось прочитать " + file + ", используются стандартные тайминги", e);
            return FadeSettings.DEFAULTS;
        }
    }

    private void saveDefaultConfig(File file) throws IOException {
        Files.createDirectories(getDataFolder().toPath());
        try (InputStream bundled = getResourceAsStream(CONFIG)) {
            Files.copy(bundled, file.toPath());
        }
    }
}
