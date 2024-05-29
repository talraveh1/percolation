package percolation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import percolation.util.Queue;

import static org.junit.jupiter.api.Assertions.*;

public abstract class QueueTest {
    protected Queue q;

    @BeforeEach
    void setUp() {
        q = createQueue(5); // Adjust capacity as needed
    }

    protected abstract Queue createQueue(int cap);

    void testEnqueue() {
        q.enqueue(3);
        q.enqueue(1);
        q.enqueue(2);
    }

    @Test
    void testSize() {
        assertEquals(0, q.size());

        q.enqueue(1);
        assertEquals(1, q.size());

        q.enqueue(2);
        q.enqueue(3);
        assertEquals(3, q.size());

        q.dequeue();
        assertEquals(2, q.size());

        q.clear();
        assertEquals(0, q.size());
    }

    @Test
    void testFirstAndLastInt() {
        q.enqueue(1);
        q.enqueue(2);
        q.enqueue(3);
    }

    @Test
    void testClear() {
        q.enqueue(1);
        q.enqueue(2);

        q.clear();

        assertEquals(0, q.size());
    }
}
