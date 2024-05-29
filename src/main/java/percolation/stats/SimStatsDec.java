package percolation.stats;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import percolation.util.Config;
import percolation.Sim;
import percolation.SimDec;

import java.sql.SQLException;

public class SimStatsDec extends SimDec {

    private static final Logger log = LoggerFactory.getLogger(SimStatsDec.class);

    private final Stats stats;
    private final boolean full;
    private final int from, step;

    public SimStatsDec(Config gc, Sim that, Stats stats) {
        super(that);

        log.info("Creating sim stats decorator");

        this.stats = stats;
        from = gc.from;
        step = gc.step;
        full = gc.stats.equals("full");
    }

    @Override
    public void run(long seed) throws SQLException {
        stats.starting();
        super.run(seed);
        stats.finished();
        stats.elapsed(true);
    }

    @Override public void trans(boolean done) throws SQLException {
        stats.dump(done);
        super.trans(done);
    }

    @Override
    public void run(long seed, int run) throws SQLException {
        stats.exp().start();
        stats.initializing();
        stats.exps(run + 1, seed);
        stats.running();
        super.run(seed, run);
        stats.exp().end();
        stats.queue(0);
    }

    @Override
    public void exp(boolean close) throws SQLException {
        super.exp(close);
    }

    @Override
    public void run(int pi, int pn) throws SQLException {
        if (full) stats.queue();
        stats.elapsed(true);
        stats.cycle().start();
        super.run(pi, pn);
        stats.mem();
        stats.cycle().end();
    }

    @Override
    public void save(int pi, int vid, int x, int y, long dsum, int dcnt) throws SQLException {
        if (log.isDebugEnabled()) { // avoid unnecessary calculations
            int ad = Math.abs(x) + Math.abs(y);
            double d = (double) (100 * dsum / dcnt) / 100.0; // precision 2
            double s = (double) (100 * dsum / dcnt / ad) / 100.0; // precision 2
            log.debug("Averages for vertex {} in actual dist {} ({},{}): dist:{} stretch:{}", vid, ad, x, y, d, s);
        }
        stats.result(from + pi * step, x, y, dsum, dcnt);
        stats.elapsed();
        super.save(pi, vid, x, y, dsum, dcnt);
    }

}
