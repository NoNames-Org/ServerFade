package mc.net.fell.serverfade.paper;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import mc.net.fell.serverfade.api.ScreenFade;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

final class FadeService implements ScreenFade, Listener {

    private static final long TEST_BLACK_TICKS = 20;

    private record Waiting(Runnable action, CompletableFuture<Boolean> result) {
    }

    private final Plugin plugin;
    private final FadeQueue<Waiting> queue = new FadeQueue<>();
    private FadeSettings settings;

    FadeService(Plugin plugin, FadeSettings settings) {
        this.plugin = plugin;
        this.settings = settings;
    }

    void settings(FadeSettings settings) {
        this.settings = settings;
    }

    @Override
    public CompletableFuture<Boolean> fade(Player player, Runnable whileBlack) {
        CompletableFuture<Boolean> result = new CompletableFuture<>();
        UUID id = player.getUniqueId();
        if (queue.add(id, new Waiting(whileBlack, result))) {
            FadeTitles.darken(player, settings);
            Bukkit.getScheduler().runTaskLater(plugin, () -> onBlack(id), settings.fadeInTicks());
        }
        return result;
    }

    private void onBlack(UUID id) {
        List<Waiting> waiting = queue.take(id);
        Player player = Bukkit.getPlayer(id);
        // Пустой список: игрок вышел, onQuit уже всё закрыл.
        if (player == null) {
            waiting.forEach(entry -> entry.result().complete(false));
            return;
        }
        waiting.forEach(entry -> run(player, entry));
        if (!waiting.isEmpty() && player.isOnline()) {
            FadeTitles.reveal(player, settings);
        }
    }

    // Действие чужого плагина: его ошибка не должна оставить игрока с чёрным экраном.
    private void run(Player player, Waiting entry) {
        try {
            entry.action().run();
            entry.result().complete(true);
        } catch (RuntimeException e) {
            plugin.getLogger().log(Level.WARNING, "Действие при затемнении у " + player.getName() + " упало", e);
            entry.result().completeExceptionally(e);
        }
    }

    @Override
    public void darken(Player player) {
        FadeTitles.darken(player, settings);
    }

    @Override
    public void reveal(Player player) {
        FadeTitles.reveal(player, settings);
    }

    @Override
    public boolean isFading(Player player) {
        return queue.has(player.getUniqueId());
    }

    /** {@code /fade test}: затемнить, подержать, проявить. */
    void test(Player player) {
        FadeTitles.darken(player, settings);
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline()) {
                FadeTitles.reveal(player, settings);
            }
        }, settings.fadeInTicks() + TEST_BLACK_TICKS);
    }

    void close() {
        queue.takeAll().forEach(entry -> entry.result().complete(false));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        queue.take(event.getPlayer().getUniqueId()).forEach(entry -> entry.result().complete(false));
    }
}
