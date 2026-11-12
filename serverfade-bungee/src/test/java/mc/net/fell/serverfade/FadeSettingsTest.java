package mc.net.fell.serverfade;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.InputStream;
import java.util.Set;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import org.junit.jupiter.api.Test;

class FadeSettingsTest {

    private static Configuration yaml(String text) {
        return ConfigurationProvider.getProvider(YamlConfiguration.class).load(text);
    }

    @Test
    void load_bundledConfigMatchesDefaults() throws Exception {
        try (InputStream in = FadeSettingsTest.class.getResourceAsStream("/config.yml")) {
            Configuration bundled = ConfigurationProvider.getProvider(YamlConfiguration.class).load(in);
            assertEquals(FadeSettings.DEFAULTS, FadeSettings.load(bundled));
        }
    }

    @Test
    void load_missingKeysUseDefaults() {
        assertEquals(FadeSettings.DEFAULTS, FadeSettings.load(yaml("")));
    }

    @Test
    void load_outOfRangeValuesAreClamped() {
        FadeSettings settings = FadeSettings.load(yaml("fade-in-ticks: -5\nhold-ticks: 9999\nreveal-ticks: 7\n"));
        assertEquals(0, settings.fadeInTicks());
        assertEquals(FadeSettings.MAX_TICKS, settings.holdTicks());
        assertEquals(7, settings.revealTicks());
    }

    @Test
    void load_ignoredServersAreLowerCase() {
        FadeSettings settings = FadeSettings.load(yaml("ignored-servers: [Limbo, duels]\nreveal-on-first-join: false\n"));
        assertEquals(Set.of("limbo", "duels"), settings.ignoredServers());
        assertFalse(settings.revealOnFirstJoin());
    }

    @Test
    void fadeInMillis_isTicksTimesFifty() {
        assertEquals(500, FadeSettings.DEFAULTS.fadeInMillis());
    }
}
