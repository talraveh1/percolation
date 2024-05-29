package percolation.stats;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import percolation.util.Config;
import percolation.Dec;
import percolation.SPT;

public class MinSptStatsDec extends Dec {

    private static final Logger log = LoggerFactory.getLogger(MinSptStatsDec.class);

    protected final Stats stats;
    protected final int div;

    public MinSptStatsDec(Config gc, SPT that, Stats stats) {
        super(that);

        log.info("Creating SPT min stats decorator");

        this.stats = stats;
        div = gc.div;
    }

    @Override public void update(int pn) {
        stats.prob(pn);
        stats.spt().start();
        super.update(pn);
        stats.spt().end();
    }
}
