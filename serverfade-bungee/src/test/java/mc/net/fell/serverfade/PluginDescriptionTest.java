package mc.net.fell.serverfade;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.InputStream;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import org.junit.jupiter.api.Test;

class PluginDescriptionTest {

    // Прокси не грузит плагин с невалидным bungee.yml (так уже было из-за ':' без кавычек в description).
    @Test
    void bungeeYml_parsesAfterFiltering() throws Exception {
        try (InputStream in = PluginDescriptionTest.class.getResourceAsStream("/bungee.yml")) {
            Configuration yml = ConfigurationProvider.getProvider(YamlConfiguration.class).load(in);
            assertEquals("ServerFade", yml.getString("name"));
            assertEquals(ServerFadePlugin.class.getName(), yml.getString("main"));
        }
    }
}
