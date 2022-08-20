package com.musicslayer.cryptobuddy.view.red;

import android.content.Context;
import android.util.AttributeSet;

import java.math.BigDecimal;

public class NumericEditText extends RedEditText {
    public NumericEditText(Context context) {
        this(context, null);
    }

    public NumericEditText(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);

        // Enforce a maximum length to protect algorithms from having to process large values.
        // For now, any asset amount should not need more than 50 digits.
        setMaxLength(50);
    }

    // Returns if the value is a number.
    public boolean condition() {
        try {
            new BigDecimal(this.getTextString());
            return true;
        }
        catch(Exception ignored) {
            return false;
        }
    }
}
