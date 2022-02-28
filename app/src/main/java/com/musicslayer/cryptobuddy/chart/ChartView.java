package com.musicslayer.cryptobuddy.chart;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.LinearLayout;

import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatTextView;

import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.api.chart.Candle;
import com.musicslayer.cryptobuddy.api.chart.ChartData;
import com.musicslayer.cryptobuddy.api.chart.PricePoint;
import com.musicslayer.cryptobuddy.crash.CrashLinearLayout;
import com.musicslayer.cryptobuddy.crash.CrashView;
import com.musicslayer.cryptobuddy.transaction.Timestamp;
import com.musicslayer.cryptobuddy.util.AppearanceUtil;
import com.musicslayer.cryptobuddy.util.HashMapUtil;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;

// Each ChartView instance represents a single graphical chart.
public class ChartView extends CrashLinearLayout {
    String timeframe = "24H"; // 24H or 30D
    String pointsType = "POINT"; // POINT, LINE, or CANDLE
    boolean isLogScale;
    boolean isHollowStyle;

    // Graphical properties of the chart.
    int buttonSize = 150;
    float fontSize = 48f;
    int pointRadius = 3;
    int lineStrokeWidth = 5;
    int candleWidth = 10;
    int candleStrokeWidth = 1;
    int axisOffsetLeft = 10;
    int axisOffsetTop = 10;
    int axisOffsetRight = 10;
    int axisOffsetBottom = 10;
    int axisStrokeWidth = 5; // X and Y always match.
    int axisTickWidthX = 20;
    int axisTickWidthY = 20;
    int axisTickStrokeWidthX = 1;
    int axisTickStrokeWidthY = 1;
    int zoomLevel = 3; // For now, keep this fixed.

    // Calculate these once and then use them when drawing the rest of the graph.
    int canvasWidth;
    int canvasHeight;
    BigDecimal topPrice;
    BigDecimal bottomPrice;
    BigDecimal topTime;
    BigDecimal bottomTime;
    int textHeightTop;
    int textHeightBottom;

    // Reuse this so we do not have to keep allocating these objects.
    final private Rect r = new Rect();

    public ChartData chartData;

    public SurfaceView surfaceView;
    AppCompatTextView T_INFO;
    AppCompatImageButton B_TIMEFRAME;
    AppCompatImageButton B_POINTS;
    AppCompatImageButton B_SCALE;
    AppCompatImageButton B_STYLE;

    public ChartView(Context context) {
        this(context, null);
    }

