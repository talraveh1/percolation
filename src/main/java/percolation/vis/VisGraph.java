package percolation.vis;

import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGeometry;
import com.mxgraph.view.mxGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import percolation.util.ProbFormatter;

import java.util.*;

import static java.lang.Integer.MAX_VALUE;
import static percolation.vis.VisStyles.*;

class VisGraph extends mxGraph {

    private static final Logger log = LoggerFactory.getLogger(VisGraph.class);

    private final VisConfig vc;
    private final mxCell rc; // root cell
    private final ProbFormatter pf;
    private final TreeStrategy tree;
    private final Set<Edge> enabledEdges, disabledEdges;

    VisGraph(VisConfig vc, ProbFormatter pf, TreeStrategy tree, int vs) {
        this.vc = vc; this.pf = pf; this.tree = tree;
        rc = (mxCell) getDefaultParent();
        enabledEdges = new HashSet<>(vs * 2);
        disabledEdges = new HashSet<>(vs * 2);
    }

    void addVertex(int vid, int x, int y, VertexStyles vss, int xScr, int yScr, int size) {
        addCell(new Vertex(vid, x, y, vss, xScr, yScr, size), rc);
    }

    void addEdge(int v1, int v2) {
        Edge e = new Edge(v1, v2);
        super.addEdge(e, rc, getVertex(v1), getVertex(v2), null);
        disabledEdges.add(e);
    }

    public void init() {
        Iterator<Edge> iterator = enabledEdges.iterator();
        while (iterator.hasNext()) {
            Edge e = iterator.next();
            iterator.remove();
            e.setDisabled();
            disabledEdges.add(e);
        }
    }

    public void update(int gpn) {
        log.info("Enabling all edges with pn <= {}", gpn);
        Iterator<Edge> iterator = disabledEdges.iterator();
        while (iterator.hasNext()) {
            Edge e = iterator.next();
            if (e.pn <= gpn) {
                iterator.remove();
                e.setEnabled();
                enabledEdges.add(e);
            }
        }
    }

    Vertex getVertex(int vid) {
        return (Vertex) rc.getChildAt(vid);
    }

    Edge getEdge(Vertex v1, Vertex v2) {
        return (Edge) getEdgesBetween(v1, v2)[0];
    }

    Edge getEdge(int v1, int v2) {
        return getEdge(getVertex(v1), getVertex(v2));
    }

    public void beginUpdate() {
        model.beginUpdate();
    }

    public void endUpdate() {
        model.endUpdate();
    }

    class Vertex extends mxCell {

        private final int vid, x, y;
        private final VertexStyles vss;
        private VertexStyle vs;
        private Vertex parent;
        private int dist;
        private final Set<Vertex> children = new HashSet<>(3);

        Vertex(int vid, int x, int y, VertexStyles vss, int xScr, int yScr, int size) {
            super(makeLabel(vid, x, y, vss), new mxGeometry(xScr, yScr, size, size), vss.def.str);
            this.vid = vid; this.x = x; this.y = y; this.vss = vss; vs = vss.def;
            setId(String.valueOf(vid)); setVertex(true); setConnectable(true);
        }

        void reset() {
            if (parent != null) {
                getEdgeTo(parent).reset();
                parent = null;
            }
            setNormal(vss.initDist); // change style to normal with initial distance
        }

        void setNormal(int dist) {
            style(vss.def, dist); // change style to normal
        }

        void setEnqueued(int dist) {
            style(vss.enq, dist); // change style to enqueued
        }

        void setDequeued(int dist) {
            style(vss.deq, dist); // change style to dequeued
        }

        void setProcessed(int dist) {
            tree.restore(this, dist); // change style to default tree style
        }

        void setInTree(int parent, int dist) {
            tree.update(this, getVertex(parent), dist);
        }

        Edge getEdgeTo(Vertex v) {
            return getEdge(this, v);
        }

        void style(VertexStyle vs, int dist) {
            this.vs = vs; this.dist = dist;
            if (dist != -1) switchLabel(vs.label, dist);
            model.setStyle(this, vs.str);
        }

        void switchLabel(String tmpl, int dist) {
            //noinspection StringEquality (internalized strings)
            if (tmpl != vs.label || vs.distLabel)
                model.setValue(this, tmpl == null || tmpl.isEmpty() ? null : makeLabel(vid, x, y, tmpl, dist));
        }

        static String makeLabel(int vid, int x, int y, VertexStyles vss) {
            return vss.def.label == null ? null : makeLabel(vid, x, y, vss.def.label, vss.initDist);
        }

