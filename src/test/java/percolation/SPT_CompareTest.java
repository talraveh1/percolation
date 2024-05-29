package percolation;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import percolation.spts.*;
import percolation.stats.FullSptStatsDec;
import percolation.stats.MinSptStatsDec;
import percolation.stats.Stats;
import percolation.util.Config;
import percolation.util.ConfigBuilder;
import percolation.util.RemoteStream;

import java.net.SocketException;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SuppressWarnings("NewClassNamingConvention") public class SPT_CompareTest {

    private static RemoteStream out;

    @FunctionalInterface
    private interface MakeSptArg {
        SPT make(Config gc, Graph g);
    }

    private static Arguments args(long seed, int r, int from, int to, int div, int step, MakeSptArg sptArg) {
        ConfigBuilder cb = new ConfigBuilder();
//        cb.visProps("conf/config.vis.properties"); // uncomment to enable visualization
        Config gc = cb.seed(seed).radius(r).from(from).to(to).div(div).step(step).stats("full").build();
        JGT_BackedGraph jgt = new JGT_BackedGraph(gc);
        Graph g = new Graph(gc);
        jgt.init(seed);
        g.init(seed);
        SPT jgtSpt = new SPT_JGT(jgt);
        SPT spt = sptArg.make(gc, g);
        spt = setStats(gc, spt);
//        spt = new Decs(gc, g, Graph.targets(gc, g)).dec(spt); // uncomment to enable visualization
        return Arguments.of(gc, jgtSpt, spt);
    }

    private static SPT setStats(Config gc, SPT spt) {
        // min stats
        String name = spt.getClass().getSimpleName();
        return out == null || gc.stats.equals("none") ? spt
                : gc.stats.equals("full")
                ? spt.set(new FullSptStatsDec(gc, spt, new Stats(gc, out, 100, name)))
                : spt.set(new MinSptStatsDec(gc, spt, new Stats(gc, out, 100, name)));
    }

    private static Stream<Arguments> args() {

//        return args(BFS::create);
        return Stream.concat(args(BFS::create),
                Stream.concat(args(IBFS::create),
                        args(PIBFS::create)));
    }

    private static Stream<Arguments> args(MakeSptArg create) {
//        return IntStream.range(1, 100).mapToObj(i
//                -> args(i, 2304, 45, 100, 100, 1, create));
                return Stream.of(
//                        args(17, 3, 0, 100, 100, 1, create));
                        IntStream.range(1, 100).mapToObj(i
                                -> args(i, 3, 0, 100, 100, 1, create)),
                        IntStream.range(1, 10).mapToObj(i
                                -> args(i, 10, 0, 100, 100, 1, create)),
                        IntStream.range(0, 5).mapToObj(i -> switch (i) {
                            // avoid eager evaluation creating many threads in the parallel implementation
                            case 0 -> args(2013, 3, 0, 100, 100, 1, create);
                            case 1 -> args(1, 144, 0, 100, 100, 1, create);
                            case 2 -> args(2, 144, 0, 100, 100, 1, create);
                            case 3 -> args(3, 640, 45, 100, 100, 5, create);
                            case 4 -> args(4, 1152, 60, 100, 100, 20, create);
                            case 5 -> args(4, 1152, 48, 100, 100, 1, create);
                            default -> throw new IllegalStateException("Unexpected value: " + i);
                        })).flatMap(s -> s);
    }

    @BeforeAll
    static void beforeAll() {
        try {
            out = new RemoteStream(); // set up a remote stream if possible
        } catch (SocketException ignored) {}
    }

    @AfterAll
    static void afterAll() {
        out.close();
    }

    @ParameterizedTest @MethodSource("args") // @Timeout(value = 30, threadMode = SEPARATE_THREAD)
    void testDist(Config gc, SPT jgtSpt, SPT spt) {
        int pi, pn = 0;
        try {
            jgtSpt.reset();
            spt.reset();
            for (pi = 0, pn = gc.from; pi < gc.ps; pi++, pn += gc.step) { // percentages
                spt.preUpdate(pn);
                spt.update(pn);
                jgtSpt.preUpdate(pn);
                jgtSpt.update(pn);
                for (int vid = 0; vid < gc.vs; vid++)
                    assertDistEquals(gc, jgtSpt, spt, vid, pn); // avoid string formatting if unneeded
            }
        } catch (RuntimeException e) {
            throw new RuntimeException("pn=" + pn + ", " + toString(gc), e);
        }
    }

    private void assertDistEquals(Config gc, SPT jgtSpt, SPT spt, int vid, int pn) {
        if (jgtSpt.dist(vid) != spt.dist(vid))
            assertEquals(jgtSpt.dist(vid), spt.dist(vid),
                    String.format("vid: %d, pn: %d, gc: %s", vid, pn, toString(gc)));
    }

    public String toString(Config gc) {
        return "seed=" + gc.seed + ", r=" + gc.r + ", skip=" + gc.skip;
    }
}
