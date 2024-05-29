package percolation.vis;

import com.mxgraph.swing.mxGraphComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import percolation.Graph;
import percolation.util.Config;
import percolation.util.ProbFormatter;
import percolation.vis.VisStyles.VertexStyles;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static java.lang.Integer.*;
import static percolation.Graph.*;
import static percolation.vis.VisGraph.*;

public class VisEngine extends JFrame implements AutoCloseable {

    private static final Logger log = LoggerFactory.getLogger(VisEngine.class);

    private final Config gc;
    private final Graph g;
    private final VisConfig vc;
    private final VisGraph vg;
    private final ProbFormatter pf;
    private final Set<Integer> pauseAt, targets;
    private volatile boolean resize, pause;

    public VisEngine(Config gc, VisConfig vc, Graph g, int[] targets) {

        log.info("Creating visualizer object");

        this.gc = gc; this.g = g; this.vc = vc;
        pauseAt = vc.pauseAt == null ? null : Arrays.stream(vc.pauseAt).boxed().collect(Collectors.toSet());
        this.targets = Arrays.stream(targets).boxed().collect(Collectors.toSet());

        pf = new ProbFormatter(gc);

        TreeStrategy tree = switch (vc.tree) {
            case TARGETS -> new TargetTreeStrategy();
            case FULL -> new FullTreeStrategy();
            default -> new NoTreeStrategy();
        };

        vg = new VisGraph(vc, pf, tree, gc.vs);

        initWindow(); // initialize the swing window

        vg.beginUpdate();
        try {
            // important: do not change the order of insertions to mxGraph (the code throughout
            // the app assumes that each vertex id is equal to its index in the root cell)

            initVertices(false); // add the vertices to the graph

            // add all edges to the graph in "disabled" mode (grayed)
            for (int vid = 0; vid < this.gc.vs; vid++) {
                if (g.epn(vid, R) != MAX_VALUE) vg.addEdge(vid, g.next(vid, R));
                if (g.epn(vid, D) != MAX_VALUE) vg.addEdge(vid, g.next(vid, D));
            }
        } finally {
            vg.endUpdate();
        }

        delay(false);
    }

    private void initWindow() {
        java.awt.Dimension dim = vc.dim;
        if (dim == null) {
            dim = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
            dim.width = dim.height = Math.min(dim.height, dim.width) - 100;
        }
        pack();
        Insets frameInsets = getInsets();
        dim.width += frameInsets.left + frameInsets.right;
        dim.height += frameInsets.top + frameInsets.bottom;
        setPreferredSize(dim);
        pack();
        mxGraphComponent comp = new mxGraphComponent(vg);
        comp.setConnectable(false);
        comp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        comp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        getContentPane().add(comp);
        if (vc.pos != null) setLocation(vc.pos.width, vc.pos.height);
        else setLocationRelativeTo(null);
        comp.setFocusable(true);
        comp.requestFocusInWindow();
        initCallbacks(comp);
        setVisible(true);
    }

    private void initCallbacks(mxGraphComponent comp) {
        mxGraphComponent.mxGraphControl cont = comp.getGraphControl();
        for (KeyListener k : comp.getKeyListeners()) comp.removeKeyListener(k);
        comp.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_SPACE) pause(true);
            }
        });
        for (MouseListener l : cont.getMouseListeners()) cont.removeMouseListener(l);
        for (MouseMotionListener l : cont.getMouseMotionListeners()) cont.removeMouseMotionListener(l);
        cont.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                pause(true);
            }
        });
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                resize(true);
            }
        });
    }

    private void initVertices(boolean move) {
        Dimension dim = getContentPane().getSize();
        int defSize = Math.min(dim.width, dim.height) / (gc.r + 1) / 4;
        int div = 2 * (gc.r + 1); // divide the window size by this number
        for (int vid = 0; vid < gc.vs; vid++)
            initVertex(move, vid, getVertexStyles(vid, targets), defSize, dim, div);
    }

    private VertexStyles getVertexStyles(int vid, Set<Integer> targets) {
        return vid == g.root ? vc.vsr : targets.contains(vid) ? vc.vst : vc.vsd;
    }

    private void initVertex(boolean move, int vid, VertexStyles vss, int defSize, Dimension dim, int div) {
        int isInner = vid / gc.outer;
        int idx = vid - isInner * gc.outer;
        int cols = gc.r + 1 - isInner;
        int x = idx % cols, y = idx / cols; // coordinates on the inner or outer grid
        int size = vss.size > 0 ? vss.size : defSize;
        int xScr = dim.width * (2 * x + isInner + 1) / div - size / 2;
        int yScr = dim.height * (2 * y + isInner + 1) / div - size / 2;
        if (move) vg.getVertex(vid).move(xScr, yScr, size);
        else vg.addVertex(vid, g.x(vid), g.y(vid), vss, xScr, yScr, size);
    }

    // initialize the graph edges with the new probability numerators
    public void init() {
        for (int vid = 0; vid < gc.vs; vid++) {
            if (g.epn(vid, R) != MAX_VALUE) vg.getEdge(vid, g.next(vid, R)).init(g.epn(vid, R));
            if (g.epn(vid, D) != MAX_VALUE) vg.getEdge(vid, g.next(vid, D)).init(g.epn(vid, D));
        }
    }

    // reset the graph vertices and edges state
    public void reset() {
        for (int vid = 0; vid < gc.vs; vid++)
            vg.getVertex(vid).reset();
        delay(false);
    }

    // update the graph probability label and edges
    public void update(int pn) {
        setTitle("p=" + pf.format(pn));
        vg.update(pn);
    }

    public void delay(int pn) {
        delay(pn >= 0 && pauseAt != null && pauseAt.contains(pn));
    }

    @Override public void close() {
        log.info("Closing visualizer");
        dispose();
    }

    public void setVertexEnq(int vid, int dist) {
        vg.getVertex(vid).setEnqueued(dist);
        delay(false);
    }

    public void setVertexDeq(int vid, int dist) {
        vg.getVertex(vid).setDequeued(dist);
        delay(false);
    }

    public void setVertexInTree(int vid, int parent, int dist) {
        vg.getVertex(vid).setInTree(parent, dist);
        delay(false);
    }

    public void setVertexProcessed(int vid, int dist) {
        vg.getVertex(vid).setProcessed(dist);
        delay(false);
    }

    // begins an update if small changes are grouped
    public void beginUpdate(boolean force) {
        if (resize) resize(false);
        if (pause) pause(false);
        if (force || vc.minorPause < 0) vg.beginUpdate();
    }

    // ends an update if small changes are grouped

    public void endUpdate(boolean force) {
        if (force || vc.minorPause < 0) vg.endUpdate();
    }

    public void delay(boolean major) {
        try {
            if (major && vc.majorPause < 0)
                pause(false);
            else if (major && vc.majorPause > 0)
                Thread.sleep(vc.majorPause);
            else if (!major && vc.minorPause > 0)
                Thread.sleep(vc.minorPause);
        } catch (InterruptedException ignored) {}
    }

    private synchronized void pause(boolean listener) {
        if (listener) {
            pause = !pause;
            if (!pause) notifyAll();
        } else {
            pause = true;
            while (pause) try {wait();} catch (InterruptedException ignored) {}
        }
    }

    private synchronized void resize(boolean listener) {
        resize = false;
        if (!listener || pause) {
            vg.beginUpdate();
            try {
                initVertices(true); // move/resize the vertices
            } finally {
                vg.endUpdate();
            }
            vg.refresh();
        }
    }
}