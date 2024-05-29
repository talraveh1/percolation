package percolation;

import org.jgrapht.alg.interfaces.ShortestPathAlgorithm;
import org.jgrapht.alg.interfaces.ShortestPathAlgorithm.SingleSourcePaths;
import org.jgrapht.alg.shortestpath.IntVertexDijkstraShortestPath;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;
import percolation.JGT_BackedGraph;
import percolation.SPT;
import percolation.util.Queue;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SPT_JGT implements SPT {
    private final JGT_BackedGraph jgt;
    ShortestPathAlgorithm<Integer, DefaultEdge> spa;
    SingleSourcePaths<Integer, DefaultEdge> spt;

    public SPT_JGT(JGT_BackedGraph jgt) {
        this.jgt = jgt;
        SimpleGraph<Integer, DefaultEdge> bg = jgt.getBackingGraph();
        for (int i = 0; i < jgt.vs; i++) bg.addVertex(i);
    }

    @Override public SPT set(SPT dec) {
        return null; // do nothing
    }

    @Override public void init(long seed) {
        jgt.init(seed);
    }

    @Override public void reset() {
        spa = new IntVertexDijkstraShortestPath<>(jgt.getBackingGraph());
    }

    @Override public void preUpdate(int pn) {
        jgt.update(pn); // add the edges to the backing graph
    }

    @Override
    public void update(int pn) {
        spt = spa.getPaths(jgt.vid(0, 0));
    }

    @Override public boolean neighbor(Queue q, int src, int dst, int dist) {
        return false; // not used
    }

    @Override public void neighbors(Queue q, int src) {
        // not used
    }

    @Override public void enqueue(Queue q, int vid) {
        // not used
    }

    @Override public int dequeue(Queue q) {
        return 0; // not used
    }

    @Override public int dist(int vid) {
        double weight = (int) spt.getWeight(vid);
        assertEquals(weight, Math.floor(weight)); // check that weight is an integer
        return (int) weight;
    }

    @Override public int qsize() {
        return 0; // not used
    }
}
