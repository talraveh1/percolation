package percolation;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import percolation.util.Config;
import percolation.util.ConfigBuilder;

import java.util.Arrays;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.lang.Integer.*;
import static org.junit.jupiter.api.Assertions.*;
import static percolation.Graph.*;

public class FixedGraphTest extends GraphTest {

    private final int MV = MAX_VALUE;
    private final int[][] ep = { {
            //   1   2   3   4   5   6   7   8   9  10  11  12  13  14  15  16  17 18  19  20  21 22  23  24
            MV, MV, MV, MV, 41, 60, 30, MV, 50, 64, 81, MV, 30, 31, 38, MV, 55, 71, 7, 71, 35, 20, 6, 20, 78
    }, {
            97, 5, 21, MV, 77, 33, 45, MV, 4, 19, 43, MV, MV, MV, MV, MV, 33, 58, 88, 48, 56, 48, 60, 50, 65
    }, {
            MV, 55, 71, 7, MV, 71, 35, 20, MV, 6, 20, 78, MV, MV, MV, MV, 41, 60, 30, 50, 64, 81, 30, 31, 38
    }, {
            MV, MV, MV, MV, MV, 33, 58, 88, MV, 48, 56, 48, MV, 60, 50, 65, 97, 5, 21, 77, 33, 45, 4, 19, 43
    } };


    public static Stream<Arguments> args1() {
        Config gc = new ConfigBuilder().radius(3).build();
        return Stream.of(
                Arguments.of(new Graph(gc)),
                Arguments.of(new JGT_BackedGraph(gc)));
    }

    public static Stream<Arguments> args2() {
        int[] arr = { 18, 12, 6, 13, 20, 5, 9, 17, 19 }; // sorted right edges
        //          { 7,  30,30, 31, 35,60,64, 71, 71 }; // sorted right edge values
        int[] fnd = { 0, 7, 10, 29, 30, 31, 31, 32, 64, 70, 71, 80 };
        int[] frm = { 0, 0, 0, 1, 1, 3, 5, 0, 2, 0, 0, 8 };
        int[] exp = { 0, 1, 1, 1, 3, 4, 5, 4, 7, 7, 9, 9 };
        Config gc = new ConfigBuilder().radius(3).build();
        Graph g = new Graph(gc);
        return IntStream.range(0, fnd.length).mapToObj(i -> Arguments.of(g, exp[i], arr, R, frm[i], fnd[i]));
    }

    @ParameterizedTest @MethodSource("args1")
    public void resetRetainsRandomEdgeProbabilitiesAccordingToSeed(Graph g) {
        g.init(1L);
        assertTrue(Arrays.deepEquals(ep, g.epn));
    }

    @ParameterizedTest @MethodSource("args1")
    public void rightEdgeProbabilitiesEqualLeftEdgeProbabilityOfRightVertex(Graph g) {
        g.init(1L);
        for (int vid = 0; vid < g.vs; vid++)
            if (g.isNotAt(R, vid))
                assertEquals(g.epn[R][vid], g.epn[L][g.next(vid, R)]);
    }

    @ParameterizedTest @MethodSource("args1")
    public void downEdgeProbabilitiesEqualUpEdgeProbabilityOfDownVertex(Graph g) {
        g.init(1L);
        for (int vid = 0; vid < g.vs; vid++)
            if (g.isNotAt(D, vid))
                assertEquals(g.epn[D][vid], g.epn[U][g.next(vid, D)]);
    }

    @ParameterizedTest @MethodSource("args1")
    void resetChangesEdgeProbabilitiesTest(Graph g) {
        g.init(1L);
        g.init(2L);
        for (int dir = 0; dir < ep.length; dir++)
            for (int vid = 0; vid < ep[dir].length; vid++)
                if (ep[dir][vid] == MAX_VALUE)
                    assertEquals(MAX_VALUE, g.epn[dir][vid]);
                else
                    assertNotEquals(ep[dir][vid], g.epn[dir][vid]);
    }

    @ParameterizedTest @MethodSource("args2")
    void findGreater(Graph g, int exp, int[] a, int dir, int frm, int fnd) {
        g.init(1L);
        int res = g.findGreater(a, dir, frm, fnd);
        assertEquals(exp, res);
    }
}
