package percolation.stats;

import org.apache.commons.lang3.time.StopWatch;
import org.fusesource.jansi.AnsiConsole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import percolation.util.Config;
import percolation.util.ProbFormatter;
import percolation.util.RemoteStream;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.net.SocketException;
import java.text.DecimalFormat;

import static org.fusesource.jansi.Ansi.ansi;

public class Stats implements AutoCloseable {

    private static final Logger log = LoggerFactory.getLogger(Stats.class);

    public final PrintStream out;
    public final boolean isRedirected;
    private int bottom = 1; // last row of the output
    private final Output expCounter, resultSample, runState;
    private final VoidValOutput memoryStats, elapsedTimeFast, elapsedTimeSlow;
    private final IncValOutput dequeueCounter;
    private final LongValOutput probVal, maxDist, queueSize;
    private final TimeOutput expTime, cycleTime, sptTime, dumpTime;
    private final long refresh; // refresh rate in ms
    private final DecimalFormat df = new DecimalFormat("0.00");

    public Stats(Config gc) {
        this(gc, gc.remote || System.console() == null ? newRemoteStream() : System.out, gc.refresh);
    }

    public Stats(Config gc, PrintStream out, long refresh) {
        this(gc, out, refresh, gc.spt); // constructor for tests
    }

    public Stats(Config gc, PrintStream out, long refresh, String spt) {

        log.info("Creating stats object with refresh rate: {}", refresh);

        this.out = out;
        this.refresh = refresh;
        isRedirected = out != System.out; // must be before ansiConsole.systemInstall()
        PrintStream err = System.err;
        AnsiConsole.systemInstall();
        if (gc.stderr != null) System.setErr(err);
        out.print(ansi().eraseScreen());
        runState = new BaseOutput("Experiment runner state:");
        initializing();
        // pretty-print the current data and time
        out("SPT class: @|yellow,bold %s|@", spt);
        out("Start time: @|cyan,bold %s|@", new java.util.Date());
        out("Runs: @|bold %d|@ | R: @|bold %s|@ Vs: @|bold %s|@ Ts: @|bold %s|@ Skip: @|bold %s|@",
                gc.runs, gc.r, numFmt(gc.vs), numFmt(gc.ts), gc.skip);
        out("Stats: @|bold %s|@ | From: @|bold %d|@ To: @|bold %d|@ Step: @|bold %d|@ Div: @|bold %d|@",
                gc.stats, gc.from, gc.to, gc.step, gc.div);
        memoryStats = getMemOutput("Mem:");
        out("-"); // line separator
        expCounter = new TimeOutput("Experiment:");
        probVal = getProbOutput(gc, "Probability:");
        elapsedTimeFast = getElapsedOutput("Elapsed time:");
        expTime = new TimeOutput("Exp time:");
        sptTime = new TimeOutput("SPT time:");
        cycleTime = new TimeOutput("Cycle time:");
        dumpTime = gc.dbUrl != null && gc.save != 0 ? new TimeOutput("Dump time:") : null;

        if (gc.stats.equals("full")) { // full stats only
            out("-"); // line separator
            dequeueCounter = new IncValOutput("Dequeues");
            maxDist = getMaxOutput("Distance", false);
            queueSize = getMaxOutput("Queue size", true);
            out("-"); // line separator
            resultSample = new BaseOutput(bottom++, refresh, "Result:");
            try {
                elapsedTimeSlow = (VoidValOutput) elapsedTimeFast.clone();
            } catch (CloneNotSupportedException e) {
                throw new RuntimeException(e);
            }
        } else {
            dequeueCounter = null;
            maxDist = null;
            queueSize = null;
            resultSample = null;
            elapsedTimeSlow = null;
        }
    }

    public void prob(int pn) {
        probVal.out(pn);
    }

    public void elapsed(boolean force) {
        if (force)
            elapsedTimeFast.out();
        else
            elapsedTimeFast.val();
    }

    public void elapsed() {
        elapsedTimeSlow.val();
    }

    public void mem() {
        memoryStats.val();
    }

    public void exps(int i, long seed) {
        expCounter.val("@|bold,green %d|@ Seed: @|bold,green %d|@  ", i, seed);
    }

    public void result(int pn, int x, int y, long dsum, int dcnt) {
        resultSample.val("@|bold pn: %d, (%d,%d): %d/%d|@        ", pn, x, y, dsum, dcnt);
    }

