package mc.net.fell.serverfade.paper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class FadeQueueTest {

    private final UUID alice = UUID.randomUUID();
    private final UUID bob = UUID.randomUUID();

    @Test
    void onlyTheFirstEntryStartsAFade() {
        FadeQueue<String> queue = new FadeQueue<>();

        assertTrue(queue.add(alice, "a1"));
        assertFalse(queue.add(alice, "a2"));
        assertTrue(queue.add(bob, "b1"));
    }

    @Test
    void takeReturnsEntriesInOrderAndForgetsThePlayer() {
        FadeQueue<String> queue = new FadeQueue<>();
        queue.add(alice, "a1");
        queue.add(alice, "a2");

        assertEquals(List.of("a1", "a2"), queue.take(alice));
        assertFalse(queue.has(alice));
        assertEquals(List.of(), queue.take(alice));
        assertTrue(queue.add(alice, "a3"), "после завершения затемнения можно начать новое");
    }

    @Test
    void takeAllEmptiesTheQueue() {
        FadeQueue<String> queue = new FadeQueue<>();
        queue.add(alice, "a1");
        queue.add(bob, "b1");

        assertEquals(2, queue.takeAll().size());
        assertFalse(queue.has(alice) || queue.has(bob));
    }
}
