package com.musicslayer.cryptobuddy.view.red;

import android.content.Context;
import android.util.AttributeSet;

public class PlainTextEditText extends RedEditText {
    public PlainTextEditText(Context context) {
        this(context, null);
    }

    public PlainTextEditText(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    // Returns if the value is nonempty text.
    public boolean condition() {
        try {
            return this.getText().toString().length() != 0;
        }
        catch(Exception e) {
            return false;
        }
    }
}
