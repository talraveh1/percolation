package percolation.util;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.filter.ThresholdFilter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;

public class Config {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(Config.class);

    public final String visProps, stats, dbUrl, stderr, spt, targets;
    public final long seed, refresh, save;
    public final int r, dist, vs, runs, skip, from, to, step, div;
    public final int[] targetList;
    public final int ps, log2r, ts, inner, outer; // inner/outer vertices
    public final boolean remote, vis;

    public Config(String configFile) {
        this(load(configFile));
    }

    Config(Properties p) {
        this(p, initLoggingLevels(p));
    }

    Config(Properties p, @SuppressWarnings("unused") int dummy) {
        this(get(p, "db.url"), get(p, "stderr"), getClass(p, "spt"), get(p, "runs", "1"),
                get(p, "save.every", "600"), get(p, "radius"), get(p, "distance", "0"),
                get(p, "skip", "0"), get(p, "seed"), get(p, "from", "48"),
                get(p, "to", "100"), get(p, "step", "1"), get(p, "div", "100"),
                get(p, "targets", "all"), get(p, "stats", "full"),
                get(p, "stats.remote", "false"), get(p, "stats.refresh", "100"),
                get(p, "vis.props"));
    }

    Config(String dbUrl, String stderr, String spt, String runs,
           String save, String r, String dist, String skip, String seed,
           String from, String to, String step, String div, String targets,
           String stats, String remote, String refresh, String visProps) {
        this(dbUrl, stderr, spt,
                parseInt("runs", runs), parseLong("save.every", save),
                parseInt("radius", r), parseInt("distance", dist), parseInt("skip", skip),
                seed == null ? System.currentTimeMillis() : parseLong("seed", seed),
                parseInt("from", from), parseInt("to", to), parseInt("step", step),
                parseInt("div", div), targets, parseList(targets),
                parseChoice("stats", stats, "none", "min", "full"),
                parseBoolean("remote", remote), parseLong("stats.refresh", refresh), visProps);
    }

    static int initLoggingLevels(Properties p) {
        Level logLevel = parseLevel(p.getProperty("log.level", "info"));
        Level logConsoleFilter = parseLevel(p.getProperty("log.console", "off"));
        Level logFileFilter = parseLevel(p.getProperty("log.file", "info"));
        setLogLevel(logLevel);
        setLogFilter(logConsoleFilter, "CONSOLE");
        setLogFilter(logFileFilter, "FILE");
        return 0;
    }

    private static String getClass(Properties p, String cls) {
        String c = p.getProperty(cls + ".class");
        log.info("{}={}", cls + ".class", c);
        return c;
    }

    private static String get(Properties p, String key) {
        return get(p, key, null);
    }

    private static String get(Properties p, String key, String def) {
        String prop = p.getProperty(key, def);
        prop = prop == null ? def : prop.toLowerCase();
        log.info("{}={}", key, prop);
        return prop;
    }

    Config(String dbUrl, String stderr, String spt, int runs, long save,
           int r, int dist, int skip, long seed, int from, int to, int step, int div,
           String targets, int[] targetList, String stats, boolean remote, long refresh, String visProps) {

        this.dbUrl = dbUrl; this.stderr = stderr; this.spt = spt; this.runs = runs; this.r = r; this.dist = dist;
        this.skip = skip; this.seed = seed; this.from = from; this.to = to; this.step = step; this.div = div;
        this.targets = targets; this.targetList = targetList; this.stats = stats; this.remote = remote;
        this.refresh = refresh; this.save = save > 0 ? 1000L * save : save; this.visProps = visProps;
        verify(runs, r, skip, from, to, step, div, save);
        vis = visProps != null && !visProps.isEmpty();
        inner = this.r * this.r; // inner vertices
        outer = (this.r + 1) * (this.r + 1); // outer vertices
        vs = outer + inner; // total vertices
        ps = (this.to - this.from) / this.step + 1; // number of probabilities
        log2r = (int) (Math.log(this.r) / Math.log(2));
        ts = (1 << (log2r + 3)) - 4;
    }

