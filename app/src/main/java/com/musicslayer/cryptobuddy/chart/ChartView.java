package com.musicslayer.cryptobuddy.chart;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatTextView;

import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.api.chart.Candle;
import com.musicslayer.cryptobuddy.api.chart.ChartData;
import com.musicslayer.cryptobuddy.api.chart.PricePoint;
import com.musicslayer.cryptobuddy.crash.CrashLinearLayout;
import com.musicslayer.cryptobuddy.crash.CrashView;
import com.musicslayer.cryptobuddy.util.ThrowableUtil;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;

// Each ChartView instance represents a single graphical chart.
public class ChartView extends CrashLinearLayout {
    String info = "A"; // TODO Later, we can fill in first price, last price, % change
    String timeLabel = "24H";
    boolean isCandle;
    boolean isLogScale;

    // Graphical properties of the chart.
    int buttonSize = 150;
    float fontSize = 48f;
    int lineStrokeWidth = 5;
    int candleWidth = 10;
    int axisOffsetLeft = 10;
    int axisOffsetTop = 10;
    int axisOffsetRight = 10;
    int axisOffsetBottom = 10;
    int axisStrokeWidth = 5; // X and Y always match.
    int axisTickWidthX = 20;
    int axisTickWidthY = 20;
    int axisTickStrokeWidthX = 1;
    int axisTickStrokeWidthY = 1;

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
    AppCompatImageButton B_TIME;
    AppCompatImageButton B_POINTS;
    AppCompatImageButton B_SCALE;
    AppCompatImageButton B_REFRESH;

    public ChartView(Context context) {
        this(context, null);
    }

