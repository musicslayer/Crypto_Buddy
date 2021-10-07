package com.musicslayer.cryptobuddy.view.red;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.Editable;
import android.util.AttributeSet;

import androidx.core.graphics.BlendModeColorFilterCompat;
import androidx.core.graphics.BlendModeCompat;

import com.musicslayer.cryptobuddy.crash.CrashEditText;

// An EditText that can turn red if a condition is not met.

abstract public class RedEditText extends CrashEditText {
    boolean is_red = false;

    public RedEditText(Context context) {
        this(context, null);
    }

    public RedEditText(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.getBackground().mutate();
        test();
    }

    // Use this to work around warnings regarding getText() potentially returning null.
    public String getTextString() {
        Editable E = getText();
        assert E != null;
        return E.toString();
    }

    // Returns if the value satisfies the condition, and will highlight itself in red if it does not.
    public boolean test() {
        boolean isValid;

        try {
            isValid = condition();
        }
        catch(Exception ignored) {
            isValid = false;
        }

        if(isValid) {
            getBackground().clearColorFilter();
            is_red = false;
        }
        else {
            getBackground().setColorFilter(BlendModeColorFilterCompat.createBlendModeColorFilterCompat(0xFFFF0000, BlendModeCompat.SRC_ATOP));
            is_red = true;
        }

        return isValid;
    }

    abstract public boolean condition();

    @Override
    public Parcelable onSaveInstanceStateImpl(Parcelable state)
    {
        Bundle bundle = new Bundle();
        bundle.putParcelable("superState", state);
        bundle.putBoolean("is_red", is_red);

        return bundle;
    }

    @Override
    public Parcelable onRestoreInstanceStateImpl(Parcelable state)
    {
        if (state instanceof Bundle) // implicit null check
        {
            Bundle bundle = (Bundle) state;
            state = bundle.getParcelable("superState");

            is_red = bundle.getBoolean("is_red");
            if(is_red) {
                getBackground().setColorFilter(BlendModeColorFilterCompat.createBlendModeColorFilterCompat(0xFFFF0000, BlendModeCompat.SRC_ATOP));
            }
            else {
                getBackground().clearColorFilter();
            }
        }
        return state;
    }
}
