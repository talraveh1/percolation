package percolation.stats;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import percolation.util.Config;
import percolation.util.Queue;
import percolation.SPT;

public class FullSptStatsDec extends MinSptStatsDec {

    private static final Logger log = LoggerFactory.getLogger(FullSptStatsDec.class);

    public FullSptStatsDec(Config gc, SPT that, Stats stats) {
        super(gc, that, stats);

        log.info("Creating SPT full stats decorator");
    }

    @Override public boolean neighbor(Queue q, int src, int dst, int dist) {
        if (dist != Integer.MAX_VALUE)
            stats.dist(dist + 1);
        return super.neighbor(q, src, dst, dist);
    }

    @Override public int dequeue(Queue q) {
        int size = qsize();
        stats.queue(size);
        stats.elapsed(size == 0);
        return super.dequeue(q);
    }
}
