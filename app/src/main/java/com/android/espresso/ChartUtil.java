package com.android.espresso;

/**
 * Created by blue on 2015/10/13.
 */
public class ChartUtil {

    private static final int POW10[] = {1, 10, 100, 1000, 10000, 100000, 1000000};

    /**
     * Rounds the given number to the given number of significant digits. Based on an answer on
     * <a href="http://stackoverflow.com/questions/202302">Stack Overflow</a>.
     */
    public static float roundToOneSignificantFigure(double num) {
        final float d = (float) Math.ceil((float) Math.log10(num < 0 ? -num : num));
        final int power = 1 - (int) d;
        final float magnitude = (float) Math.pow(10, power);
        final long shifted = Math.round(num * magnitude);
        return shifted / magnitude;
    }

    /**
     * Formats a float value to the given number of decimals. Returns the length of the string.
     * The string begins at out.length - [return value].
     */
    public static int formatFloat(final char[] out, float val, int digits) {
        boolean negative = false;
        if (val == 0) {
            out[out.length - 1] = '0';
            return 1;
        }
        if (val < 0) {
            negative = true;
            val = -val;
        }
        if (digits > POW10.length) {
            digits = POW10.length - 1;
        }
        val *= POW10[digits];
        long lval = Math.round(val);
        int index = out.length - 1;
        int charCount = 0;
        while (lval != 0 || charCount < (digits + 1)) {
            int digit = (int) (lval % 10);
            lval = lval / 10;
            out[index--] = (char) (digit + '0');
            charCount++;
            if (charCount == digits) {
                out[index--] = '.';
                charCount++;
            }
        }
        if (negative) {
            out[index--] = '-';
            charCount++;
        }
        return charCount;
    }

    /**
     * Computes the set of axis labels to show given start and stop boundaries and an ideal number
     * of stops between these boundaries.
     *
     * @param start    The minimum extreme (e.g. the left edge) for the axis.
     * @param stop     The maximum extreme (e.g. the right edge) for the axis.
     * @param steps    The ideal number of stops to create. This should be based on available screen
     *                 space; the more space there is, the more stops should be shown.
     * @param outStops The destination {@link AxisStops} object to populate.
     */
    public static void computeAxisStops(float start, float stop, int steps, AxisStops outStops) {
        double range = stop - start;
        if (steps == 0 || range <= 0) {
            outStops.stops = new float[]{};
            outStops.numStops = 0;
            return;
        }

        double rawInterval = range / steps;
        double interval = roundToOneSignificantFigure(rawInterval);
        double intervalMagnitude = Math.pow(10, (int) Math.log10(interval));
        int intervalSigDigit = (int) (interval / intervalMagnitude);
        if (intervalSigDigit > 5) {
            // Use one order of magnitude higher, to avoid intervals like 0.9 or 90
            interval = Math.floor(10 * intervalMagnitude);
        }

        double first = Math.ceil(start / interval) * interval;
        double last = Math.nextUp(Math.floor(stop / interval) * interval);

        double f;
        int i;
        int n = 0;
        //在当前区域再加上左右两点
        for (f = first-interval; f <= last+interval; f += interval) {
            ++n;
        }

        outStops.numStops = n;

        if (outStops.stops.length < n) {
            // Ensure stops contains at least numStops elements.
            outStops.stops = new float[n];
        }

        for (f = first-interval, i = 0; i < n; f += interval, ++i) {
            outStops.stops[i] = (float) f;
        }

        if (interval < 1) {
            outStops.decimals = (int) Math.ceil(-Math.log10(interval));
        } else {
            outStops.decimals = 0;
        }
    }

    /**
     * The simple math function Y = fun(X) to draw on the chart.
     *
     * @param x The X value
     * @return The Y value
     */
    protected static float fun(float x) {
        return (float) Math.pow(x, 3) - x / 4;
    }
}
