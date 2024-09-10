package percolation.spts;

import it.unimi.dsi.fastutil.ints.IntArrays;
import percolation.*;
import percolation.util.Config;
import percolation.util.IntQueue;

import java.util.stream.IntStream;

import static percolation.Graph.*;


public class IBFS extends BFS {
    protected final IntQueue aq; // auxiliary queue
    protected final int[][] se = new int[2][]; // sorted edges
    protected final int[] sei = new int[2]; // sorted edges indices

    // static factory
    public static SPT create(Config gc, Graph g) {
        return new IBFS(gc, g, new int[gc.vs], new int[gc.vs], new int[gc.vs]);
    }

    protected IBFS(Config gc, Graph g, int[] dists, int[] a, int[] aq) {
        super(gc, g, dists, a);
        this.aq = new IntQueue(aq); // auxiliary queue (on same array as the main queue)
        se[R] = IntStream.range(0, dists.length).toArray();
        se[D] = IntStream.range(0, dists.length).toArray();
    }

    @Override public void init(long seed) {
        super.init(seed);
        dec.reset(); // reset the distances
    }

    @Override
    public void reset() {
        super.reset();
        Main.print("Sorting %d vertices...", se[R].length + se[D].length);
        IntArrays.parallelQuickSortIndirect(se[R], g.epn[R]);
        IntArrays.parallelQuickSortIndirect(se[D], g.epn[D]);
        Main.clear();
    }

    @Override public void preUpdate(int pn) {
        g.update(pn); // add new edges
    }

    @Override
    public void update(int pn) {
        if (pn == gc.from) {
            sei[R] = sei[D] = 0;
            super.update(pn); // first update after reset we use the regular BFS algorithm
            while (sei[R] < gc.vs && g.epn[R][se[R][sei[R]]] <= pn) sei[R]++;
            while (sei[D] < gc.vs && g.epn[D][se[D][sei[D]]] <= pn) sei[D]++;
        } else if (enqueueActivated(pn) > 0) {
            Main.print("Sorting %d vertices... ", aq.end - aq.start);
            IntArrays.parallelQuickSortIndirect(aq.a, dists, aq.start, aq.end);
            processAuxQueue();
            Main.clear();
        }
    }

    protected void processAuxQueue() {
        Main.print("processing queue...");
        while (aq.size() != 0) {
            q.enqueue(dec.dequeue(aq));
            while (q.size() != 0)
                dec.neighbors(q, dec.dequeue(q));
            q.clear(); // reset start pos
        }
        aq.clear(); // reset start pos
    }

    private int enqueueActivated(int pn) {
        enqueueActivated(pn, R);
        enqueueActivated(pn, D);
        return aq.size();
    }

    // enqueue vertices of edges that were activated in the last update. for each activated edge (v1, v2),
    // if dist(v2) > dist(v1) + 1 (w.l.o.g.) dist(v2) is updated to dist(v1) + 1 and v2 is enqueued
    // in order to update the rest of the graph
    protected void enqueueActivated(int pn, int dir) {
        //noinspection DuplicatedCode // cba to extract to a new util class or make SPT abstract
        int[] epn = g.epn[dir], se = this.se[dir];
        int i;
        for (i = sei[dir]; i < se.length && epn[se[i]] <= pn; i++)
            aq.enqueue(se[i], g.next(se[i], dir));
        sei[dir] = i;
    }
}
