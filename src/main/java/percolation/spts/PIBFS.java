package percolation.spts;

import it.unimi.dsi.fastutil.ints.IntArrays;
import percolation.*;
import percolation.util.Config;
import percolation.util.IntQueue;
import percolation.util.Queue;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static java.lang.Integer.MAX_VALUE;
import static percolation.Graph.*;


public class PIBFS implements SPT {
    protected SPT dec;
    private final ExecutorService es;
    private final CyclicBarrier start, done;
    private final AtomicInteger task = new AtomicInteger(0);
    private final Config gc;
    private final Graph g;
    private final AtomicIntArray dists;
    private final IntQueue aq;
    private final int[][] se = new int[2][]; // sorted edges
    private final int[] sei = new int[2]; // sorted edges indices
    private volatile boolean shutdown = false;

    // static factory
    public static SPT create(Config gc, Graph g) {
        return new PIBFS(gc, g, new int[gc.vs], new int[gc.vs]);
    }

    protected PIBFS(Config gc, Graph g, int[] dists, int[] aq) {
        this.gc = gc;
        this.g = g;
        this.dists = new AtomicIntArray(dists);
        this.aq = new IntQueue(aq);
        dec = this;

        se[R] = IntStream.range(0, dists.length).toArray();
        se[D] = IntStream.range(0, dists.length).toArray();

        // create each thread's personal queue
        int threads = Runtime.getRuntime().availableProcessors() / 2;
        start = new CyclicBarrier(threads + 1);
        done = new CyclicBarrier(threads + 1);
        es = Executors.newFixedThreadPool(threads);
        for (int i = 0; i < threads; i++)
            es.execute(new Worker(new int[gc.vs]));
    }

    @Override public void init(long seed) {
        g.init(seed);
        dec.reset(); // reset the distances
    }

    @Override
    public void reset() {
        assert aq.size() == 0;
        for (int i = 0; i < dists.length(); i++)
            dists.set(i, MAX_VALUE);
        dists.set(g.root, 0);
        Main.print("Sorting %d vertices...", se[R].length + se[D].length);
        IntArrays.parallelQuickSortIndirect(se[R], g.epn[R]);
        IntArrays.parallelQuickSortIndirect(se[D], g.epn[D]);
        Main.clear();
    }

    @Override
    public SPT set(SPT dec) {
        return this.dec = dec;
    }

    @Override public void preUpdate(int pn) {
        g.update(pn); // add new edges
    }

    @Override
    public void update(int pn) {
        if (pn == gc.from) {
            sei[R] = sei[D] = 0;
            dec.reset(); // reset the distances
            g.update(pn);
            dec.enqueue(aq, g.root);
            while (aq.size() != 0)
                dec.neighbors(aq, dec.dequeue(aq));
            aq.clear(); // reset start pos
            while (sei[R] < gc.vs && g.epn[R][se[R][sei[R]]] <= pn)
                sei[R]++;
            while (sei[D] < gc.vs && g.epn[D][se[D][sei[D]]] <= pn)
                sei[D]++;
        } else if (enqueueActivated(pn) > 0) {
            Main.print("Sorting %d vertices... ", aq.end - aq.start);
            IntArrays.parallelQuickSortIndirect(aq.a, dists.a, aq.start, aq.end);
            processAuxQueue();
            Main.clear();
        }
    }

    protected void processAuxQueue() {
        Main.print("processing queue...");

        try {
            task.set(aq.start); // reset the task counter
            start.await(); // start processing the auxiliary queue
            start.reset(); // reset the start barrier
            done.await();  // wait until all tasks are finished
            done.reset();  // reset the done barrier
            assert task.get() >= aq.end;
            aq.clear();
        } catch (InterruptedException | BrokenBarrierException e) {
            throw new RuntimeException(e);
        }

        Main.clear();
    }

    private int enqueueActivated(int pn) {
        enqueueActivated(pn, R);
        enqueueActivated(pn, D);
        return aq.size();
    }

    private void enqueueActivated(int pn, int dir) {
        int[] epn = g.epn[dir], se = this.se[dir];
        int i;
        for (i = sei[dir]; i < se.length && epn[se[i]] <= pn; i++)
            aq.enqueue(se[i], g.next(se[i], dir));
        sei[dir] = i;
    }

    @Override public void neighbors(Queue q, int src) {
        int dist = dists.get(src);
        if (dist++ != MAX_VALUE)
            for (int dir = 0; dir < 4; dir++)
                if (g.isEdgeEnabled(src, dir))
                    neighbor(q, src, g.next(src, dir), dist);
    }

    @Override public boolean neighbor(Queue q, int src, int dst, int dist) {
        int before; // distance before update
        do if ((before = dists.get(dst)) <= dist) return false;
        while (!dists.compareAndSet(dst, before, dist)); // lock-free update
        dec.enqueue(q, dst);
        return true;
    }

    @Override public void enqueue(Queue q, int vid) {
        q.enqueue(vid);
    }

    @Override public int dequeue(Queue q) {
        return q.dequeue();
    }

    @Override public int dist(int vid) {
        return dists.get(vid);
    }

    @Override public int qsize() {
        return aq.size();
    }

    @Override public void close() throws Exception {
        shutdown = true;
        start.await(); // wake up the sleeping workers
        es.shutdown();
        //noinspection ResultOfMethodCallIgnored
        es.awaitTermination(1, TimeUnit.SECONDS);
    }

    private class Worker implements Runnable {
        private static int cnt = 0;
        public final IntQueue q;
        public final int id = Worker.cnt++;

        private Worker(int[] a) {
            q = new IntQueue(a);
        }

        @Override public void run() {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    start.await();
                    if (shutdown) return;
                    int t = task.getAndIncrement();
                    while (t < aq.end) {
                        dec.enqueue(q, aq.a[t]);
                        while (q.size() != 0)
                            dec.neighbors(q, dec.dequeue(q));
                        q.clear(); // reset start pos
                        t = task.getAndIncrement();
                    }
                    aq.clear(); // reset start pos
                    done.await();
                    if (shutdown) return;
                }
            } catch (InterruptedException | BrokenBarrierException e) {
                throw new RuntimeException(e);
            }
        }

        @Override public String toString() {
            return "Worker{" +
                   "id=" + id +
                   '}';
        }
    }

    private record AtomicIntArray(int[] a) {
        private static final VarHandle AA = MethodHandles.arrayElementVarHandle(int[].class);

        public int length() {
            return a.length;
        }

        public int get(int i) {
            return (int) AA.getVolatile(a, i);
        }

        public void set(int i, int newValue) {
            AA.setVolatile(a, i, newValue);
        }

        public boolean compareAndSet(int i, int expectedValue, int newValue) {
            return AA.compareAndSet(a, i, expectedValue, newValue);
        }

        public String toString() {
            int iMax = a.length - 1;
            if (iMax == -1)
                return "[]";

            StringBuilder b = new StringBuilder();
            b.append('[');
            for (int i = 0; ; i++) {
                b.append(get(i));
                if (i == iMax)
                    return b.append(']').toString();
                b.append(',').append(' ');
            }
        }
    }
}
