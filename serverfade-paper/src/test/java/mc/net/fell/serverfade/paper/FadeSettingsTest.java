package mc.net.fell.serverfade.paper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

class FadeSettingsTest {

    @Test
    void missingKeysUseTheDefaults() {
        assertEquals(FadeSettings.DEFAULTS, FadeSettings.load(new YamlConfiguration()));
    }

    @Test
    void valuesAreClampedToTheAllowedRange() throws InvalidConfigurationException {
        YamlConfiguration config = new YamlConfiguration();
        config.loadFromString("fade-in-ticks: -5\nhold-ticks: 9999\nreveal-hold-ticks: 4\nreveal-ticks: 30\n");

        assertEquals(new FadeSettings(0, FadeSettings.MAX_TICKS, 4, 30), FadeSettings.load(config));
    }
}
