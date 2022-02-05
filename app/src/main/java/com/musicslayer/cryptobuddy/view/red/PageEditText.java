package com.musicslayer.cryptobuddy.view.red;

import android.content.Context;
import android.util.AttributeSet;

import java.math.BigInteger;

public class PageEditText extends RedEditText {
    int pageMin;
    int pageMax;

    public PageEditText(Context context) {
        this(context, null);
    }

    public PageEditText(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);

        // Enforce a maximum length to protect algorithms from having to process large values.
        // For now, there should not be more than 5 digits of pages.
        setMaxLength(5);
    }

    public void setPageMinMax(int pageMin, int pageMax) {
        this.pageMin = pageMin;
        this.pageMax = pageMax;
    }

    // Returns if the value is an integer between the min and max (both inclusive).
    public boolean condition() {
        try {
            int value = new BigInteger(this.getTextString()).intValue();
            return value >= pageMin && value <= pageMax;
        }
        catch(Exception e) {
            return false;
        }
    }
}
