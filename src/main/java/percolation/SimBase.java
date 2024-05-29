package percolation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import percolation.db.Store;
import percolation.util.Config;

import java.sql.SQLException;
import java.util.Arrays;

import static java.lang.Integer.*;


public class SimBase implements Sim {

    private static final Logger log = LoggerFactory.getLogger(SimBase.class);

    private final Config gc;
    protected Sim dec;
    private Store store;
    private final SPT spt;
    private final int[] targets;

    @Override
    public Sim set(Sim dec) {

        log.info("Setting sim object: {}", dec);

        if (gc.save == 0)
            store = dec;
        return this.dec = dec;
    }

    @Override
    public void exp(long seed) {
        // do nothing
    }

    @Override
    public void exp(boolean close) {
        // do nothing
    }

    public SimBase(Config gc, SPT spt, int[] targets) {
        this.spt = spt;
        this.targets = targets;
        this.gc = gc;
        dec = this;

        if (gc.save != 0)
            store = new Aggs(new long[gc.ps][targets.length], new int[gc.ps][targets.length]);
    }

    @Override
    public void run(long seed) throws SQLException {

        log.info("Running simulation with seed {}", seed);

        for (int run = 0; run < gc.runs; run++)
            dec.run(seed++, run);
        if (gc.dbUrl != null && (gc.save == -2 || gc.save > 0))
            dec.dump(true);
    }

    @Override
    public void run(long seed, int run) throws SQLException {
        dec.exp(seed); // create a new experiment in the db
        spt.init(seed); // initialize the graph and the spt class
        log.info("Simulation run {} with seed {} started", run + 1, seed);
        for (int pi = 0, pn = gc.from; pi < gc.ps; pi++, pn += gc.step)
            dec.run(pi, pn); // pi = probability index, pn = probability numerator
        if (gc.dbUrl != null && gc.save == -1)
            dec.dump(true);
        dec.exp(true);
    }

    @Override
    public void run(int pi, int pn) throws SQLException {
        log.trace("Probability numerator: {}", pn);
        spt.preUpdate(pn);
        spt.update(pn);
        for (int tid = 0, vid; tid < targets.length; tid++) // tid = target id
            if (spt.dist(vid = targets[tid]) != MAX_VALUE)
                store.save(pi, tid, spt.dist(vid), 1);
        if (gc.dbUrl != null && gc.save > 0)
            dec.dump(false);
    }

    @Override public void dump(boolean force) throws SQLException {
        store.dump(force);
    }

    public class Aggs implements Store {
        private final long[][] tds; // target distances sum
        private final int[][] tdc; // target distances count
        private int calls, mod = 1;
        private long last = System.currentTimeMillis();

        public Aggs(long[][] tds, int[][] tdc) {
            this.tds = tds;
            this.tdc = tdc;
        }

        @Override
        public void save(int pi, int tid, long dsum, int dcnt) {
            tds[pi][tid] += dsum;
            tdc[pi][tid] += dcnt;
        }

        @Override
        public void dump(boolean force) throws SQLException {
            if (gc.save != 0 && (calls++ % mod == 0 || force)) {
                if (System.currentTimeMillis() - last >= gc.save || force)
                    last = dump();
                else
                    mod++; // increase the mod to reduce the number of calls
            }
        }

        private long dump() throws SQLException {

            log.info("Database dump started");

            dec.trans(false);
            for (int pi = 0; pi < tds.length; pi++) {
                for (int tid = 0; tid < tds[pi].length; tid++)
                    if (tdc[pi][tid] > 0)
                        dec.save(pi, tid, tds[pi][tid], tdc[pi][tid]);
                Arrays.fill(tds[pi], 0);
                Arrays.fill(tdc[pi], 0);
            }
            dec.exp(false);
            dec.trans(true);

            log.info("Database dump finished");

            return System.currentTimeMillis();
        }
    }
}

