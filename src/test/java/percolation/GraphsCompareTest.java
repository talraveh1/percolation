package percolation;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import percolation.util.Config;
import percolation.util.ConfigBuilder;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static percolation.Graph.*;

public class GraphsCompareTest extends GraphTest {

    @FunctionalInterface
    protected interface VidAssertion {
        void test(int vid);
    }

    protected void test(Graph g, Graph jgt, int vs, VidAssertion vidAssertion) {
        for (int pn = 0; pn <= 10; pn++) {
            g.update(pn); jgt.update(pn);
            for (int vid = 0; vid < vs; vid++)
                vidAssertion.test(vid);
        }
    }

    protected static Arguments args(int r) {
        Config gc = new ConfigBuilder().radius(r).step(10).visProps("config.vis.properties").build();
        return Arguments.of(new Graph(gc), new JGT_BackedGraph(gc), gc.vs);
    }

    protected static Stream<Arguments> args() {
        return Stream.of(args(3), args(500));
    }

    @BeforeAll
    static void beforeAll() {
        GraphTest.beforeAll();
    }

    @ParameterizedTest @MethodSource("args")
    void testVid(Graph g, Graph jgt, @SuppressWarnings("unused") int vs) {
        for (int x = -g.r; x <= g.r; x++)
            for (int y = -g.r; y <= g.r; y++)
                assertEquals(g.vid(x, y), jgt.vid(x, y));
    }

    @ParameterizedTest @MethodSource("args")
    void testHaveRight(Graph g, Graph jgt, int vs) {
        test(g, jgt, vs, vid -> assertEquals(g.have(R, vid), jgt.have(R, vid)));
    }

    @ParameterizedTest @MethodSource("args")
    void testHaveDown(Graph g, Graph jgt, int vs) {
        test(g, jgt, vs, vid -> assertEquals(g.have(D, vid), jgt.have(D, vid)));
    }

    @ParameterizedTest @MethodSource("args")
    void testHaveLeft(Graph g, Graph jgt, int vs) {
        test(g, jgt, vs, vid -> assertEquals(g.have(L, vid), jgt.have(L, vid)));
    }

    @ParameterizedTest @MethodSource("args")
    void testHaveUp(Graph g, Graph jgt, int vs) {
        test(g, jgt, vs, vid -> assertEquals(g.have(U, vid), jgt.have(U, vid)));
    }

    @ParameterizedTest @MethodSource("args")
    void testIsNotRightmost(Graph g, Graph jgt, int vs) {
        test(g, jgt, vs, vid -> assertEquals(g.isNotAt(R, vid), jgt.isNotAt(R, vid)));
    }

    @ParameterizedTest @MethodSource("args")
    void testIsNotAtBottom(Graph g, Graph jgt, int vs) {
        test(g, jgt, vs, vid -> assertEquals(g.isNotAt(D, vid), jgt.isNotAt(D, vid)));
    }

    @ParameterizedTest @MethodSource("args")
    void testIsNotLeftmost(Graph g, Graph jgt, int vs) {
        test(g, jgt, vs, vid -> assertEquals(g.isNotAt(L, vid), jgt.isNotAt(L, vid)));
    }

    @ParameterizedTest @MethodSource("args")
    void testIsNotAtTop(Graph g, Graph jgt, int vs) {
        test(g, jgt, vs, vid -> assertEquals(g.isNotAt(U, vid), jgt.isNotAt(U, vid)));
    }
}
