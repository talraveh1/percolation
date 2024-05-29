package percolation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import percolation.db.Database;
import percolation.db.SimDbDec;
import percolation.stats.FullSptStatsDec;
import percolation.stats.MinSptStatsDec;
import percolation.stats.SimStatsDec;
import percolation.stats.Stats;
import percolation.util.Config;
import percolation.vis.VisDec;
import percolation.vis.VisConfig;
import percolation.vis.VisEngine;

import java.sql.SQLException;

public class Decs implements AutoCloseable {

    private static final Logger log = LoggerFactory.getLogger(Decs.class);

    private final Config gc;
    private final Graph g;
    private final int[] targets;
    private final Database db;
    public final Stats stats;
    private final VisEngine vis;

    public Decs(Config gc, Graph g, int[] targets) {
        this.gc = gc;
        this.g = g;
        this.targets = targets;
        try {
            db = gc.dbUrl == null ? null : new Database(gc, gc.dbUrl);
            stats = gc.stats.equals("none") ? null : new Stats(gc);
            vis = gc.vis ? new VisEngine(gc, new VisConfig(gc.visProps), g, targets) : null;
        } catch (SQLException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public Sim dec(Sim sim) {

        log.debug("Decorating sim object");

        if (gc.stats.equals("full") || gc.stats.equals("min"))
            sim = sim.set(new SimStatsDec(gc, sim, stats));
        if (db != null)
            sim = sim.set(new SimDbDec(gc, sim, db, g, targets));
        return sim;
    }

    public SPT dec(SPT spt) {
        if (gc.stats.equals("full"))
            spt = spt.set(new FullSptStatsDec(gc, spt, stats));
        else if (gc.stats.equals("min"))
            spt = spt.set(new MinSptStatsDec(gc, spt, stats));
        if (gc.vis)
            spt = spt.set(new VisDec(spt, vis));
        return spt;
    }

    @Override public void close() throws Exception {
        if (db != null)
            db.close();
        if (stats != null)
            stats.close();
        if (vis != null)
            vis.close();
    }
}