        static String makeLabel(int vid, int x, int y, String tmpl, int dist) {
            return tmpl.replace("%vid%", String.valueOf(vid))
                    .replace("%x%", String.valueOf(x)).replace("%y%", String.valueOf(y))
                    .replace("%dist%", dist < MAX_VALUE ? String.valueOf(dist) : "∞");
        }

        void move(int x, int y, int size) {
            mxGeometry g = getGeometry();
            g.setWidth(size); g.setHeight(size);
            g.setX(x); g.setY(y);
        }

        @Override public String toString() {
            return "V[vid=" + vid + "," + vss + "," + vs + (parent == null ? "" : ",parent=" + parent.vid) + "]";
        }

        public boolean isTarget() {
            return vss == vc.vst;
        }
    }

    class Edge extends mxCell implements Comparable<Edge> {

        private EdgeStyle es;
        private String prob;
        private final int v1, v2;
        private int pn;

        Edge(int v1, int v2) {
            super("", new mxGeometry(), vc.ess.disabled.str);
            this.v1 = v1; this.v2 = v2; es = vc.ess.disabled;
            setEdge(true); getGeometry().setRelative(true);
        }

        private void switchLabel(String tmpl) {
            //noinspection StringEquality (internalized strings)
            if (tmpl != es.label)
                setLabel(tmpl);
        }

        private void setLabel(String tmpl) {
            setValue(tmpl == null ? null : tmpl.replace("%p%", prob));
        }

        void init(int pn) {
            prob = pf.format(this.pn = pn);
            setLabel(vc.ess.disabled.label);
            setDisabled();
            if (enabledEdges.remove(this))
                disabledEdges.add(this);
        }

        void reset() {
            if (es == vc.ess.tree)
                setEnabled();
        }

        void setEnabled() {
            switchLabel(vc.ess.enabled.label);
            es = vc.ess.enabled;
            model.setStyle(this, es.str);
        }

        void setDisabled() {
            switchLabel(vc.ess.disabled.label);
            es = vc.ess.disabled;
            model.setStyle(this, es.str);
        }

        void setTree() {
            switchLabel(vc.ess.tree.label);
            es = vc.ess.tree;
            model.setStyle(this, es.str);
        }

        @Override public int compareTo(Edge o) {
            return Integer.compare(pn, o.pn);
        }

        @Override public String toString() {
            return "E[" + "(" + v1 + ", " + v2 + ")" + "style=" + es + ",prob=" + prob + ']';
        }
    }

    static class NoTreeStrategy implements TreeStrategy {
        @Override public void restore(Vertex v, int dist) {
            v.style(v.vss.def, dist);
        }

        @Override public void update(Vertex v, Vertex p, int dist) {
            // do nothing
        }
    }

    static class FullTreeStrategy implements TreeStrategy {

        @Override public void restore(Vertex v, int dist) {
            v.style(dist < MAX_VALUE ? v.vss.tree : v.vss.def, dist);
        }

        @Override public void update(Vertex v, Vertex p, int dist) {
            if (v.parent == p) return;
            if (v.parent != null)
                v.getEdgeTo(v.parent).setEnabled();
            v.getEdgeTo(v.parent = p).setTree();
            v.style(v.vss.tree, dist);
        }
    }

    static class TargetTreeStrategy implements TreeStrategy {

        @Override public void restore(Vertex v, int dist) {
            v.style(v.children.isEmpty() ? v.vss.def : v.vss.tree, dist);
        }

        @Override public void update(Vertex v, Vertex p, int dist) {
            if (v.parent != p && v.parent != null)
                untree(v.parent, v);
            v.parent = p;
            if (v.isTarget())
                tree(p, v, dist - 1);
        }

        private void tree(Vertex v, Vertex c, int dist) {
            v.getEdgeTo(c).setTree();
            v.children.add(c);
            v.style(v.vss.tree, dist);
            if (v.parent != null)
                tree(v.parent, v, dist - 1);
        }

        private void untree(Vertex v, Vertex c) {
            v.getEdgeTo(c).setEnabled();
            v.children.remove(c);
            if (v.isTarget()) return;
            if (v.children.isEmpty()) {
                v.style(v.vss.def, v.dist);
                if (v.parent != null)
                    untree(v.parent, v);
            }
        }
    }

    interface TreeStrategy {
        void restore(Vertex v, int dist);
        void update(Vertex v, Vertex p, int dist);
    }
}
