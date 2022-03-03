package com.musicslayer.cryptobuddy.view;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View;

import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.crash.CrashButton;
import com.musicslayer.cryptobuddy.crash.CrashView;

public class ToggleButton extends CrashButton {
    public boolean toggleState;
    String optionOffAdjusted;
    String optionOnAdjusted;

    OnClickListener additionalOnClickListener;

    public ToggleButton(Context context) {
        this(context, null);
    }

    public ToggleButton(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);

        makeLayout();
    }

    public void setOptions(String optionOff, String optionOn) {
        optionOffAdjusted = optionOff;
        optionOnAdjusted = optionOn;

        // TODO Make the two adjusted strings of equal length so the button has the same width for any state.

        updateLayout();
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
            this.setText(optionOnAdjusted);
        }
        else {
            this.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_toggle_off_24, 0, 0, 0);
            this.setText(optionOffAdjusted);
        }
    }

    @Override
    public Parcelable onSaveInstanceStateImpl(Parcelable state)
    {
        Bundle bundle = new Bundle();
        bundle.putParcelable("superState", state);

        bundle.putBoolean("toggleState", toggleState);
        bundle.putString("optionOffAdjusted", optionOffAdjusted);
        bundle.putString("optionOnAdjusted", optionOnAdjusted);

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
            optionOffAdjusted = bundle.getString("optionOffAdjusted");
            optionOnAdjusted = bundle.getString("optionOnAdjusted");

            updateLayout();
        }
        return state;
    }
}
