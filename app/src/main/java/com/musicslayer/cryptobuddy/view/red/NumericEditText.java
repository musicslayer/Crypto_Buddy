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
    }

    // Returns if the value is a number.
    public boolean condition() {
        try {
            new BigDecimal(this.getText().toString());
            return true;
        }
        catch(java.lang.Exception e) {
            return false;
        }
    }
}
