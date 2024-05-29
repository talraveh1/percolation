package percolation;

import java.sql.SQLException;

public abstract class SimDec implements Sim {

    private final Sim that;

    public SimDec(Sim that) {
        this.that = that;
    }

    @Override
    public Sim set(Sim dec) {
        return that.set(dec);
    }

    @Override
    public void exp(long seed) throws SQLException {
        that.exp(seed);
    }

    @Override
    public void exp(boolean close) throws SQLException {
        that.exp(true);
    }

    @Override
    public void run(long seed) throws SQLException {
        that.run(seed);
    }

    @Override
    public void run(long seed, int run) throws SQLException {
        that.run(seed, run);
    }

    @Override
    public void run(int pi, int pn) throws SQLException {
        that.run(pi, pn);
    }

    @Override
    public void trans(boolean done) throws SQLException {
        that.trans(done);
    }

    @Override
    public void save(int pi, int vid, int x, int y, long dsum, int dcnt) throws SQLException {
        that.save(pi, vid, x, y, dsum, dcnt);
    }

    @Override
    public void save(int pi, int tid, long dsum, int dcnt) throws SQLException {
        that.save(pi, tid, dsum, dcnt);
    }

    @Override
    public void dump(boolean force) throws SQLException {
        that.dump(force);
    }
}
