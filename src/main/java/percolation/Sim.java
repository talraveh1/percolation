package percolation;

import percolation.db.Store;

import java.sql.SQLException;

public interface Sim extends Store {
    Sim set(Sim dec);
    void exp(long seed) throws SQLException;
    void exp(boolean close) throws SQLException;
    void run(long seed) throws SQLException;
    void run(long seed, int run) throws SQLException;
    void run(int pi, int pn) throws SQLException;
    default void trans(boolean done) throws SQLException {
        // do nothing
    }
    default void save(int pn, int vid, int x, int y, long dsum, int dcnt) throws SQLException {
        // do nothing
    }
}
