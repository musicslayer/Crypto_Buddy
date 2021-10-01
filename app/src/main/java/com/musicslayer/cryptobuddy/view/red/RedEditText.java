package com.musicslayer.cryptobuddy.view.red;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatEditText;
import androidx.core.graphics.BlendModeColorFilterCompat;
import androidx.core.graphics.BlendModeCompat;

// An EditText that can turn red if a condition is not met.

abstract public class RedEditText extends AppCompatEditText {
    boolean is_red = false;

    public RedEditText(Context context) {
        this(context, null);
    }

    public RedEditText(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.getBackground().mutate();
        test();
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
    public Parcelable onSaveInstanceState()
    {
        Bundle bundle = new Bundle();
        bundle.putParcelable("superState", super.onSaveInstanceState());
        bundle.putBoolean("is_red", is_red);

        return bundle;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state)
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
        super.onRestoreInstanceState(state);
    }
}
