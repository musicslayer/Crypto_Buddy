package com.musicslayer.cryptobuddy.chart;

public class ChartConfig {
    String timeframe;
    String pointsType;
    boolean isLogScale;
    boolean isHollowStyle;
    int LAST_CHECK;

    public ChartConfig(String timeframe, String pointsType, boolean isLogScale, boolean isHollowStyle, int LAST_CHECK) {
        this.timeframe = timeframe;
        this.pointsType = pointsType;
        this.isLogScale = isLogScale;
        this.isHollowStyle = isHollowStyle;
        this.LAST_CHECK = LAST_CHECK;
    }
}
