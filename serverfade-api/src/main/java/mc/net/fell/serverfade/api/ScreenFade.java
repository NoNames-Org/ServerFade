package mc.net.fell.serverfade.api;

import java.util.concurrent.CompletableFuture;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

/**
 * Затемнение экрана внутри одного Paper-сервера: тот же чёрный экран с логотипом, что показывает ServerFade на
 * BungeeCord при переходе между серверами. Удобно оборачивать телепорт на арену, смену мира и т.п.
 *
 * <p>Добавьте {@code ServerFade} в {@code depend} или {@code softdepend}. Все методы вызываются только из главного
 * потока.
 */
public interface ScreenFade {

    /** @return реализация или {@code null}, если ServerFade на этом сервере не включён */
    static @Nullable ScreenFade get() {
        return Bukkit.getServicesManager().load(ScreenFade.class);
    }

    /**
     * Затемняет экран, когда он стал чёрным, выполняет {@code whileBlack} в главном потоке и проявляет обратно.
     * Если затемнение у игрока уже идёт, действие присоединяется к нему и выполняется в тот же момент, по порядку
     * вызовов.
     *
     * @return {@code true}, когда действие выполнено; {@code false}, если игрок вышел раньше и действие пропущено.
     *         Исключение из действия завершает future с ошибкой, экран при этом всё равно проявляется
     */
    CompletableFuture<Boolean> fade(Player player, Runnable whileBlack);

    /** Затемняет и держит чёрным (не дольше {@code hold-ticks} из конфига), пока не вызовут {@link #reveal}. */
    void darken(Player player);

    /** Сразу чёрный экран, затем плавное проявление. */
    void reveal(Player player);

    /** Ждёт ли {@link #fade} этого игрока, пока экран станет чёрным. */
    boolean isFading(Player player);
}
