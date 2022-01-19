package com.musicslayer.cryptobuddy.view.fixed;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import com.musicslayer.cryptobuddy.crash.CrashFrameLayout;
import com.musicslayer.cryptobuddy.state.StateObj;
import com.musicslayer.cryptobuddy.util.AuthUtil;

// A FrameLayout that makes sure the enclosed view does not get recreated when the activity/dialog it is in gets recreated.

abstract public class FixedFrameLayout extends CrashFrameLayout {
    public View innerView;

    public FixedFrameLayout(Context context) {
        super(context, null);
    }

    public FixedFrameLayout(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);

        if(StateObj.view == null) {
            StateObj.view = createInnerView(context);
        }

        innerView = StateObj.view;

        this.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow(View v) {
                if(innerView != null) {
                    ((ViewGroup)v).addView(innerView);
                }
            }

            @Override
            public void onViewDetachedFromWindow(View v) {
                ((ViewGroup)v).removeAllViews();
            }
        });
    }

    abstract public View createInnerView(Context context);
}