    private static void setLogLevel(Level level) {
        ch.qos.logback.classic.Logger root
                = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        root.setLevel(level);
    }

    private static void setLogFilter(Level level, String appender) {
        LoggerContext c = (LoggerContext) LoggerFactory.getILoggerFactory();
        Appender<ILoggingEvent> a = c.getLogger(Logger.ROOT_LOGGER_NAME).getAppender(appender);
        if (a != null) {
            a.getCopyOfAttachedFiltersList().stream()
                    .filter(f -> f instanceof ThresholdFilter)
                    .map(ThresholdFilter.class::cast)
                    .findFirst().orElseGet(() -> {
                        ThresholdFilter f = new ThresholdFilter();
                        f.setContext(c);
                        f.start();
                        a.addFilter(f);
                        return f;
                    }).setLevel(level.toString());
        }
    }

    private static void verify(int runs, int r, int skip, int from, int to, int step, int div, long save) {
        if (r < 1)
            throw new RuntimeException("Invalid config (radius must be >= 1)");
        if (skip < 0)
            throw new RuntimeException("Invalid config (skip must be >= 0)");
        if (runs < 0)
            throw new RuntimeException("Invalid config (runs must be >= 0)");
        if (from > to)
            throw new RuntimeException("Invalid config (from must be <= to)");
        if ((double) from / div < 0 || (double) from / div > 1)
            throw new RuntimeException("Invalid config (from/div must be between 0 and 1, inclusive)");
        if ((double) to / div < 0 || (double) to / div > 1)
            throw new RuntimeException("Invalid config (to/div must be between 0 and 1, inclusive)");
        if (step < 1)
            throw new RuntimeException("Invalid config (step must be >= 1)");
        if (div < 1)
            throw new RuntimeException("Invalid config (div must be >= 1)");
        if (save < -2L)
            throw new RuntimeException("Invalid config (save.every must be >= -2)");
    }

    public static Level parseLevel(String level) {
        level = level.toUpperCase();
        Level l = Level.toLevel(level);
        if (!l.toString().equals(level))
            throw new RuntimeException("Invalid config (log.level must be one of: trace, debug, info, warn, error)");
        return l;
    }

    public static String parseChoice(String key, String val, String... choices) {
        for (String choice : choices)
            if (choice.equalsIgnoreCase(val))
                return choice;
        throw new RuntimeException(String.format("Invalid config (must be one of: %s): %s=%s",
                Arrays.toString(choices), key, val));
    }

    public static int[] parseList(String val) {
        if (val == null) return null;
        try {
            return Arrays.stream(val.split(","))
                    .map(String::trim)
                    .mapToInt(Integer::parseInt)
                    .toArray();
        } catch (NumberFormatException e) {
            return null; // ignore if not a valid list of integers
        }
    }

    public static int parseInt(String key, String val) {
        try {
            return Integer.parseInt(val);
        } catch (NumberFormatException e) {
            throw new RuntimeException(String.format("Invalid config (number format): %s=%s", key, val), e);
        }
    }

    public static boolean parseBoolean(String key, String val) {
        try {
            return Boolean.parseBoolean(val);
        } catch (NumberFormatException e) {
            throw new RuntimeException(String.format("Invalid config (boolean format): %s=%s", key, val), e);
        }
    }

    public static long parseLong(String key, String val) {
        try {
            return Long.parseLong(val);
        } catch (NumberFormatException e) {
            throw new RuntimeException(String.format("Invalid config (number format): %s=%s", key, val), e);
        }
    }

    private static Properties load(String file) {
        try (FileInputStream in = new FileInputStream(file)) {
            Properties props = new Properties();
            props.load(in);
            return props;
        } catch (IOException e) {
            throw new RuntimeException("Error loading config file: " + file, e);
        }
    }
}
