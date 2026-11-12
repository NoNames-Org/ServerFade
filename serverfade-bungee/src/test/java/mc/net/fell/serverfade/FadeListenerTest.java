package mc.net.fell.serverfade;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import net.md_5.bungee.event.EventHandler;
import org.junit.jupiter.api.Test;

class FadeListenerTest {

    // BungeeCord вызывает обработчики рефлексией из своего пакета. Если класс слушателя не public, каждое событие
    // падает с "Method became inaccessible", и прокси выкидывает игрока.
    @Test
    void handlers_areReachableByTheEventBus() {
        assertTrue(Modifier.isPublic(FadeListener.class.getModifiers()), "класс слушателя должен быть public");
        boolean any = false;
        for (Method method : FadeListener.class.getDeclaredMethods()) {
            if (method.isAnnotationPresent(EventHandler.class)) {
                any = true;
                assertTrue(Modifier.isPublic(method.getModifiers()), method.getName() + " должен быть public");
            }
        }
        assertTrue(any, "не найдено ни одного @EventHandler");
        assertFalse(FadeListener.class.isMemberClass());
    }
}
