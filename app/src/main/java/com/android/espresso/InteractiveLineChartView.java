package com.android.espresso;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;

/**
 * Created by blue on 2015/10/13.
 */
public class InteractiveLineChartView extends GraphView {


    // Buffers for storing current X and Y stops. See the computeAxisStops method for more details.
    private final AxisStops mXStopsBuffer = new AxisStops();
    private final AxisStops mYStopsBuffer = new AxisStops();
    private final char[] mLabelBuffer = new char[100];

    // Buffers used during drawing. These are defined as fields to avoid allocation during
    // draw calls.
    private float[] mAxisXPositionsBuffer = new float[]{};
    private float[] mAxisYPositionsBuffer = new float[]{};
    private float[] mAxisXLinesBuffer = new float[]{};
    private float[] mAxisYLinesBuffer = new float[]{};


    public InteractiveLineChartView(Context context) {
        this(context, null);
    }

    public InteractiveLineChartView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public InteractiveLineChartView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setIsOutlineShown(false);
        initContentArea();
    }

    @Override
    protected void drawAxes(Canvas canvas) {
        int i;

        ChartUtil.computeAxisStops(
                getCurrentViewport().left,
                getCurrentViewport().right,
                getContentRect().width() / mMaxLabelWidth / 2,
                mXStopsBuffer);
        ChartUtil.computeAxisStops(
                getCurrentViewport().top,
                getCurrentViewport().bottom,
                getContentRect().height() / mLabelHeight / 2,
                mYStopsBuffer);

        // Avoid unnecessary allocations during drawing. Re-use allocated
        // arrays and only reallocate if the number of stops grows.
        if (mAxisXPositionsBuffer.length < mXStopsBuffer.numStops) {
            mAxisXPositionsBuffer = new float[mXStopsBuffer.numStops];
        }
        if (mAxisYPositionsBuffer.length < mYStopsBuffer.numStops) {
            mAxisYPositionsBuffer = new float[mYStopsBuffer.numStops];
        }
        if (mAxisXLinesBuffer.length < mXStopsBuffer.numStops * 4) {
            mAxisXLinesBuffer = new float[mXStopsBuffer.numStops * 4];
        }
        if (mAxisYLinesBuffer.length < mYStopsBuffer.numStops * 4) {
            mAxisYLinesBuffer = new float[mYStopsBuffer.numStops * 4];
        }

        // Compute positions
        for (i = 0; i < mXStopsBuffer.numStops; i++) {
            mAxisXPositionsBuffer[i] = getDrawX(mXStopsBuffer.stops[i]);
        }
        for (i = 0; i < mYStopsBuffer.numStops; i++) {
            mAxisYPositionsBuffer[i] = getDrawY(mYStopsBuffer.stops[i]);
        }

        // Draws grid lines using drawLines (faster than individual drawLine calls)
        //去除左右多余的两点
        for (i = 1; i < mXStopsBuffer.numStops-1; i++) {
            mAxisXLinesBuffer[i * 4 + 0] = (float) Math.floor(mAxisXPositionsBuffer[i]);
            mAxisXLinesBuffer[i * 4 + 1] = getContentRect().top;
            mAxisXLinesBuffer[i * 4 + 2] = (float) Math.floor(mAxisXPositionsBuffer[i]);
            mAxisXLinesBuffer[i * 4 + 3] = getContentRect().bottom;
        }
        canvas.drawLines(mAxisXLinesBuffer, 0, mXStopsBuffer.numStops * 4, mGridPaint);

        for (i = 0; i < mYStopsBuffer.numStops; i++) {
            mAxisYLinesBuffer[i * 4 + 0] = getContentRect().left;
            mAxisYLinesBuffer[i * 4 + 1] = (float) Math.floor(mAxisYPositionsBuffer[i]);
            mAxisYLinesBuffer[i * 4 + 2] = getContentRect().right;
            mAxisYLinesBuffer[i * 4 + 3] = (float) Math.floor(mAxisYPositionsBuffer[i]);
        }
        canvas.drawLines(mAxisYLinesBuffer, 0, mYStopsBuffer.numStops * 4, mGridPaint);

        // Draws X labels
        int labelOffset;
        int labelLength;
        mLabelTextPaint.setTextAlign(Paint.Align.CENTER);
        for (i = 1; i < mXStopsBuffer.numStops-1; i++) {
            // Do not use String.format in high-performance code such as onDraw code.
            labelLength = ChartUtil.formatFloat(mLabelBuffer, mXStopsBuffer.stops[i], mXStopsBuffer.decimals);
            labelOffset = mLabelBuffer.length - labelLength;
            canvas.drawText(
                    mLabelBuffer, labelOffset, labelLength,
                    mAxisXPositionsBuffer[i],
                    getContentRect().bottom + mLabelHeight + mLabelSeparation,
                    mLabelTextPaint);
        }

        // Draws Y labels
        mLabelTextPaint.setTextAlign(Paint.Align.RIGHT);
        for (i = 0; i < mYStopsBuffer.numStops; i++) {
            // Do not use String.format in high-performance code such as onDraw code.
            labelLength = ChartUtil.formatFloat(mLabelBuffer, mYStopsBuffer.stops[i], mYStopsBuffer.decimals);
            labelOffset = mLabelBuffer.length - labelLength;
            canvas.drawText(
                    mLabelBuffer, labelOffset, labelLength,
                    getContentRect().left - mLabelSeparation,
                    mAxisYPositionsBuffer[i] + mLabelHeight / 2,
                    mLabelTextPaint);
        }
    }

    private void initContentArea(){
        getCurrentViewport().left = 2;
    }

    private Path path = new Path();
    @Override
    protected void drawDataSeriesUnclipped(Canvas canvas) {
        path.reset();
        path.moveTo(mAxisXPositionsBuffer[0],getDrawY(0));
        for(int i=0;i<mXStopsBuffer.numStops;i++){
            canvas.drawCircle(mAxisXPositionsBuffer[i],getDrawY(0),20,mDataPaint);
            if(i!=0){
                path.lineTo(mAxisXPositionsBuffer[i],getDrawY(0));
            }
        }
        canvas.drawPath(path,mDataPaint);
    }

    @Override
    public String dev() {

        return mXStopsBuffer.toString()+super.dev();
    }
}
