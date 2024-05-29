package percolation.util;

import static percolation.util.Config.*;

@SuppressWarnings("unused")
public class ConfigBuilder {
    private String dbUrl, stderr, spt, vis, stats = "full", targets;
    private int r, dist = 0, runs = 1, skip = 0, from, to = 100, step = 1, div = 100;
    private long save, seed, refresh;
    private boolean remote;

    public ConfigBuilder dbUrl(String dbUrl) {
        this.dbUrl = dbUrl;
        return this;
    }

    public ConfigBuilder stderr(String stderr) {
        this.stderr = stderr;
        return this;
    }

    public ConfigBuilder spt(String spt) {
        this.spt = spt;
        return this;
    }

    public ConfigBuilder radius(int r) {
        this.r = r;
        return this;
    }
    public ConfigBuilder distance(int dist) {
        this.dist = dist;
        return this;
    }

    public ConfigBuilder runs(int runs) {
        this.runs = runs;
        return this;
    }

    public ConfigBuilder skip(int skip) {
        this.skip = skip;
        return this;
    }

    public ConfigBuilder from(int from) {
        this.from = from;
        return this;
    }

    public ConfigBuilder to(int to) {
        this.to = to;
        return this;
    }

    public ConfigBuilder step(int step) {
        this.step = step;
        return this;
    }

    public ConfigBuilder div(int div) {
        this.div = div;
        return this;
    }

    public ConfigBuilder targets(String targets) {
        this.targets = targets;
        return this;
    }

    public ConfigBuilder stats(String stats) {
        this.stats = stats;
        return this;
    }

    public ConfigBuilder remote(boolean remote) {
        this.remote = remote;
        return this;
    }

    public ConfigBuilder refresh(long refresh) {
        this.refresh = refresh;
        return this;
    }

    public ConfigBuilder saveEvery(long save) {
        this.save = save;
        return this;
    }

    public ConfigBuilder visProps(String vis) {
        this.vis = vis;
        return this;
    }

    public ConfigBuilder seed(long seed) {
        this.seed = seed;
        return this;
    }

    public Config build() {
        return new Config(dbUrl, stderr, spt, runs, save, r, dist, skip, seed, from, to, step, div,
                targets, parseList(targets), stats, remote, refresh, vis);
    }
}