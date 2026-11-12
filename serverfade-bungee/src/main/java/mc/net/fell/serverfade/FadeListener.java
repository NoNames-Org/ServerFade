package mc.net.fell.serverfade;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import net.md_5.bungee.api.ServerConnectRequest;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.event.ServerSwitchEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

/**
 * Придерживает переход на другой сервер, пока экран не почернеет: подключение отменяется, запускается затемнение,
 * а потом то же подключение отправляется заново. На новом сервере экран проявляется.
 */
public final class FadeListener implements Listener {

    /** Куда отправить игрока, когда экран почернеет, и откуда (если он ушёл сам, отправка отменяется). */
    private record Pending(String from, ServerInfo target, ServerConnectEvent.Reason reason, boolean retry,
                           int timeout) {
    }

    private static final long TEST_BLACK_MILLIS = 1000;
    /** Таймаут BungeeCord по умолчанию, если запроса нет. */
    private static final int DEFAULT_CONNECT_TIMEOUT = 5000;

    private final Plugin plugin;
    private volatile FadeSettings settings;
    private final Map<UUID, Pending> pending = new ConcurrentHashMap<>();
    /** Наши повторные подключения: слушатель их пропускает. */
    private final Set<UUID> released = ConcurrentHashMap.newKeySet();
    /** Ушли с чёрным экраном: проявляем, когда дойдут до сервера или подключение сорвётся. */
    private final Set<UUID> arriving = ConcurrentHashMap.newKeySet();

    FadeListener(Plugin plugin, FadeSettings settings) {
        this.plugin = plugin;
        this.settings = settings;
    }

    void settings(FadeSettings settings) {
        this.settings = settings;
    }

    /** {@code /serverfade test}: затемнение целиком, без перехода. */
    void test(ProxiedPlayer player) {
        FadeSettings now = settings;
        FadeTitles.darken(player, now);
        plugin.getProxy().getScheduler().schedule(plugin, () -> FadeTitles.reveal(player, settings),
                now.fadeInMillis() + TEST_BLACK_MILLIS, TimeUnit.MILLISECONDS);
    }

    // Последними: другие плагины уже выбрали сервер или отменили переход.
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onConnect(ServerConnectEvent event) {
        UUID id = event.getPlayer().getUniqueId();
        boolean ours = released.remove(id);
        if (event.isCancelled() || ours) {
            return;
        }
        Server current = event.getPlayer().getServer();
        ServerConnectRequest request = event.getRequest();
        String from = current == null ? null : current.getInfo().getName();
        boolean hasCallback = request != null && request.getCallback() != null;
        FadeSettings now = settings;
        if (!FadePolicy.fades(event.getReason(), hasCallback, from, event.getTarget().getName(),
                now.ignoredServers())) {
            return;
        }
        event.setCancelled(true);
        Pending next = new Pending(from, event.getTarget(), event.getReason(),
                request == null || request.isRetry(),
                request == null ? DEFAULT_CONNECT_TIMEOUT : request.getConnectTimeout());
        // Затемнение уже идёт: побеждает последний выбор сервера, заново не затемняем.
        if (pending.put(id, next) == null) {
            FadeTitles.darken(event.getPlayer(), now);
            plugin.getProxy().getScheduler().schedule(plugin, () -> send(id), now.fadeInMillis(),
                    TimeUnit.MILLISECONDS);
        }
    }

    @EventHandler
    public void onSwitch(ServerSwitchEvent event) {
        boolean firstJoin = event.getFrom() == null && settings.revealOnFirstJoin();
        if (arriving.remove(event.getPlayer().getUniqueId()) || firstJoin) {
            FadeTitles.reveal(event.getPlayer(), settings);
        }
    }

    @EventHandler
    public void onDisconnect(PlayerDisconnectEvent event) {
        UUID id = event.getPlayer().getUniqueId();
        pending.remove(id);
        released.remove(id);
        arriving.remove(id);
    }

    private void send(UUID id) {
        Pending next = pending.remove(id);
        ProxiedPlayer player = plugin.getProxy().getPlayer(id);
        if (next == null || player == null) {
            return;
        }
        Server current = player.getServer();
        if (current == null || !current.getInfo().getName().equals(next.from())) {
            // Пока темнело, игрока увели другим путём (кик в лимбо и т.п.): просто проявляем экран.
            FadeTitles.reveal(player, settings);
            return;
        }
        arriving.add(id);
        released.add(id);
        player.connect(ServerConnectRequest.builder()
                .target(next.target())
                .reason(next.reason())
                .retry(next.retry())
                .connectTimeout(next.timeout())
                .callback((result, error) -> {
                    released.remove(id);
                    if (result != ServerConnectRequest.Result.SUCCESS && arriving.remove(id)) {
                        FadeTitles.reveal(player, settings);
                    }
                })
                .build());
    }
}
