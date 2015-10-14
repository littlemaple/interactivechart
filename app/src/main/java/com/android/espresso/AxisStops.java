package com.android.espresso;

/**
 * A simple class representing axis label values.
 */
public class AxisStops {
    float[] stops = new float[]{};
    int numStops;
    int decimals;

    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer("AxisStop>>>");
        for (int i = 0; i < stops.length; i++) {
            buffer.append("[ " + stops[i] + " ]");
        }
        buffer.append("\nnumStops" + numStops + "decimals" + decimals);
        return buffer.toString();
    }
}