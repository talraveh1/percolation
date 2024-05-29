package percolation.vis;

import org.apache.commons.lang3.mutable.MutableBoolean;

import static com.mxgraph.util.mxConstants.*;

public class VisStyles {

    public static final byte[] DEFAULTS = """
        vertex.default.label=
        vertex.default.shape=ellipse
        vertex.default.stroke.width=1                                    
        vertex.default.size=auto
        vertex.default.font.size=12
        vertex.default.font.family=Arial,Helvetica
        vertex.default.font.style=0
        vertex.default.font.color=black
        edge.disabled.label=
        edge.disabled.stroke.color=#ffffff
        edge.disabled.stroke.width=1
        edge.disabled.font.size=10
        edge.disabled.font.family=Arial,Helvetica
        edge.disabled.font.style=0
        edge.disabled.font.color=black
        """.getBytes();

    static class EdgeStyle {
        final Stroke stroke;
        final Font font;
        final String label, str;

        EdgeStyle(VisConfig vc, String name, EdgeStyle parent) {
            String prefix = "edge." + name;
            String tmpl = vc.get(prefix + ".label");
            label = tmpl != null ? tmpl.replace("\\n", "\n").intern() : parent != null ? parent.label : null;
            this.stroke = new Stroke(vc, parent == null ? null : parent.stroke, prefix);
            this.font = new Font(vc, parent == null ? null : parent.font, prefix);
            str = String.format("%s;%s;endArrow=none;startArrow=none", this.stroke, this.font);
        }

        @Override public String toString() {
            return str;
        }
    }

    static class EdgeStyles {

        final EdgeStyle disabled;
        final EdgeStyle enabled;
        final EdgeStyle tree;

        EdgeStyles(EdgeStyle disabled, EdgeStyle enabled, EdgeStyle tree) {
            this.disabled = disabled;
            this.enabled = enabled;
            this.tree = tree;
        }

        @Override public String toString() {
            return "EdgeStyles{" +
                   "disabled=" + disabled +
                   ", enabled=" + enabled +
                   ", tree=" + tree +
                   '}';
        }
    }

    static class Fill {

        private final Fill parent;
        final String color, str;

        Fill(VisConfig vc, Fill parent, String prefix) {
            this(parent, vc.get(prefix + ".fill.color"));
        }

        private Fill(Fill parent, String color) {
            this.parent = parent;
            this.color = color;
            str = String.format("%s=%s", STYLE_FILLCOLOR, color());
        }

        private String color() {
            return color != null ? color : parent != null ? parent.color() : null;
        }

        @Override public String toString() {
            return str;
        }
    }

    static class Font {
        private final Font parent;
        final String family, style, size, color, str;

        Font(VisConfig vc, Font parent, String prefix) {
            this(parent, vc.get(prefix + ".font.family"), vc.get(prefix + ".font.style"),
                    vc.get(prefix + ".font.size"), vc.get(prefix + ".font.color"));
        }

        private Font(Font parent, String family, String style, String size, String color) {
            this.parent = parent;
            this.family = family;
            this.style = style;
            this.size = size;
            this.color = color;
            str = String.format("%s=%s;%s=%s;%s=%s;%s=%s", STYLE_FONTFAMILY, family(),
                    STYLE_FONTSTYLE, style(), STYLE_FONTSIZE, size(), STYLE_FONTCOLOR, color());
        }

        private String family() {
            return family != null ? family : parent != null ? parent.family() : null;
        }

        private String style() {
            return style != null ? style : parent != null ? parent.style() : null;
        }

        private String size() {
            return size != null ? size : parent != null ? parent.size() : null;
        }

        private String color() {
            return color != null ? color : parent != null ? parent.color() : null;
        }

        @Override public String toString() {
            return str;
        }
    }

    static class Stroke {
        private final Stroke parent;
        final String color, width, str;

        Stroke(VisConfig vc, Stroke parent, String prefix) {
            this(parent, vc.get(prefix + ".stroke.color"), vc.get(prefix + ".stroke.width"));
        }

        private Stroke(Stroke parent, String color, String width) {
            this.parent = parent;
            this.color = color;
            this.width = width;
            str = String.format("%s=%s;%s=%s", STYLE_STROKECOLOR, color(), STYLE_STROKEWIDTH, width());
        }

        private String color() {
            return color != null ? color : parent != null ? parent.color() : null;
        }

        private String width() {
            return width != null ? width : parent != null ? parent.width() : null;
        }

        @Override public String toString() {
            return str;
        }
    }

    static class VertexStyle {
        private final String name;
        private final VertexStyle parent;
        private final String shape, perimeter;
        private final Fill fill;
        private final Stroke stroke;
        private final Font font;
        final boolean distLabel;
        final String label, str;
        final int size;

        VertexStyle(VisConfig vc, String name, String key, VertexStyle parent, MutableBoolean distLabel) {
            this.name = name;
            this.parent = parent;
            String prefix = "vertex." + key;
            shape = vc.get(prefix + ".shape");
            perimeter = vc.get(prefix + ".perimeter");
            String sizeStr = vc.get(prefix + ".size");
            size = Math.max(0, sizeStr == null || sizeStr.equals("auto") ? 0 : Integer.parseInt(sizeStr));
            fill = new Fill(vc, parent != null ? parent.fill : null, prefix);
            stroke = new Stroke(vc, parent != null ? parent.stroke : null, prefix);
            font = new Font(vc, parent != null ? parent.font : null, prefix);
            String tmpl = vc.get(prefix + ".label");
            String origLabel = tmpl == null ? null : tmpl.replace("\\n", "\n").intern();
            label = origLabel != null ? origLabel : parent != null ? parent.label : null;
            this.distLabel = label != null && distLabel(label);
            if (this.distLabel) distLabel.setTrue(); // notify caller that dist labels are used
            str = this.perimeter == null
                    ? String.format("%s=%s;%s;%s;%s", STYLE_SHAPE, shape(), fill, stroke, font)
                    : String.format("%s=%s;%s=%s;%s;%s;%s",
                        STYLE_SHAPE, shape(), STYLE_PERIMETER, perimeter(), fill, stroke, font);
        }

        private String shape() {
            return shape != null ? shape : parent != null ? parent.shape() : null;
        }

        private String perimeter() {
            return perimeter != null ? perimeter : parent != null ? parent.perimeter() : null;
        }

        boolean distLabel(String label) {
            return label != null && label.contains("%dist%");
        }

        @Override public String toString() {
            return name;
        }
    }

    static class VertexStyles {
        private final String name;
        final VertexStyle def, enq, deq, tree;
        final boolean root, target;
        final int initDist, size;

        VertexStyles(VisConfig vc, String name, VertexStyle n, VertexStyle e, VertexStyle d,
                     VertexStyle t, MutableBoolean distLabel, boolean root, boolean target) {

            this(name, new VertexStyle(vc, "def", name, n, distLabel),
                    new VertexStyle(vc, "enq", name, e, distLabel),
                    new VertexStyle(vc, "deq", name, d, distLabel),
                    new VertexStyle(vc, "tree", name, t, distLabel),
                    root, target);
        }

        VertexStyles(String name, VertexStyle n, VertexStyle e, VertexStyle d, VertexStyle t, boolean root, boolean target) {
            this.name = name; this.def = n; this.enq = e; this.deq = d;
            this.tree = t; this.root = root; this.target = target;
            initDist = root ? 0 : Integer.MAX_VALUE; size = n.size;
        }

        @Override public String toString() {
            return name;
        }
    }
}
