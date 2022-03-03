package com.musicslayer.cryptobuddy.view;

import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View;

import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.crash.CrashButton;
import com.musicslayer.cryptobuddy.crash.CrashView;

public class ToggleButton extends CrashButton {
    public boolean toggleState;
    String optionOff;
    String optionOn;

    OnClickListener additionalOnClickListener;

    Rect boundsOff = new Rect();
    Rect boundsOn = new Rect();

    public ToggleButton(Context context) {
        this(context, null);
    }

    public ToggleButton(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        makeLayout();
    }

    public void setOptions(String optionOff, String optionOn) {
        this.optionOff = optionOff;
        this.optionOn = optionOn;

        updateLayout();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // Use same height.
        int height = getDefaultSize(getSuggestedMinimumHeight(), heightMeasureSpec);

        // Change the width to be the width that would hold the largest option.
        this.getPaint().getTextBounds(optionOff, 0, optionOff.length(), boundsOff);
        this.getPaint().getTextBounds(optionOn, 0, optionOn.length(), boundsOn);
        int width = Math.max(boundsOff.width(), boundsOn.width()) + 200;

        setMeasuredDimension(width, height);
    }

    public void setAdditionalOnClickListener(OnClickListener additionalOnClickListener) {
        this.additionalOnClickListener = additionalOnClickListener;
    }

    public void makeLayout() {
        setOnClickListener(new CrashView.CrashOnClickListener(getContext()) {
            @Override
            public void onClickImpl(View view) {
                toggleState = !toggleState;
                updateLayout();

                if(additionalOnClickListener != null) {
                    additionalOnClickListener.onClick(ToggleButton.this);
                }
            }
        });

        updateLayout();
    }

    public void updateLayout() {
        if(toggleState) {
            this.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_toggle_on_24, 0, 0, 0);
            this.setText(optionOn);
        }
        else {
            this.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_toggle_off_24, 0, 0, 0);
            this.setText(optionOff);
        }
    }

    @Override
    public Parcelable onSaveInstanceStateImpl(Parcelable state)
    {
        Bundle bundle = new Bundle();
        bundle.putParcelable("superState", state);

        bundle.putBoolean("toggleState", toggleState);
        bundle.putString("optionOff", optionOff);
        bundle.putString("optionOn", optionOn);

        return bundle;
    }

    @Override
    public Parcelable onRestoreInstanceStateImpl(Parcelable state)
    {
        if (state instanceof Bundle) // implicit null check
        {
            Bundle bundle = (Bundle) state;
            state = bundle.getParcelable("superState");

            toggleState = bundle.getBoolean("toggleState");
            optionOff = bundle.getString("optionOff");
            optionOn = bundle.getString("optionOn");

            updateLayout();

            if(toggleState && additionalOnClickListener != null) {
                additionalOnClickListener.onClick(ToggleButton.this);
            }
        }
        return state;
    }
}
