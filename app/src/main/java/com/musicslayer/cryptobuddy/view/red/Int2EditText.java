package com.musicslayer.cryptobuddy.view.red;

import android.content.Context;
import android.util.AttributeSet;

import java.math.BigInteger;

public class Int2EditText extends RedEditText {
    public Int2EditText(Context context) {
        this(context, null);
    }

    public Int2EditText(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    // Returns if the value is an integer with up to 2 digits.
    public boolean condition() {
        try {
            BigInteger scale = new BigInteger(this.getTextString());
            return scale.compareTo(BigInteger.ZERO) >= 0 && scale.compareTo(BigInteger.valueOf(99)) <= 0;
        }
        catch(Exception e) {
            return false;
        }
    }
}
