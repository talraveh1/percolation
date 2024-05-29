package percolation.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import percolation.Main;

public class IntQueue implements Queue {

    private static final Logger log = LoggerFactory.getLogger(IntQueue.class);

    public final int[] a;
    public int start;
    public int end;

    public IntQueue(int[] a) {

        log.debug("Creating IntQueue object");

        this.a = a;
    }

    @Override
    public int dequeue() {
        return a[start++];
    }

    @Override
    public void enqueue(int x) {
        a[end++] = x;
    }

    @Override
    public void enqueue(int x, int y) {
        a[end++] = x;
        a[end++] = y;
    }

    @Override
    public int first() {
        return a[start];
    }

    @Override
    public int last() {
        return a[end - 1];
    }

    @Override
    public void clear() {
        start = end = 0;
    }

    @Override
    public int[] arr() {
        return a;
    }

    @Override
    public int start() {
        return start;
    }

    @Override
    public int end() {
        return end;
    }

    @Override
    public int size() {
        return end - start;
    }

    @Override
    public String toString() {
        return Main.indArrToString(a, null, start, end, 3);
    }
}
