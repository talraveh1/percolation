package percolation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import percolation.stats.Stats;
import percolation.util.Config;

import java.io.PrintStream;

public class Main {

    private static final Logger log = LoggerFactory.getLogger(Main.class);
    private static Stats stats = null;

    public static void main(String[] args) throws Exception {

        long t = System.currentTimeMillis();
        System.out.println("Starting...");

        Config gc = new Config(args.length > 0 ? args[0] : "conf/config.properties");

        if (gc.stderr != null) { // redirect stderr to the stderr file if needed
            log.info("Redirecting stderr to {}", gc.stderr);
            System.setErr(new PrintStream(gc.stderr));
        }

        Graph g = new Graph(gc);
        int[] targets = Graph.targets(gc, g);
        try (Decs decs = new Decs(gc, g, targets); SPT spt = decs.dec(SPT.load(gc, g, gc.spt))) {
            stats = decs.stats;
            Sim sim = decs.dec(new SimBase(gc, spt, targets)); // create and decorate the sim object
            sim.run(gc.seed); // run the simulation
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            print("Done (in %s).%n", Stats.timeFmt(System.currentTimeMillis() - t));
        }
    }

    private static int len = 0;

    public static void print(String fmt, Object... args) {
        String s = String.format(fmt, args);
        if (stats == null || stats.isRedirected)
            System.out.print(s);
        if (stats != null)
            stats.print(s);
        len += s.length();
    }

    public static void clear() {
        String back = "\b".repeat(len);
        String s = back + " ".repeat(len) + back;
        System.out.print(s);
        if (stats != null)
            stats.print(s);
        len = 0;
    }

    public static String indArrToString(int[] a, int[] vals, int start, int end, int count) {
        StringBuilder sb = new StringBuilder().append("size=").append(end - start).append(", a=[");
        if (end > start)
            sb.append(vals == null ? a[start] : a[start] + "(" + vals[a[start]] + ")");
        for (int i = start + 1; i < end && i < start + count; i++)
            sb.append(",").append(vals == null ? a[i] : a[i] + "(" + vals[a[i]] + ")");
        if (end > start + count + 1)
            sb.append(",...");
        if (end > start + count)
            sb.append(",").append(vals == null ? a[end - 1] : a[end - 1] + "(" + vals[a[end - 1]] + ")");
        return sb.append("]").toString();
    }
}
