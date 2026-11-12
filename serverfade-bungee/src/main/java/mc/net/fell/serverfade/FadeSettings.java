package mc.net.fell.serverfade;

import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import net.md_5.bungee.config.Configuration;

/**
 * config.yml, времена в тиках.
 *
 * @param fadeInTicks     за сколько экран темнеет, после этого игрок уходит на другой сервер
 * @param holdTicks       сколько держится чёрный экран, пока идёт переход
 * @param revealHoldTicks на новом сервере экран ещё столько остаётся чёрным (грузятся чанки)...
 * @param revealTicks     ...и потом столько проявляется
 * @param ignoredServers  имена в нижнем регистре: переходы на них и с них не затемняются
 */
record FadeSettings(int fadeInTicks, int holdTicks, int revealHoldTicks, int revealTicks, boolean revealOnFirstJoin,
                    Set<String> ignoredServers) {

    static final int MAX_TICKS = 200;
    static final FadeSettings DEFAULTS = new FadeSettings(10, 100, 10, 20, true, Set.of());
    private static final long MILLIS_PER_TICK = 50;

    FadeSettings {
        ignoredServers = Set.copyOf(ignoredServers);
    }

    static FadeSettings load(Configuration config) {
        return new FadeSettings(
                ticks(config, "fade-in-ticks", DEFAULTS.fadeInTicks),
                ticks(config, "hold-ticks", DEFAULTS.holdTicks),
                ticks(config, "reveal-hold-ticks", DEFAULTS.revealHoldTicks),
                ticks(config, "reveal-ticks", DEFAULTS.revealTicks),
                config.getBoolean("reveal-on-first-join", DEFAULTS.revealOnFirstJoin),
                config.getStringList("ignored-servers").stream()
                        .map(name -> name.toLowerCase(Locale.ROOT))
                        .collect(Collectors.toUnmodifiableSet()));
    }

    long fadeInMillis() {
        return fadeInTicks * MILLIS_PER_TICK;
    }

    private static int ticks(Configuration config, String key, int fallback) {
        return Math.max(0, Math.min(MAX_TICKS, config.getInt(key, fallback)));
    }
}
