package percolation.vis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import percolation.util.Queue;
import percolation.SPT;

public class VisDec extends percolation.Dec {

    private static final Logger log = LoggerFactory.getLogger(VisDec.class);

    public final VisEngine vis;

    public VisDec(SPT that, VisEngine vis) {
        super(that);

        log.info("Creating SPT visualizer decorator");

        this.vis = vis;
    }

    @Override public void init(long seed) {
        super.init(seed);
        vis.beginUpdate(true);
        try {
            vis.init();
        } finally {
            vis.endUpdate(true);
        }
    }

    @Override public void reset() {
        super.reset();
        vis.beginUpdate(true);
        try{
            vis.reset();
        } finally {
            vis.endUpdate(true);
        }
    }

    @Override public void preUpdate(int pn) {
        vis.beginUpdate(true);
        try {
            vis.update(pn);
            super.preUpdate(pn);
        } finally {
            vis.endUpdate(true);
        }
    }

    @Override public void update(int pn) {
        vis.beginUpdate(false);
        try {
            super.update(pn);
        } finally {
            vis.endUpdate(false);
        }
        vis.delay(pn);
    }

    @Override public void neighbors(Queue q, int src) {
        vis.beginUpdate(false);
        try {
            super.neighbors(q, src);
            vis.setVertexProcessed(src, dist(src));
        } finally {
            vis.endUpdate(false);
        }
    }

    @Override public boolean neighbor(Queue q, int src, int dst, int dist) {
        boolean res = super.neighbor(q, src, dst, dist);
        if (res) vis.setVertexInTree(dst, src, dist);
        return res;
    }

    @Override public void enqueue(Queue q, int vid) {
        super.enqueue(q, vid);
        vis.setVertexEnq(vid, dist(vid));
    }

    @Override public int dequeue(Queue q) {
        int vid = super.dequeue(q);
        vis.setVertexDeq(vid, dist(vid));
        return vid;
    }
}
