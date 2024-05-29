package percolation;

import percolation.util.Queue;

public class Dec implements SPT {

    private final SPT that;

    public Dec(SPT that) {
        this.that = that;
    }

    @Override public SPT set(SPT dec) {
        return that.set(dec);
    }

    @Override public void init(long seed) {
        that.init(seed);
    }

    @Override public void reset() {
        that.reset();
    }

    @Override public void preUpdate(int pn) {
        that.preUpdate(pn);
    }

    @Override public void update(int pn) {
        that.update(pn);
    }

    @Override public boolean neighbor(Queue q, int src, int dst, int dist) {
        return that.neighbor(q, src, dst, dist);
    }

    @Override public void neighbors(Queue q, int src) {
        that.neighbors(q, src);
    }

    @Override public void enqueue(Queue q, int vid) {
        that.enqueue(q, vid);
    }

    @Override public int dequeue(Queue q) {
        return that.dequeue(q);
    }

    @Override public int dist(int vid) {
        return that.dist(vid);
    }

    @Override public int qsize() {
        return that.qsize();
    }

    @Override public void close() throws Exception {
        that.close();
    }

    @Override public String toString() {
        return that.toString();
    }
}
