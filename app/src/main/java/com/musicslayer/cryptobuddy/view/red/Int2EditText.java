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

        // Enforce a maximum length to protect algorithms from having to process large values.
        // For now, all of these only need 2 digits.
        setMaxLength(2);
    }

    // Returns if the value is an integer with up to 2 digits.
    public boolean condition() {
        try {
            BigInteger value = new BigInteger(this.getTextString());
            return value.compareTo(BigInteger.ZERO) >= 0 && value.compareTo(BigInteger.valueOf(99)) <= 0;
        }
        catch(Exception e) {
            return false;
        }
    }
}
