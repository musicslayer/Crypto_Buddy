package com.musicslayer.cryptobuddy.view.red;

import android.content.Context;
import android.util.AttributeSet;

public class AnythingEditText extends RedEditText {
    public AnythingEditText(Context context) {
        this(context, null);
    }

    public AnythingEditText(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    // Always passes.
    public boolean condition() {
        return true;
    }
}
