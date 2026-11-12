package mc.net.fell.serverfade;

import java.util.Locale;
import java.util.Set;
import net.md_5.bungee.api.event.ServerConnectEvent.Reason;

/** Какие подключения затемнять. Чистая функция, покрыта тестами. */
final class FadePolicy {

    /** Команда {@code /server}, сообщение с бэкенда (NPC, меню) или плагин на прокси. */
    private static final Set<Reason> FADED = Set.of(Reason.COMMAND, Reason.PLUGIN_MESSAGE, Reason.PLUGIN);

    private FadePolicy() {
    }

    /**
     * @param hasCallback другой плагин ждёт результат: задержка означает отмену, а он увидит её как ошибку
     * @param current     сервер игрока, {@code null} при входе на прокси
     * @param ignored     имена серверов в нижнем регистре, на которые и с которых не затемняем
     */
    static boolean fades(Reason reason, boolean hasCallback, String current, String target, Set<String> ignored) {
        return FADED.contains(reason) && !hasCallback && current != null && !current.equals(target)
                && !ignored.contains(current.toLowerCase(Locale.ROOT))
                && !ignored.contains(target.toLowerCase(Locale.ROOT));
    }
}
