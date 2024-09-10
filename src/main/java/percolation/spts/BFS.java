package percolation.spts;

import percolation.*;
import percolation.util.Config;
import percolation.util.IntQueue;
import percolation.util.Queue;

import java.util.Arrays;

import static java.lang.Integer.*;

public class BFS implements SPT {
    protected SPT dec;
    protected final Config gc;
    protected final Graph g;
    protected final int[] dists;
    protected final Queue q;

    // static factory
    public static SPT create(Config gc, Graph g) {
        return new BFS(gc, g, new int[gc.vs], new int[gc.vs]);
    }

    protected BFS(Config gc, Graph g, int[] dists, int[] a) {
        this.gc = gc;
        this.g = g;
        this.dists = dists;
        q = new IntQueue(a);
        dec = this;
    }

    @Override
    public SPT set(SPT dec) {
        return this.dec = dec;
    }

    @Override public void init(long seed) {
        g.init(seed);
    }

    @Override
    public void reset() {
        assert q.size() == 0;
        Arrays.fill(dists, MAX_VALUE);
        dists[g.root] = 0;
    }

    @Override
    public void preUpdate(int pn) {
        dec.reset(); // reset the distances
        g.update(pn); // add new edges
    }

    @Override
    public void update(int pn) {
        dec.enqueue(q, g.root);
        while (q.size() != 0)
            dec.neighbors(q, dec.dequeue(q));
        q.clear(); // reset start pos
    }

    @Override public void neighbors(Queue q, int src) {
        int dist = dists[src];
        if (dist++ != MAX_VALUE)
            for (int dir = 0; dir < 4; dir++)
                if (g.isEdgeEnabled(src, dir) && dist < dists[g.next(src, dir)])
                    dec.neighbor(q, src, g.next(src, dir), dist);
    }

    @Override public boolean neighbor(Queue q, int src, int dst, int dist) {
        dists[dst] = dist;
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
        return dists[vid];
    }

    @Override public int qsize() {
        return q.size();
    }
}