    public ChartView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.makeLayout();
    }

    public void makeLayout() {
        Context context = getContext();

        this.setOrientation(VERTICAL);

        T_INFO = new AppCompatTextView(context);

        LinearLayout L_CHART = new LinearLayout(context);
        L_CHART.setOrientation(HORIZONTAL);

        // Create the vertical column of buttons along the side.
        LinearLayout L_BUTTONS = new LinearLayout(context);
        L_BUTTONS.setOrientation(VERTICAL);
        L_BUTTONS.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

        B_TIMEFRAME = new AppCompatImageButton(context);
        B_TIMEFRAME.setLayoutParams(new LayoutParams(buttonSize, buttonSize));
        B_TIMEFRAME.setOnClickListener(new CrashView.CrashOnClickListener(context) {
            public void onClickImpl(View v) {
                // Toggle between 24H and 30D timeframe.
                if("24H".equals(timeframe)) {
                    timeframe = "30D";
                }
                else if("30D".equals(timeframe)) {
                    timeframe = "24H";
                }
                updateInfo();
                updateTimeframe();
                drawChart();
            }
        });

        B_POINTS = new AppCompatImageButton(context);
        B_POINTS.setLayoutParams(new LayoutParams(buttonSize, buttonSize));
        B_POINTS.setOnClickListener(new CrashView.CrashOnClickListener(context) {
            public void onClickImpl(View v) {
                // Toggle between point, line, and candle.
                if("POINT".equals(pointsType)) {
                    pointsType = "LINE";
                }
                else if("LINE".equals(pointsType)) {
                    pointsType = "CANDLE";
                }
                else if("CANDLE".equals(pointsType)) {
                    pointsType = "POINT";
                }
                updateInfo();
                updatePoints();
                drawChart();
            }
        });

        B_SCALE = new AppCompatImageButton(context);
        B_SCALE.setLayoutParams(new LayoutParams(buttonSize, buttonSize));
        B_SCALE.setOnClickListener(new CrashView.CrashOnClickListener(context) {
            public void onClickImpl(View v) {
                // Toggle between linear and log scale.
                isLogScale = !isLogScale;
                updateInfo();
                updateScale();
                drawChart();
            }
        });

        B_STYLE = new AppCompatImageButton(context);
        B_STYLE.setLayoutParams(new LayoutParams(buttonSize, buttonSize));
        B_STYLE.setOnClickListener(new CrashView.CrashOnClickListener(context) {
            public void onClickImpl(View v) {
                isHollowStyle = !isHollowStyle;
                updateInfo();
                updateStyle();
                drawChart();
            }
        });

        surfaceView = new SurfaceView(context);
        surfaceView.setZOrderOnTop(true);
        surfaceView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                drawChart();
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {}

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {}
        });

        L_BUTTONS.addView(B_TIMEFRAME);
        L_BUTTONS.addView(B_POINTS);
        L_BUTTONS.addView(B_SCALE);
        L_BUTTONS.addView(B_STYLE);

        L_CHART.addView(L_BUTTONS);
        L_CHART.addView(surfaceView);

        this.addView(T_INFO);
        this.addView(L_CHART);

        updateInfo();
        updateTimeframe();
        updatePoints();
        updateScale();
        updateStyle();
    }

    public void updateInfo() {
        // When this View is first inflated, we don't have anything available.
        if(chartData == null) { return; }

        String infoString = chartData.cryptoChart.toString();

        // If we have the info, add on the start/end times.
        boolean isCandle = "CANDLE".equals(pointsType);
        if((isCandle && !chartData.isCandlesComplete()) || (!isCandle && !chartData.isPricePointsComplete())) {
            infoString = infoString + "\nNo Chart Data Found.";
        }
        else {
            Timestamp bottomTimestamp = new Timestamp(new Date(getBottomTime().longValue()));
            Timestamp topTimestamp = new Timestamp(new Date(getTopTime().longValue()));

            infoString = infoString + "\nChart Start Time: " + bottomTimestamp + "\nChart End Time: " + topTimestamp;
        }
        T_INFO.setText(infoString);
    }

    public void updateTimeframe() {
        if("24H".equals(timeframe)) {
            B_TIMEFRAME.setImageResource(R.drawable.ic_baseline_access_time_24);
        }
        else if("30D".equals(timeframe)) {
            B_TIMEFRAME.setImageResource(R.drawable.ic_baseline_calendar_month_24);
        }
    }

    public void updatePoints() {
        if("POINT".equals(pointsType)) {
            B_POINTS.setImageResource(R.drawable.ic_baseline_scatter_plot_24);
        }
        else if("LINE".equals(pointsType)) {
            B_POINTS.setImageResource(R.drawable.ic_baseline_show_chart_24);
        }
        else if("CANDLE".equals(pointsType)) {
            B_POINTS.setImageResource(R.drawable.ic_baseline_candlestick_chart_24);
        }
    }

    public void updateScale() {
        if(isLogScale) {
            B_SCALE.setImageResource(R.drawable.ic_baseline_log_scale_24);
        }
        else {
            B_SCALE.setImageResource(R.drawable.ic_baseline_linear_scale_24);
        }
    }

    public void updateStyle() {
        if(isHollowStyle) {
            B_STYLE.setImageResource(R.drawable.ic_baseline_hollow_circle_24);
        }
        else {
            B_STYLE.setImageResource(R.drawable.ic_baseline_circle_24);
        }
    }

    public void draw(ChartData chartData) {
        this.chartData = chartData;
        drawChart();
        updateInfo();
    }

    public void drawChart() {
        // Early returns if data isn't complete or the canvas isn't ready.
        if(chartData == null) { return; }

        boolean isCandle = "CANDLE".equals(pointsType);
        if(isCandle && !chartData.isCandlesComplete()) { return; }
        if(!isCandle && !chartData.isPricePointsComplete()) { return; }

        Canvas canvas = surfaceView.getHolder().lockCanvas();
        if(canvas == null) { return; }

        // Calculate these values upfront.
        canvasWidth = canvas.getWidth();
        canvasHeight = canvas.getHeight();
        topPrice = getTopPrice();
        bottomPrice = getBottomPrice();
        topTime = getTopTime();
        bottomTime = getBottomTime();

        textHeightTop = getTextHeight(topPrice.toPlainString());
        textHeightBottom = getTextHeight(bottomPrice.toPlainString());

        // Draw all the graphical parts of the chart.
        drawBackground(canvas);
        drawText(canvas);
        drawAxes(canvas);
        drawXAxisTicks(canvas);
        drawYAxisTicks(canvas);
        drawPriceData(canvas);
        surfaceView.getHolder().unlockCanvasAndPost(canvas);
    }

    private int getTextHeight(String text) {
        TextPaint textPaint = new TextPaint(TextPaint.ANTI_ALIAS_FLAG);
        textPaint.setTextAlign(Paint.Align.LEFT);
        textPaint.setTextSize(fontSize);
        textPaint.getTextBounds(text, 0, text.length(), r);
        return r.height();
    }



    private void drawBackground(Canvas canvas) {
        Paint backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backgroundPaint.setColor(AppearanceUtil.getPrimaryColor(getContext()));
        canvas.drawRect(0, 0, canvasWidth, canvasHeight, backgroundPaint);
    }

    private void drawText(Canvas canvas) {
        TextPaint textPaint = new TextPaint(TextPaint.ANTI_ALIAS_FLAG);
        textPaint.setColor(AppearanceUtil.getSecondaryColor(getContext()));
        textPaint.setTextAlign(Paint.Align.LEFT);
        textPaint.setTextSize(fontSize);

        // Top Price Label
        String topPriceString = topPrice.toPlainString();
        textPaint.getTextBounds(topPriceString, 0, topPriceString.length(), r);
        canvas.drawText(topPriceString, -r.left, -r.top, textPaint);

        // Bottom Price Label
        String bottomPriceString = bottomPrice.toPlainString();
        textPaint.getTextBounds(bottomPriceString, 0, bottomPriceString.length(), r);
        canvas.drawText(bottomPriceString, -r.left, canvasHeight - r.bottom, textPaint);

        // Time Label
        String timeLabel = timeframe;
        textPaint.getTextBounds(timeLabel, 0, timeLabel.length(), r);
        canvas.drawText(timeLabel, canvasWidth/2f - r.width()/2f -r.left, canvasHeight - r.bottom, textPaint);
    }

    private void drawAxes(Canvas canvas) {
        Paint axisPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        axisPaint.setColor(AppearanceUtil.getSecondaryColor(getContext()));
        axisPaint.setStrokeWidth(axisStrokeWidth);
        axisPaint.setStyle(Paint.Style.STROKE);

        Path path = new Path();
        path.moveTo(axisOffsetLeft, textHeightTop + axisOffsetTop); // Top
        path.lineTo(axisOffsetLeft, canvasHeight - textHeightBottom - axisOffsetBottom); // Origin
        path.lineTo(canvasWidth - axisOffsetRight, canvasHeight - textHeightBottom - axisOffsetBottom); // Right

        canvas.drawPath(path, axisPaint);
    }

    private void drawXAxisTicks(Canvas canvas) {
        // Draw evenly spaced ticks based on the time frame chosen.
        // We would need 1 more tick than the number, but than we subtract 1 because we don't draw a tick at the origin.
        Paint axisTickXPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        axisTickXPaint.setColor(AppearanceUtil.getSecondaryColor(getContext()));
        axisTickXPaint.setStrokeWidth(axisTickStrokeWidthX);

        int numTicks = 24;
        for(int tick = 1; tick <= numTicks;  tick++) {
            BigDecimal n = new BigDecimal(tick).divide(new BigDecimal(numTicks), 50, RoundingMode.HALF_UP);
            float canvasPositionX = getCanvasX(n);
            canvas.drawLine(canvasPositionX, canvasHeight - textHeightBottom - axisOffsetBottom + axisTickWidthX/2f, canvasPositionX, canvasHeight - textHeightBottom - axisOffsetBottom - axisTickWidthX/2f, axisTickXPaint);
        }
    }

    private void drawYAxisTicks(Canvas canvas) {
        // Draw 10 evenly spaced ticks for price.
        // We would need 1 more tick than the number, but than we subtract 1 because we don't draw a tick at the origin.
        Paint axisTickYPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        axisTickYPaint.setColor(AppearanceUtil.getSecondaryColor(getContext()));
        axisTickYPaint.setStrokeWidth(axisTickStrokeWidthY);

        int numTicks = 10;
        for(int tick = 1; tick <= numTicks;  tick++) {
            BigDecimal n = new BigDecimal(tick).divide(new BigDecimal(numTicks), 50, RoundingMode.HALF_UP);
            float canvasPositionY = getCanvasY(n);
            canvas.drawLine(axisOffsetLeft - axisTickWidthY/2f, canvasPositionY, axisOffsetLeft + axisTickWidthY/2f, canvasPositionY, axisTickYPaint);
        }
    }

    private void drawPriceData(Canvas canvas) {
        if("POINT".equals(pointsType)) {
            drawPoints(canvas);
        }
        else if("LINE".equals(pointsType)) {
            drawLines(canvas);
        }
        else if("CANDLE".equals(pointsType)) {
            drawCandles(canvas);
        }
    }

    private void drawPoints(Canvas canvas) {
        Paint pointPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        pointPaint.setColor(AppearanceUtil.getSecondaryColor(getContext()));
        pointPaint.setStrokeWidth(candleStrokeWidth);
        if(isHollowStyle) {
            pointPaint.setStyle(Paint.Style.STROKE);
        }
        else {
            pointPaint.setStyle(Paint.Style.FILL);
        }

        ArrayList<BigDecimal> time = new ArrayList<>();
        ArrayList<BigDecimal> price = new ArrayList<>();

        ArrayList<PricePoint> timeframePricePointsArrayList = HashMapUtil.getValueFromMap(chartData.pricePointsHashMap, timeframe);

        for(PricePoint pricePoint : timeframePricePointsArrayList) {
            // Normalize the times and prices here.
            time.add(normalizeTime(new BigDecimal(pricePoint.timestamp.date.getTime())));
            price.add(normalizePrice(pricePoint.price));
        }

        for(int i = 0; i < timeframePricePointsArrayList.size(); i++) {
            canvas.drawCircle(getCanvasX(time.get(i)), getCanvasY(price.get(i)), pointRadius, pointPaint);
        }
    }

    private void drawLines(Canvas canvas) {
        Paint linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setColor(AppearanceUtil.getSecondaryColor(getContext()));
        linePaint.setStrokeWidth(lineStrokeWidth);
        linePaint.setStyle(Paint.Style.STROKE);
        if(isHollowStyle) {
            linePaint.setPathEffect(new DashPathEffect(new float[]{5, 5}, 0));
        }

        ArrayList<BigDecimal> time = new ArrayList<>();
        ArrayList<BigDecimal> price = new ArrayList<>();

        ArrayList<PricePoint> timeframePricePointsArrayList = HashMapUtil.getValueFromMap(chartData.pricePointsHashMap, timeframe);

        for(PricePoint pricePoint : timeframePricePointsArrayList) {
            // Normalize the times and prices here.
            time.add(normalizeTime(new BigDecimal(pricePoint.timestamp.date.getTime())));
            price.add(normalizePrice(pricePoint.price));
        }

        Path path = new Path();
        for(int i = 0; i < timeframePricePointsArrayList.size(); i++) {
            if(i == 0) {
                path.moveTo(getCanvasX(time.get(i)), getCanvasY(price.get(i)));
            }
            else {
                path.lineTo(getCanvasX(time.get(i)), getCanvasY(price.get(i)));
            }
        }

        canvas.drawPath(path, linePaint);
    }

    private void drawCandles(Canvas canvas) {
        Paint candlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        candlePaint.setStrokeWidth(candleStrokeWidth);
        if(isHollowStyle) {
            candlePaint.setStyle(Paint.Style.STROKE);
        }
        else {
            candlePaint.setStyle(Paint.Style.FILL);
        }

        ArrayList<Candle> timeframeCandlesArrayList = HashMapUtil.getValueFromMap(chartData.candlesHashMap, timeframe);

        for(Candle candle : timeframeCandlesArrayList) {
            // Normalize the times and prices here.
            BigDecimal time = normalizeTime(new BigDecimal(candle.timestamp.date.getTime()));
            BigDecimal openPrice = normalizePrice(candle.openPrice);
            BigDecimal highPrice = normalizePrice(candle.highPrice);
            BigDecimal lowPrice = normalizePrice(candle.lowPrice);
            BigDecimal closePrice = normalizePrice(candle.closePrice);
            drawCandle(canvas, candlePaint, time, openPrice, highPrice, lowPrice, closePrice);
        }
    }

    private void drawCandle(Canvas canvas, Paint paint, BigDecimal time, BigDecimal open, BigDecimal high, BigDecimal low, BigDecimal close) {
        // Draw an individual candle.
        BigDecimal boxMin;
        BigDecimal boxMax;
        int c = open.compareTo(close);
        if(c > 0) {
            boxMin = close;
            boxMax = open;
            paint.setColor(Color.RED);
        }
        else if(c < 0) {
            boxMin = open;
            boxMax = close;
            paint.setColor(Color.GREEN);
        }
        else {
            // If they are exactly equal, just use black so we don't have to arbitrarily choose red or green.
            boxMin = close;
            boxMax = open;
            paint.setColor(Color.BLACK);
        }

        float centerX = getCanvasX(time);

        // Draw the box of the candle.
        float boxLeft = centerX - candleWidth/2f;
        float boxRight = centerX + candleWidth/2f;
        float boxTop = getCanvasY(boxMax);
        float boxBottom = getCanvasY(boxMin);
        canvas.drawRect(boxLeft, boxTop, boxRight, boxBottom, paint);

        // Draw the whiskers.
        float whiskerTop = getCanvasY(high);
        float whiskerBottom = getCanvasY(low);
        canvas.drawLine(centerX, boxTop, centerX, whiskerTop, paint);
        canvas.drawLine(centerX, boxBottom, centerX, whiskerBottom, paint);
    }

    private BigDecimal normalizeTime(BigDecimal time) {
        // Input is absolute time, output is the normalized time.
        return time.subtract(bottomTime).divide(topTime.subtract(bottomTime), 50, RoundingMode.HALF_UP);
    }

    private BigDecimal normalizePrice(BigDecimal price) {
        // Input is absolute price, output is the normalized price.
        return price.subtract(bottomPrice).divide(topPrice.subtract(bottomPrice), 50, RoundingMode.HALF_UP);
    }

    private float getCanvasX(BigDecimal n) {
        // Input is normalized X value on our graph, output is absolute X value on the canvas.
        // X-axis is not affected by the scale.
        return axisOffsetLeft + (canvasWidth - axisOffsetLeft - axisOffsetRight) * n.floatValue();
    }

    private float getCanvasY(BigDecimal n) {
        // Input is normalized Y value on our graph, output is absolute Y value on the canvas.
        // Remember that the Y-axis is affected by the scale, and that the graph Y-axis and the canvas Y-axis have opposite positive directions.
        float nF = n.floatValue();
        if(isLogScale) {
            nF = (float)Math.log10(1 + 9 * nF);
        }

        int totalOffsetTop = axisOffsetTop + textHeightTop;
        int totalOffsetBottom = axisOffsetBottom + textHeightBottom;
        return canvasHeight - totalOffsetBottom - (canvasHeight - totalOffsetBottom - totalOffsetTop) * nF;
    }

    private BigDecimal getTopPrice() {
        // Return a price that is higher than any price in the data.
        BigDecimal maxPrice = HashMapUtil.getValueFromMap(chartData.maxPriceHashMap, timeframe);

        int scale = maxPrice.scale();
        int precision = maxPrice.precision();

        int tenPower = precision - scale - 1;
        int firstDigits = maxPrice.movePointLeft(tenPower - zoomLevel).intValue() + 1;

        return new BigDecimal(firstDigits).movePointRight(tenPower - zoomLevel);
    }

    private BigDecimal getBottomPrice() {
        // Return a price that is lower than any price in the data.
        BigDecimal minPrice = HashMapUtil.getValueFromMap(chartData.minPriceHashMap, timeframe);

        int scale = minPrice.scale();
        int precision = minPrice.precision();

        int tenPower = precision - scale - 1;
        int firstDigit = minPrice.movePointLeft(tenPower - zoomLevel).intValue();

        return new BigDecimal(firstDigit).movePointRight(tenPower - zoomLevel);
    }

    private BigDecimal getTopTime() {
        // For 24H, the top time is the max time rounded up to the hour.
        // For 30D, the top time is the max time rounded up to the day.
        BigDecimal maxTime = HashMapUtil.getValueFromMap(chartData.maxTimeHashMap, timeframe);
        long divisor = 0;
        if("24H".equals(timeframe)) {
            divisor = 60 * 60 * 1000;
        }
        else if("30D".equals(timeframe)) {
            divisor = 24L * 60L * 60L * 1000L;
        }

        return new BigDecimal(divisor * ((maxTime.longValue() / divisor) + 1));
    }

    private BigDecimal getBottomTime() {
        // The bottom time of any graph is 24H less than the max/top time.
        BigDecimal maxTime = HashMapUtil.getValueFromMap(chartData.maxTimeHashMap, timeframe);
        BigDecimal interval = null;
        if("24H".equals(timeframe)) {
            interval = new BigDecimal(24 * 60 * 60 * 1000);
        }
        else if("30D".equals(timeframe)) {
            interval = new BigDecimal(30L * 24L * 60L * 60L * 1000L);
        }

        return maxTime.subtract(interval);
    }

    @Override
    public Parcelable onSaveInstanceStateImpl(Parcelable state)
    {
        Bundle bundle = new Bundle();
        bundle.putParcelable("superState", state);

        bundle.putString("timeframe", timeframe);
        bundle.putString("pointsType", pointsType);
        bundle.putBoolean("isLogScale", isLogScale);
        bundle.putBoolean("isHollowStyle", isHollowStyle);

        return bundle;
    }

    @Override
    public Parcelable onRestoreInstanceStateImpl(Parcelable state)
    {
        if (state instanceof Bundle) // implicit null check
        {
            Bundle bundle = (Bundle) state;
            state = bundle.getParcelable("superState");

            timeframe = bundle.getString("timeframe");
            pointsType = bundle.getString("pointsType");
            isLogScale = bundle.getBoolean("isLogScale");
            isHollowStyle = bundle.getBoolean("isHollowStyle");

            updateInfo();
            updateTimeframe();
            updatePoints();
            updateScale();
            updateStyle();

            // Don't update graph here. This has to be done via callback when the Surface is created.
        }
        return state;
    }
}