    public void dist(int d) {
        maxDist.val(d);
    }

    public void dist() {
        maxDist.out(maxDist.row, maxDist.col, "(max: @|bold %s|@): @|bold %s|@     ", maxDist.v2, maxDist.v1);
    }

    public void qsize() {
        queueSize.out(queueSize.row, queueSize.col, "(max: @|bold %s|@): @|bold %s|@     ", numFmt(queueSize.v2), numFmt(queueSize.v1));
    }

    public TimeOutput exp() {
        return expTime;
    }

    public TimeOutput cycle() {
        return cycleTime;
    }

    public TimeOutput spt() {
        return sptTime;
    }

    public void dump(boolean done) {
        if (done) {
            dumpTime.end();
            running();
        } else {
            dumping();
            dumpTime.start();
        }
    }

    public void starting() {
        this.runState.out("@|blue,bold starting|@      ");
    }

    public void finished() {
        this.runState.out("@|blue,bold finished|@      ");
    }

    public void initializing() {
        this.runState.out("@|yellow,bold initializing|@      ");
    }

    public void running() {
        this.runState.out("@|green,bold running|@      ");
    }

    public void dumping() {
        this.runState.out("@|yellow,bold dumping|@      ");
    }

    private abstract class VoidValOutput extends BaseOutput {
        public VoidValOutput(String prefix) {
            super(prefix);
        }

        void val() {
            if (time())
                out();
        }

        abstract void out();
    }

    private abstract class LongValOutput extends ValOutput {
        public LongValOutput(String prefix) {
            super(prefix);
        }

        void val(long val) {
            if (time())
                out(val);
        }

        abstract void out(long val);
    }

    private abstract class ValOutput extends BaseOutput {
        protected long v1, v2;

        public ValOutput(String prefix) {
            super(prefix);
        }
    }

    private class IncValOutput extends ValOutput {
        public IncValOutput(String prefix) {
            super(prefix);
        }

        public void inc() {
            v1++;
            v2++;
            val();
        }

        public void reset() {
            v1 = 0;
            val();
        }

        public void val() {
            if (time())
                super.out(row, col, "(total: @|bold %s|@): @|bold %s|@     ", numFmt(v2), numFmt(v1));
        }

    }

    private interface Output extends Cloneable {
        void val(String fmt, Object... args);
        void out(int row, int col, String fmt, Object... args);
        default void out(String fmt, Object... args) {}
        boolean time();
    }

    private class BaseOutput implements Output {
        protected final int row, col;
        private final long refresh;
        protected long calls, last, mod = 1L;

        public BaseOutput(String prefix, Object... args) {
            this(bottom++, Stats.this.refresh, prefix, args);
        }

        public BaseOutput(int row, long refresh, String prefix, Object... args) {
            this.refresh = refresh;
            this.row = row;
            col = prefix.length() + 2;
            out(this.row, 1, prefix, args);
            out(this.row, col, "@|bold N/A|@    ");
        }

        @Override protected Object clone() throws CloneNotSupportedException {
            return super.clone();
        }

        public boolean time() {
            if (calls++ % mod == 0) {
                if (System.currentTimeMillis() - last < refresh) {
                    mod++;
                } else {
                    last = System.currentTimeMillis();
                    return true;
                }
            }
            return false;
        }

        @Override
        public void out(int row, int col, String fmt, Object... args) {
            out.print(ansi().cursor(row, col).render(fmt, args).cursor(bottom + 1, 1));
        }

        @Override
        public void val(String fmt, Object... args) {
            if (time())
                out(row, col, fmt, args);
        }

        @Override
        public void out(String fmt, Object... args) {
            out(row, col, fmt, args);
        }
    }

    public class TimeOutput extends BaseOutput {
        private long start, elapsed, total;

        public TimeOutput(String prefix) {
            super(prefix);
        }

        public void start() {
            start = System.currentTimeMillis();
        }

        public void end() {
            total += elapsed = System.currentTimeMillis() - start;
            if (time())
                out();
        }

        public void out() {
            out(row, col, "@|bold,yellow %s|@ (%s / %s)     ",
                    timeFmt(total / calls), timeFmt(total), calls);
            elapsed = System.currentTimeMillis();
        }

