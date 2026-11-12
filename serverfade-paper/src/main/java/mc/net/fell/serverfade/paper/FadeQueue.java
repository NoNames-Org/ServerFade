package mc.net.fell.serverfade.paper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Что ждёт, пока экран игрока почернеет. Первая запись игрока запускает затемнение, следующие присоединяются к нему.
 * Только главный поток.
 */
final class FadeQueue<T> {

    private final Map<UUID, List<T>> waiting = new HashMap<>();

    /** @return {@code true}, если это первая запись игрока и нужно начинать затемнение */
    boolean add(UUID player, T entry) {
        boolean first = !waiting.containsKey(player);
        waiting.computeIfAbsent(player, id -> new ArrayList<>()).add(entry);
        return first;
    }

    /** Забирает записи игрока в порядке добавления. */
    List<T> take(UUID player) {
        List<T> entries = waiting.remove(player);
        return entries == null ? List.of() : List.copyOf(entries);
    }

    boolean has(UUID player) {
        return waiting.containsKey(player);
    }

    /** Забирает всё, для выключения плагина. */
    List<T> takeAll() {
        List<T> all = new ArrayList<>();
        waiting.values().forEach(all::addAll);
        waiting.clear();
        return all;
    }
}
