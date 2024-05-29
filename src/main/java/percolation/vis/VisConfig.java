package percolation.vis;

import org.apache.commons.lang3.mutable.MutableBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.*;
import java.util.Properties;

import static percolation.util.Config.parseList;
import static percolation.vis.VisStyles.*;

public class VisConfig {

    private static final Logger log = LoggerFactory.getLogger(VisConfig.class);

    final int minorPause, majorPause;
    final int[] pauseAt;
    final Dimension pos, dim;
    final VertexStyles vsd, vsr, vst; // vertex default / root / target styles
    final EdgeStyles ess;
    final boolean distLabels;
    private final Properties props;

    enum TreeType {
        FULL, TARGETS, NONE
    }

    final TreeType tree;

    public VisConfig(String configFile) {

        log.info("Loading visualizer configuration from: {}", configFile);

        props = loadProperties(configFile);

        String minorPauseStr = get("pause.minor", "0");
        minorPause = minorPauseStr.equals("group") ? -1 : Integer.parseInt(minorPauseStr);
        String majorPauseStr = get("pause.major", "0");
        majorPause = majorPauseStr.equals("click") ? -1 : Integer.parseInt(majorPauseStr);
        pauseAt = parseList(get("pause.at"));

        pos = getDimensions(get("position", "auto"));
        dim = getDimensions(get("dimensions", "auto"));
        tree = TreeType.valueOf(get("tree", "full").toUpperCase());

        MutableBoolean distLabels = new MutableBoolean(false);
        VertexStyle d = new VertexStyle(this, "def", "default", null, distLabels);
        VertexStyle eq = new VertexStyle(this, "enq", "enqueued", d, distLabels);
        VertexStyle dq = new VertexStyle(this, "deq", "dequeued", eq, distLabels);
        VertexStyle t = new VertexStyle(this, "tree", "tree", dq, distLabels);
        vsd = new VertexStyles("default", d, eq, dq, t, false, false);
        vst = new VertexStyles(this, "target", d, eq, dq, t, distLabels, false, true);
        vsr = new VertexStyles(this, "root", d, eq, dq, t, distLabels, true, false);
        this.distLabels = distLabels.getValue();

        EdgeStyle disabled = new EdgeStyle(this, "disabled", null);
        EdgeStyle enabled = new EdgeStyle(this, "enabled", disabled);
        EdgeStyle tree = new EdgeStyle(this, "tree", enabled);
        ess = new EdgeStyles(disabled, enabled, tree);

        log.trace("{}", this);
    }

    private static Properties loadProperties(String file) {
        Properties props = new Properties();
        try {
            loadFromStream(props, new ByteArrayInputStream(VisStyles.DEFAULTS));
            loadFromStream(props, new FileInputStream(file));
        } catch (FileNotFoundException ignored) {
            log.warn("Configuration file not found (using defaults): {}", file);
        }
        return props;
    }

    private static void loadFromStream(Properties props, InputStream in) {
        try (in) {
            props.load(in);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static Dimension getDimensions(String val) {
        if (val.equals("auto")) return null;
        String[] parts = val.split(",");
        return new Dimension(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
    }

    String get(String prop) {
        return props.getProperty(prop.toLowerCase());
    }

    String get(String prop, String def) {
        return props.getProperty(prop.toLowerCase(), def);
    }

    @Override public String toString() {
        return "VisConfig{" + "minor=" + minorPause + ", major=" + majorPause + ", pos=" + pos + ", dim=" + dim +
               ", vsd=" + vsd + ", vsr=" + vsr + ", vst=" + vst + ", ess=" + ess + ", distLabels=" + distLabels +
               ", props=" + props + ", tree=" + tree + '}';
    }
}
