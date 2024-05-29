package percolation.util;

import static java.lang.Integer.MAX_VALUE;

public class ProbFormatter {

    public final int mul, div;
    public final String fmt;

    public ProbFormatter(Config gc) {
        this.div = gc.div;
        int prec = getPrec(gc.from, gc.step, gc.div);
        this.mul = (int) Math.pow(10, prec);
        fmt = prec > 0 ? "%d.%0" + prec + "d%%" : "%d%%";
    }

    public ProbFormatter(Config gc, String fmt) {
        this.div = gc.div;
        int prec = getPrec(gc.from, gc.step, gc.div);
        this.mul = (int) Math.pow(10, prec);
        this.fmt = String.format(fmt, prec > 0 ? "%d.%0" + prec + "d%%%%" : "%d%%%%");
    }

    static int getPrec(int from, int step, int div) {
        int prec = 4;
        for (long mul = 10000000L; prec > 0; prec--, mul /= 10)
            if (mul * from / div % 100 > 0 || mul * (from + step) / div % 100 > 0)
                break;
        return prec;
    }

    public String format(int pn) {
        return pn < 0 || pn == MAX_VALUE ? null : String.format(fmt, 100 * pn / div, pn * mul % 100 / mul);
    }
}
