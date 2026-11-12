package mc.net.fell.serverfade;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import net.md_5.bungee.api.event.ServerConnectEvent.Reason;
import org.junit.jupiter.api.Test;

class FadePolicyTest {

    private static final Set<String> NONE = Set.of();

    @Test
    void fades_playerMovedByCommandPluginMessageOrPlugin() {
        assertTrue(FadePolicy.fades(Reason.COMMAND, false, "leaf", "duels", NONE));
        assertTrue(FadePolicy.fades(Reason.PLUGIN_MESSAGE, false, "leaf", "duels", NONE));
        assertTrue(FadePolicy.fades(Reason.PLUGIN, false, "limbo", "leaf", NONE));
    }

    @Test
    void doesNotFade_firstJoinOrSameServer() {
        assertFalse(FadePolicy.fades(Reason.JOIN_PROXY, false, null, "limbo", NONE));
        assertFalse(FadePolicy.fades(Reason.COMMAND, false, null, "leaf", NONE));
        assertFalse(FadePolicy.fades(Reason.COMMAND, false, "leaf", "leaf", NONE));
    }

    @Test
    void doesNotFade_redirectsAfterKickOrCrash() {
        // Старого подключения уже нет: задержка только выкинет игрока.
        assertFalse(FadePolicy.fades(Reason.KICK_REDIRECT, false, "leaf", "limbo", NONE));
        assertFalse(FadePolicy.fades(Reason.SERVER_DOWN_REDIRECT, false, "leaf", "limbo", NONE));
        assertFalse(FadePolicy.fades(Reason.LOBBY_FALLBACK, false, "leaf", "limbo", NONE));
    }

    @Test
    void doesNotFade_whenAnotherPluginWaitsForTheResult() {
        // Отмена придёт в колбэк того плагина как EVENT_CANCEL.
        assertFalse(FadePolicy.fades(Reason.PLUGIN, true, "leaf", "duels", NONE));
    }

    @Test
    void doesNotFade_fromOrToAnIgnoredServer() {
        Set<String> ignored = Set.of("limbo");
        assertFalse(FadePolicy.fades(Reason.PLUGIN, false, "limbo", "leaf", ignored));
        assertFalse(FadePolicy.fades(Reason.COMMAND, false, "Leaf", "LIMBO", ignored));
        assertTrue(FadePolicy.fades(Reason.COMMAND, false, "leaf", "duels", ignored));
    }
}
