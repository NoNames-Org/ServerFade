package mc.net.fell.serverfade.paper;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import net.kyori.adventure.util.Ticks;
import org.bukkit.entity.Player;

/** Тот же title, что у ServerFade на BungeeCord: глиф чёрного экрана {@code serverfade:fade} и логотип поверх. */
final class FadeTitles {

    private static final Component SCREEN = Component.text("", NamedTextColor.WHITE)
            .font(Key.key("serverfade", "fade"))
            .append(Component.text("").font(Key.key("serverfade", "logo")));

    private FadeTitles() {
    }

    /** Затемняет и держит чёрным {@code holdTicks}. */
    static void darken(Player player, FadeSettings settings) {
        show(player, settings.fadeInTicks(), settings.holdTicks(), 0);
    }

    /** Сразу чёрный, затем проявление. */
    static void reveal(Player player, FadeSettings settings) {
        show(player, 0, settings.revealHoldTicks(), settings.revealTicks());
    }

    private static void show(Player player, int fadeIn, int stay, int fadeOut) {
        player.showTitle(Title.title(SCREEN, Component.empty(),
                Title.Times.times(Ticks.duration(fadeIn), Ticks.duration(stay), Ticks.duration(fadeOut))));
    }
}