    public ChartView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.makeLayout();
    }

    public void updateInfo(String info) {
        this.info = info;
        makeLayout();
    }

    public void updateIsCandle(boolean isCandle) {
        this.isCandle = isCandle;
        makeLayout();
    }

    public void draw(ArrayList<ChartData> chartDataArrayList) {
        // TODO For now, assume there is only one chart.
        this.chartData = chartDataArrayList.get(0);
        drawChart();
    }

    public void makeLayout() {
        Context context = getContext();

        this.setOrientation(VERTICAL);

        TextView T_INFO = new AppCompatTextView(context);
        if(info != null) {
            T_INFO.setText(info);
        }

        LinearLayout L_CHART = new LinearLayout(context);
        L_CHART.setOrientation(HORIZONTAL);

        // Create the vertical column of buttons along the side.
        LinearLayout L_BUTTONS = new LinearLayout(context);
        L_BUTTONS.setOrientation(VERTICAL);
        L_BUTTONS.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

        B_TIME = new AppCompatImageButton(context);
        B_TIME.setImageResource(R.drawable.ic_baseline_access_time_24);
        B_TIME.setLayoutParams(new LayoutParams(buttonSize, buttonSize));
        B_TIME.setOnClickListener(new CrashView.CrashOnClickListener(context) {
            public void onClickImpl(View v) {
                // Open spinner so user can choose timeframe of data.
            }
        });

        B_POINTS = new AppCompatImageButton(context);
        B_POINTS.setLayoutParams(new LayoutParams(buttonSize, buttonSize));
        B_POINTS.setOnClickListener(new CrashView.CrashOnClickListener(context) {
            public void onClickImpl(View v) {
                // Toggle between line and candle.
                isCandle = !isCandle;
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
                updateScale();
                drawChart();
            }
        });

        B_REFRESH = new AppCompatImageButton(context);
        B_REFRESH.setImageResource(R.drawable.ic_baseline_refresh_24);
        B_REFRESH.setLayoutParams(new LayoutParams(buttonSize, buttonSize));
        B_REFRESH.setOnClickListener(new CrashView.CrashOnClickListener(context) {
            public void onClickImpl(View v) {
                // TODO Re-download data and refresh graph??? Can we do this here?
            }
        });

        surfaceView = new SurfaceView(context);
        surfaceView.setZOrderOnTop(true);
        surfaceView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                try {
                    drawChart();

                }
                catch(Exception e) {
                    ThrowableUtil.processThrowable(e);
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                try {
                }
                catch(Exception e) {
                    ThrowableUtil.processThrowable(e);
                }
            }
        });

        L_BUTTONS.addView(B_TIME);
        L_BUTTONS.addView(B_POINTS);
        L_BUTTONS.addView(B_SCALE);
        L_BUTTONS.addView(B_REFRESH);

        L_CHART.addView(L_BUTTONS);
        L_CHART.addView(surfaceView);

        this.addView(T_INFO);
        this.addView(L_CHART);

        updatePoints();
        updateScale();
    }

    public void updatePoints() {
        if(isCandle) {
            B_POINTS.setImageResource(R.drawable.ic_baseline_candlestick_chart_24);
        }
        else {
            B_POINTS.setImageResource(R.drawable.ic_baseline_show_chart_24);
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

    public void drawChart() {
        if(chartData == null) { return; }
        if(isCandle && !chartData.isCandlesComplete()) { return; }
        if(!isCandle && !chartData.isPricePointsComplete()) { return; }

        Canvas canvas = surfaceView.getHolder().lockCanvas();

        // First, calculate these values upfront.
        canvasWidth = canvas.getWidth();
        canvasHeight = canvas.getHeight();

        if(isCandle) {
            topPrice = getTopPrice(chartData.getMaxCandlesPrice());
            bottomPrice = getBottomPrice(chartData.getMinCandlesPrice());
            topTime = getTopTime(chartData.getMaxCandlesTime());
            bottomTime = getBottomTime(chartData.getMinCandlesTime());
        }
        else {
            topPrice = getTopPrice(chartData.getMaxPricePointsPrice());
            bottomPrice = getBottomPrice(chartData.getMinPricePointsPrice());
            topTime = getTopTime(chartData.getMaxPricePointsTime());
            bottomTime = getBottomTime(chartData.getMinPricePointsTime());
        }

        textHeightTop = getTextHeight(topPrice.toPlainString());
        textHeightBottom = getTextHeight(bottomPrice.toPlainString());

        Paint backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backgroundPaint.setColor(Color.WHITE);

        TextPaint textPaint = new TextPaint(TextPaint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.BLACK);
        textPaint.setTextAlign(Paint.Align.LEFT);
        textPaint.setTextSize(fontSize);

        Paint axisPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        axisPaint.setColor(Color.BLACK);
        axisPaint.setStrokeWidth(axisStrokeWidth);
        axisPaint.setStyle(Paint.Style.STROKE);

        Paint axisTickXPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        axisTickXPaint.setColor(Color.BLACK);
        axisTickXPaint.setStrokeWidth(axisTickStrokeWidthX);

        Paint axisTickYPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        axisTickYPaint.setColor(Color.BLACK);
        axisTickYPaint.setStrokeWidth(axisTickStrokeWidthY);

        Paint pricePointPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        pricePointPaint.setColor(Color.BLUE);
        pricePointPaint.setStrokeWidth(lineStrokeWidth);
        pricePointPaint.setStyle(Paint.Style.STROKE);

        Paint candlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        pricePointPaint.setStrokeWidth(lineStrokeWidth);
        pricePointPaint.setStyle(Paint.Style.STROKE);

        // Background
        canvas.drawRect(0, 0, canvasWidth, canvasHeight, backgroundPaint);

        // Top and bottom price labels.
        drawPriceTop(canvas, textPaint, "30000");
        drawPriceBottom(canvas, textPaint, "20000");

        // Time Label
        drawTimeLabel(canvas, textPaint, timeLabel);

        // Draw axes
        drawAxes(canvas, axisPaint);
        drawXAxisTicks(canvas, axisTickXPaint);
        drawYAxisTicks(canvas, axisTickYPaint);

        // Draw price points.
        if(isCandle) {
            drawCandles(canvas, candlePaint);
        }
        else {
            drawLines(canvas, pricePointPaint);
        }

        surfaceView.getHolder().unlockCanvasAndPost(canvas);
    }

    private int getTextHeight(String text) {
        TextPaint textPaint = new TextPaint(TextPaint.ANTI_ALIAS_FLAG);
        textPaint.setTextAlign(Paint.Align.LEFT);
        textPaint.setTextSize(fontSize);
        textPaint.getTextBounds(text, 0, text.length(), r);
        return r.height();
    }

    private void drawPriceTop(Canvas canvas, Paint paint, String text) {
        paint.getTextBounds(text, 0, text.length(), r);
        canvas.drawText(text, -r.left, -r.top, paint);
    }

    private void drawPriceBottom(Canvas canvas, Paint paint, String text) {
        paint.getTextBounds(text, 0, text.length(), r);
        canvas.drawText(text, -r.left, canvasHeight - r.bottom, paint);
    }

    private void drawTimeLabel(Canvas canvas, Paint paint, String text) {
        paint.getTextBounds(text, 0, text.length(), r);
        canvas.drawText(text, canvasWidth/2f - r.width()/2f -r.left, canvasHeight - r.bottom, paint);
    }

    private void drawAxes(Canvas canvas, Paint paint) {
        Path path = new Path();
        path.moveTo(axisOffsetLeft, textHeightTop + axisOffsetTop); // Top
        path.lineTo(axisOffsetLeft, canvasHeight - textHeightBottom - axisOffsetBottom); // Origin
        path.lineTo(canvasWidth - axisOffsetRight, canvasHeight - textHeightBottom - axisOffsetBottom); // Right

        canvas.drawPath(path, paint);
    }

    private void drawXAxisTicks(Canvas canvas, Paint paint) {
        // Draw evenly spaced ticks based on the time frame chosen.
        // X-axis is not affected by the scale.

        // We would need 1 more tick than the number, but than we subtract 1 because we don't draw a tick at the origin.
        int numTicks = 24;
        for(int tick = 1; tick <= numTicks;  tick++) {
            BigDecimal n = new BigDecimal(tick).divide(new BigDecimal(numTicks), 50, RoundingMode.HALF_UP);
            float canvasPositionX = getCanvasX(n);
            canvas.drawLine(canvasPositionX, canvasHeight - textHeightBottom - axisOffsetBottom + axisTickWidthX/2f, canvasPositionX, canvasHeight - textHeightBottom - axisOffsetBottom - axisTickWidthX/2f, paint);
        }
    }

    private void drawYAxisTicks(Canvas canvas, Paint paint) {
        // Draw 10 evenly spaced ticks for price.

        // We would need 1 more tick than the number, but than we subtract 1 because we don't draw a tick at the origin.
        int numTicks = 10;
        for(int tick = 1; tick <= numTicks;  tick++) {
            BigDecimal n = new BigDecimal(tick).divide(new BigDecimal(numTicks), 50, RoundingMode.HALF_UP);
            float canvasPositionY = getCanvasY(n);
            canvas.drawLine(axisOffsetLeft - axisTickWidthY/2f, canvasPositionY, axisOffsetLeft + axisTickWidthY/2f, canvasPositionY, paint);
        }
    }

    private void drawLines(Canvas canvas, Paint paint) {
        /*
        // Fill in fake price data.
        ArrayList<Float> time = new ArrayList<>();
        time.add(0f);
        time.add(0.05f);
        time.add(0.1f);
        time.add(0.15f);
        time.add(0.2f);
        time.add(0.25f);
        time.add(0.3f);
        time.add(0.35f);
        time.add(0.4f);
        time.add(0.45f);
        time.add(0.5f);
        time.add(0.55f);
        time.add(0.6f);
        time.add(0.65f);
        time.add(0.7f);
        time.add(0.75f);
        time.add(0.8f);
        time.add(0.85f);
        time.add(0.9f);
        time.add(0.95f);
        time.add(1f);

        ArrayList<Float> price = new ArrayList<>();
        price.add(1f);
        price.add(0.95f);
        price.add(0.9f);
        price.add(0.85f);
        price.add(0.8f);
        price.add(0.75f);
        price.add(0.7f);
        price.add(0.65f);
        price.add(0.6f);
        price.add(0.55f);
        price.add(0.5f);
        price.add(0.45f);
        price.add(0.4f);
        price.add(0.35f);
        price.add(0.3f);
        price.add(0.25f);
        price.add(0.2f);
        price.add(0.15f);
        price.add(0.1f);
        price.add(0.05f);
        price.add(0.0f);

         */

        ArrayList<BigDecimal> time = new ArrayList<>();
        ArrayList<BigDecimal> price = new ArrayList<>();

        for(PricePoint pricePoint : chartData.pricePointsArrayList) {
            // Normalize the times and prices here.
            time.add(normalizeTime(new BigDecimal(pricePoint.timestamp.date.getTime())));
            price.add(normalizePrice(pricePoint.price));
        }

        Path path = new Path();
        for(int i = 0; i < time.size(); i++) {
            if(i == 0) {
                path.moveTo(getCanvasX(time.get(i)), getCanvasY(price.get(i)));
            }
            else {
                path.lineTo(getCanvasX(time.get(i)), getCanvasY(price.get(i)));
            }
        }

        canvas.drawPath(path, paint);
    }

    private void drawCandles(Canvas canvas, Paint paint) {
    /*
        // Fill in fake candle data.
        ArrayList<Float> timeArrayList = new ArrayList<>();
        timeArrayList.add(0.2f);
        timeArrayList.add(0.4f);

        ArrayList<Float[]> priceArrayList = new ArrayList<>();
        priceArrayList.add(new Float[]{22000f, 28000f, 20000f, 25000f});
        priceArrayList.add(new Float[]{25000f, 30000f, 23000f, 24000f});

     */

        for(Candle candle : chartData.candlesArrayList) {
            // Normalize the times and prices here.
            BigDecimal time = normalizeTime(new BigDecimal(candle.timestamp.date.getTime()));
            BigDecimal openPrice = normalizePrice(candle.openPrice);
            BigDecimal highPrice = normalizePrice(candle.highPrice);
            BigDecimal lowPrice = normalizePrice(candle.lowPrice);
            BigDecimal closePrice = normalizePrice(candle.closePrice);
            drawCandle(canvas, paint, time, openPrice, highPrice, lowPrice, closePrice);
        }
    }

    public void drawCandle(Canvas canvas, Paint paint, BigDecimal time, BigDecimal open, BigDecimal high, BigDecimal low, BigDecimal close) {
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

        // TODO Filled or hollow candle?
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

    public BigDecimal normalizeTime(BigDecimal time) {
        // Input is absolute time, output is the normalized time.
        return time.subtract(bottomTime).divide(topTime.subtract(bottomTime), 50, RoundingMode.HALF_UP);
    }

    public BigDecimal normalizePrice(BigDecimal price) {
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
        // Remember that the graph Y-axis and the canvas Y-axis have opposite positive directions.
        float nF = n.floatValue();
        if(isLogScale) {
            nF = (float)Math.log10(1 + 9 * nF);
        }

        int totalOffsetTop = axisOffsetTop + textHeightTop;
        int totalOffsetBottom = axisOffsetBottom + textHeightBottom;
        return canvasHeight - totalOffsetBottom - (canvasHeight - totalOffsetBottom - totalOffsetTop) * nF;
    }

    // TODO Come up with better range of prices.
    public BigDecimal getTopPrice(BigDecimal maxPrice) {
        // Return a price that is higher than any price in the data, rounding up the leftmost digit.
        // For example, with a max price of 23000, the top price will be 30000.
        int scale = maxPrice.scale();
        int precision = maxPrice.precision();

        int tenPower = precision - scale - 1;
        int firstDigit = maxPrice.movePointLeft(tenPower).intValue() + 1;

        return new BigDecimal(firstDigit).movePointRight(tenPower);
    }

    public BigDecimal getBottomPrice(BigDecimal minPrice) {
        // Return a price that is lower than any price in the data, rounding down the leftmost digit.
        // For example, with a min price of 23000, the bottom price will be 20000.
        int scale = minPrice.scale();
        int precision = minPrice.precision();

        int tenPower = precision - scale - 1;
        int firstDigit = minPrice.movePointLeft(tenPower).intValue();

        return new BigDecimal(firstDigit).movePointRight(tenPower);
    }

    // TODO Do we really need max/min time?
    public BigDecimal getTopTime(BigDecimal maxTime) {
        // The top time of any graph is just the max time.
        return maxTime;
    }

    public BigDecimal getBottomTime(BigDecimal minTime) {
        // The bottom time of any graph is 24H less than the top time.
        return topTime.subtract(new BigDecimal(24 * 60 * 60 * 1000));
    }
}
