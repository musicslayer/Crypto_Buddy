package com.musicslayer.cryptobuddy.view.red;

import android.content.Context;
import android.util.AttributeSet;

public class AnythingEditText extends RedEditText {
    public AnythingEditText(Context context) {
        this(context, null);
    }

    public AnythingEditText(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);

        // Enforce a maximum length to protect algorithms from having to process large values.
        // Give this text 300 characters.
        setMaxLength(300);
    }

    // Always passes.
    public boolean condition() {
        return true;
    }
}
