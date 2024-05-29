package percolation;

import org.junit.jupiter.api.Test;
import percolation.util.IntQueue;
import percolation.util.Queue;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class IntQueueTest extends QueueTest {
    @Override
    protected Queue createQueue(int cap) {
        return new IntQueue(new int[cap]);
    }

    @Test
    @Override
    void testEnqueue() {
        super.testEnqueue();
        assertEquals(3, q.dequeue());
        assertEquals(1, q.dequeue());
        assertEquals(2, q.dequeue());
        assertEquals(0, q.size());
    }

    @Test
    @Override
    void testFirstAndLastInt() {
        super.testFirstAndLastInt();

        assertEquals(1, q.first());
        assertEquals(3, q.last());

        q.dequeue();
        assertEquals(2, q.first());
    }
}
