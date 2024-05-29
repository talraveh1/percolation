package percolation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import percolation.util.Config;

import java.util.Objects;
import java.util.Random;

import static java.lang.Integer.*;


public class Graph {

    private static final Logger log = LoggerFactory.getLogger(Graph.class);

    public static final int INIT = MAX_VALUE - 1;

    public final int root;
    protected final Config gc;
    private final Random rng = new Random();

    public static final int X = 0, Y = 1, R = 0, D = 1, L = 2, U = 3; // do not change this line!
    public final int r, vs; // radius and number of vertices

    public final int[][] epn; // edge probability numerators
    private final int[][] ax; // vid to x, y coordinate mapping
    protected final int[][] xy; // x, y coordinates to vid mapping
    public final int[][] next = new int[4][]; // vid to right, down, left, up vertex mapping
    public int pn; // current probability numerator

    public Graph(Config gc) {

        log.info("Creating graph object");

        this.gc = gc;

        r = gc.r;
        vs = gc.vs;
        epn = new int[4][vs];
        ax = new int[2][vs];
        xy = initXy(r, vs, gc.outer);

        for (int vid = 0; vid < vs; vid++) {
            int isInner = vid / gc.outer;
            int idx = vid - isInner * gc.outer;
            int cols = r + 1 - isInner;
            // x, y is are the actual VertexStyle coordinates
            int xArr = idx % cols, yArr = idx / cols;
            // xArr, yArr are the VertexStyle coordinates in the array (and the screen)
            ax[X][vid] = xArr - yArr;
            ax[Y][vid] = cols - xArr - yArr - 1;
            xy[ax[X][vid] + r][ax[Y][vid] + r] = vid;
        }

        next[R] = initDirCacheVars(R);
        next[D] = initDirCacheVars(D);
        next[L] = initDirCacheVars(L);
        next[U] = initDirCacheVars(U);

        root = vid(0, 0);

        // fill the edge probability numerators arrays with INIT for potential edges and MAX_VALUE for non-edges
        for (int vid = 0; vid < vs; vid++) {
            epn[R][vid] = isNotAt(R, vid) ? INIT : MAX_VALUE;
            epn[D][vid] = isNotAt(D, vid) && (x(vid) + y(vid)) % (gc.skip + 1) == 0 ? INIT : MAX_VALUE;
        }

        // copy the edge probability numerators from right and down to left and up
        for (int vid = 0; vid < vs; vid++) {
            epn[L][vid] = isNotAt(L, vid) ? epn[R][next(vid, L)] : MAX_VALUE;
            epn[U][vid] = isNotAt(U, vid) ? epn[D][next(vid, U)] : MAX_VALUE;
        }
    }

    private static int log2(int x) {
        return (int) Math.ceil((Math.log(x) / Math.log(2)));
    }

    public static int[] targets(Config gc, Graph g) {

        log.info("Creating targets array");

        int[] ts;
        if (Objects.equals(gc.targets, "none")) {
            ts = new int[0];
        } else if (Objects.equals(gc.targets, "all")) {
            int log2d = log2(gc.dist > 0 && gc.dist < gc.r ? gc.dist : gc.r) - 1;
            ts = new int[(1 << (log2d + 3)) - 8];
            for (int dist = 2, k = 0, x, y; dist <= (1 << log2d); dist <<= 1) {
                for (x = -dist, y = 0; x < 0; x++, y++)
                    ts[k++] = g.vid(x, y);
                for (x = 0, y = dist; y > 0; x++, y--)
                    ts[k++] = g.vid(x, y);
                for (x = dist, y = 0; x > 0; x--, y--)
                    ts[k++] = g.vid(x, y);
                for (x = 0, y = -dist; y < 0; x--, y++)
                    ts[k++] = g.vid(x, y);
            }
        } else if (Objects.equals(gc.targets, "far")) {
            int d = (int) Math.pow(2, log2(gc.r) - 1);
            ts = new int[4 * d];
            int k = 0;
            for (int x = 0, y = d; y > 0; ++x, --y) ts[k++] = g.vid(x, y);
            for (int x = d, y = 0; x > 0; --x, --y) ts[k++] = g.vid(x, y);
            for (int x = 0, y = -d; y < 0; --x, ++y) ts[k++] = g.vid(x, y);
            for (int x = -d, y = 0; x < 0; ++x, ++y) ts[k++] = g.vid(x, y);
        } else {
            ts = gc.targetList;
        }

        return ts;
    }

