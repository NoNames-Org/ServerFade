package mc.net.fell.serverfade;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

/**
 * Чёрный экран — это title из одного глифа шрифта {@code serverfade:fade}, который шейдер ресурспака растягивает на
 * весь экран, плюс логотип {@code serverfade:logo} поверх. Анимацию даёт обычное появление и исчезание title.
 */
final class FadeTitles {

    private static final String FADE_FONT = "serverfade:fade";
    private static final String FADE_GLYPH = "";
    // Логотип темнеет и светлеет вместе с экраном. Экран подключения рисует тот же логотип на том же месте,
    // поэтому при смене одного на другое он не дёргается.
    private static final String LOGO_FONT = "serverfade:logo";
    private static final String LOGO_GLYPH = "";

    private FadeTitles() {
    }

    /** Затемняет и держит чёрным {@code holdTicks}. */
    static void darken(ProxiedPlayer player, FadeSettings settings) {
        show(player, settings.fadeInTicks(), settings.holdTicks(), 0);
    }

    /** Сразу чёрный, затем проявление: по прибытии на сервер или если переход сорвался. */
    static void reveal(ProxiedPlayer player, FadeSettings settings) {
        show(player, 0, settings.revealHoldTicks(), settings.revealTicks());
    }

    private static void show(ProxiedPlayer player, int fadeIn, int stay, int fadeOut) {
        ProxyServer.getInstance().createTitle().title(screen()).subTitle(new TextComponent(""))
                .fadeIn(fadeIn).stay(stay).fadeOut(fadeOut).send(player);
    }

    private static BaseComponent screen() {
        TextComponent screen = new TextComponent(FADE_GLYPH);
        screen.setFont(FADE_FONT);
        screen.setColor(ChatColor.WHITE);
        TextComponent logo = new TextComponent(LOGO_GLYPH);
        logo.setFont(LOGO_FONT);
        screen.addExtra(logo);
        return screen;
    }
}