        public long elapsed() {
            return elapsed;
        }
    }

    private LongValOutput getMaxOutput(String prefix, boolean fmt) {
        return new LongValOutput(prefix) {
            @Override
            public void val(long val) {
                if (val > v2)
                    v2 = val;
                if (time())
                    out(val);
            }

            @Override void out(long val) {
                super.out(row, col, "(max: @|bold %s|@): @|bold %s|@     ", fmt ? numFmt(v2) : v2, fmt ? numFmt(val) : val);
            }
        };
    }

    private VoidValOutput getElapsedOutput(String prefix) {
        StopWatch sw = new StopWatch();
        sw.start();
        return new VoidValOutput(prefix) {
            private final StopWatch watch = sw;

            @Override
            public void out() {
                out(row, col, "@|bold,cyan %s|@     ", watch.formatTime());
            }
        };
    }

    private LongValOutput getProbOutput(Config gc, String prefix) {
        ProbFormatter pf = new ProbFormatter(gc, "@|bold,red %s   |@");
        return new LongValOutput(prefix) {
            @Override
            public void out(long val) {
                out(pf.format((int) val));
            }
        };
    }

    private VoidValOutput getMemOutput(String prefix) {
        MemoryMXBean mb = ManagementFactory.getMemoryMXBean();
        MemoryUsage h = mb.getHeapMemoryUsage();
        MemoryUsage nh = mb.getNonHeapMemoryUsage();
        long mm = (h.getMax() > 0 ? h.getMax() : 0) + (nh.getMax() > 0 ? nh.getMax() : 0);
        VoidValOutput mem = new VoidValOutput(prefix + " " + (mm > 0 ? mm / 1073741824L + "GB" : "UND") + " | Used:") {
            private long max;

            @Override void out() {
                MemoryUsage h = mb.getHeapMemoryUsage();
                MemoryUsage nh = mb.getNonHeapMemoryUsage();
                long cur = (h.getUsed() > 0 ? h.getUsed() : 0) + (nh.getUsed() > 0 ? nh.getUsed() : 0);
                if (cur > max)
                    max = cur;
                if (time())
                    out("@|bold %s|@ | Top: @|bold %s|@", fmt(max), fmt(cur));
            }

            public String fmt(long val) {
                return val > 1073741824L ? df.format(val / 1073741824.0) + "GB"
                        : val > 1048576L ? df.format(val / 1048576.0) + "MB"
                        : val > 1024L ? df.format(val / 1024.0) + "KB"
                        : String.valueOf(val);
            }
        };
        mem.out();
        return mem;
    }

    public void out(String fmt, Object... args) {
        out.print(ansi().cursor(bottom++, 1).render(fmt, args).cursor(bottom + 1, 1));
    }

    public void print(String s) {
        out.print(s);
    }

    public void queue(int size) {
        dequeueCounter.inc();
        queueSize.val(size);
    }

    public void queue() {
        dequeueCounter.reset();
    }

    private static String numFmt(long val) {
        return val > 1000000000L ? String.format("%.2fG", val / 1000000000.0)
                : val > 1000000L ? String.format("%.2fM", val / 1000000.0)
                : val > 1000L ? String.format("%.2fK", val / 1000.0) : String.valueOf(val);
    }

    public static String timeFmt(long t) {
        return t >= 60000L ? String.format("%.2fm", t / 60000.0)
                : t >= 1000L ? String.format("%.2fs", t / 1000.0) : t + "ms";
    }

    private static PrintStream newRemoteStream() {
        // run the remote console if it's not already running?
        PrintStream out;
        try {
            out = new RemoteStream();
            System.out.println("Sending stats to remote console");
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
        return out;
    }

    @SuppressWarnings("unused") private static void runRemoteConsole() throws IOException {
        String cmd = "java -cp out/artifacts/Percolation_jar/Percolation.jar percolation.RemoteOutputStream"
                     + System.lineSeparator();
        ProcessBuilder pb = new ProcessBuilder("cmd", "/c", "conhost.exe");
        Process proc = pb.start();
        try (OutputStream os = proc.getOutputStream()) {
            os.write(cmd.getBytes());
            os.flush();
        }
    }

    @Override public void close() {

        log.info("Closing stats object");

        AnsiConsole.systemUninstall();
    }
}