    public void init(long seed) {

        log.info("Initializing graph object with seed {}", seed);
        
        pn = gc.from;
        rng.setSeed(seed);
        // fill the edge probability numerators array with random values from 0 to DIV
        for (int vid = 0; vid < vs; vid++) {
            if (epn[R][vid] != MAX_VALUE) epn[R][vid] = rng.nextInt(gc.div + 1);
            if (epn[D][vid] != MAX_VALUE) epn[D][vid] = rng.nextInt(gc.div + 1);
        }
        // copy the right and down numerators to the corresponding left and up numerators
        for (int vid = 0; vid < vs; vid++) {
            epn[L][vid] = isNotAt(L, vid) ? epn[R][next(vid, L)] : MAX_VALUE;
            epn[U][vid] = isNotAt(U, vid) ? epn[D][next(vid, U)] : MAX_VALUE;
        }
    }

    public int vid(int x, int y) {
        return xy[x + r][y + r];
    }

    public void update(int pn) {
        this.pn = pn;
    }

    public boolean have(int dir, int vid) {
        return epn[dir][vid] <= pn;
    }

    public boolean isNotAt(int dir, int vid) {
        return switch (dir) {
            case R -> ax[X][vid] < r - Math.abs(ax[Y][vid]);
            case D -> ax[Y][vid] > Math.abs(ax[X][vid]) - r;
            case L -> ax[X][vid] > Math.abs(ax[Y][vid]) - r;
            case U -> ax[Y][vid] < r - Math.abs(ax[X][vid]);
            default -> throw new IllegalArgumentException("dir must be one of R, D, L, U");
        };
    }

    public int next(int vid, int dir) {
        return next[dir][vid];
    }

    protected int[][] initXy(int r, int vs, int outer) {
        int[][] xy = new int[2 * r + 1][2 * r + 1];
        for (int vid = 0; vid < vs; vid++) {
            int isInner = vid / outer;
            int idx = vid - isInner * outer;
            int cols = r + 1 - isInner;
            // x, y is are the actual VertexStyle coordinates
            int xArr = idx % cols, yArr = idx / cols;
            // xArr, yArr are the VertexStyle coordinates in the array (and the screen)
            ax[X][vid] = xArr - yArr;
            ax[Y][vid] = cols - xArr - yArr - 1;
            xy[ax[X][vid] + r][ax[Y][vid] + r] = vid;
        }
        return xy;
    }

    protected int[] initDirCacheVars(int dir) {
        int[] arr = new int[vs];
        for (int vid = 0; vid < vs; vid++)
            arr[vid] = isNotAt(dir, vid) ? xy[ax[X][vid] + r - (dir - 1) % 2][ax[Y][vid] + r - (2 - dir) % 2] : -1;
        return arr;
    }

    public int dir(int src, int dst) {
        for (int dir = 0; dir < 4; dir++)
            if (next[dir][src] == dst)
                return dir;
        throw new IllegalArgumentException(String.format("%d and %d are not neighbors", src, dst));
    }

    public int epn(int vid, int dir) {
        return epn[dir][vid];
    }

    public int pn() {
        return pn;
    }

    public boolean isEdgeEnabled(int src, int dir) {
        return epn(src, dir) <= pn();
    }

    public int x(int vid) {
        return ax[X][vid];
    }

    public int y(int vid) {
        return ax[Y][vid];
    }

    public int findGreater(int[] a, int dir, int from, int val) {
        int to = a.length - 1;
        if (epn(a[to], dir) <= val)
            return to + 1;
        int mid = (from + to) >>> 1;
        while (from < to) {
            if (epn(a[mid], dir) <= val)
                from = mid + 1;
            else if (mid > from && epn(a[mid - 1], dir) > val)
                to = mid - 1;
            else
                break;
            mid = (from + to) >>> 1;
        }
        return mid;
    }
}
