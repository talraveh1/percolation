package percolation;

import percolation.util.Config;
import percolation.util.Queue;

import static java.lang.Class.forName;

public interface SPT extends AutoCloseable {
    static SPT load(Config gc, Graph g, String spt) throws Exception {
        return (SPT) forName(spt.indexOf('.') == -1 ? "percolation.spts." + spt : spt)
                .getMethod("create", Config.class, Graph.class).invoke(null, gc, g);
    }
    SPT set(SPT dec);
    void init(long seed);
    void reset();
    void preUpdate(int pn);
    void update(int pn);
    void neighbors(Queue q, int vid);
    boolean neighbor(Queue q, int src, int dst, int dist);
    void enqueue(Queue q, int vid);
    int dequeue(Queue q);
    int dist(int vid);
    int qsize();
    @Override default void close() throws Exception {};
}
