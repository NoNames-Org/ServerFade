package mc.net.fell.serverfade.paper;

import org.bukkit.configuration.ConfigurationSection;

/**
 * config.yml, времена в тиках, обрезаются до 0..{@link #MAX_TICKS}.
 *
 * @param fadeInTicks     за сколько экран темнеет, после этого выполняется действие
 * @param holdTicks       сколько максимум держится чёрный экран без действия
 * @param revealHoldTicks после действия экран ещё столько остаётся чёрным (грузятся чанки)...
 * @param revealTicks     ...и потом столько проявляется
 */
record FadeSettings(int fadeInTicks, int holdTicks, int revealHoldTicks, int revealTicks) {

    static final int MAX_TICKS = 200;
    static final FadeSettings DEFAULTS = new FadeSettings(10, 100, 10, 20);

    static FadeSettings load(ConfigurationSection config) {
        return new FadeSettings(
                ticks(config, "fade-in-ticks", DEFAULTS.fadeInTicks),
                ticks(config, "hold-ticks", DEFAULTS.holdTicks),
                ticks(config, "reveal-hold-ticks", DEFAULTS.revealHoldTicks),
                ticks(config, "reveal-ticks", DEFAULTS.revealTicks));
    }

    private static int ticks(ConfigurationSection config, String key, int fallback) {
        return Math.max(0, Math.min(MAX_TICKS, config.getInt(key, fallback)));
    }
}
