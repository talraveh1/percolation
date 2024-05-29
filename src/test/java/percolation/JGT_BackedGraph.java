package percolation;

import it.unimi.dsi.fastutil.ints.IntArrays;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;
import percolation.util.Config;

import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class JGT_BackedGraph extends Graph {

    private final int[] sre; // sorted right edges
    private final int[] sde; // sorted down edges
    private int srei; // sorted right edges array index
    private int sdei; // sorted down edges array index

    private final SimpleGraph<Integer, DefaultEdge> jgt = new SimpleGraph<>(DefaultEdge.class);

    public JGT_BackedGraph(Config gc) {
        super(gc);
        for (int i = 0; i < vs; i++)
            jgt.addVertex(i);
        sre = IntStream.range(0, gc.vs).toArray();
        sde = IntStream.range(0, gc.vs).toArray();
    }

    public SimpleGraph<Integer, DefaultEdge> getBackingGraph() {
        return jgt;
    }

    @SuppressWarnings("UseCompareMethod") @Override public void init(long seed) {
        super.init(seed);
        // remove all edges with probability greater than pn
        for (int vid = 0; vid < vs; vid++) {
            if (isNotAt(R, vid) && epn[R][vid] > pn && jgt.containsEdge(vid, next(vid, R)))
                jgt.removeEdge(vid, next(vid, R));
            if (isNotAt(D, vid) && epn[D][vid] > pn && jgt.containsEdge(vid, next(vid, D)))
                jgt.removeEdge(vid, next(vid, D));
        }
        IntArrays.quickSort(sre, (a, b) -> epn(a, R) > epn(b, R) ? 1 : epn(a, R) < epn(b, R) ? -1 : 0);
        IntArrays.quickSort(sde, (a, b) -> epn(a, D) > epn(b, D) ? 1 : epn(a, D) < epn(b, D) ? -1 : 0);
        srei = sdei = 0;
    }

    @Override public int vid(int x, int y) {
        int vid = super.vid(x, y);
        assertTrue(jgt == null || jgt.vertexSet().contains(vid));
        return vid;
    }

    @Override public void update(int pn) {
        while (srei < vs && epn[R][sre[srei]] <= pn)
            jgt.addEdge(sre[srei], next(sre[srei++], R));
        while (sdei < vs && epn[D][sde[sdei]] <= pn)
            jgt.addEdge(sde[sdei], next(sde[sdei++], D));
    }

    @Override public boolean have(int dir, int vid) {
        boolean res = super.have(dir, vid);
        assertEquals(jgt.containsEdge(vid, next(vid, R)), res);
        return res;
    }
}
