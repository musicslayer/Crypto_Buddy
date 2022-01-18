package com.musicslayer.cryptobuddy.view.fixed;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.webkit.WebView;

public class FixedWebViewFrameLayout extends FixedFrameLayout {
    public View createInnerView(Context context) {
        return new WebView(context);
    }

    public FixedWebViewFrameLayout(Context context) {
        super(context, null);
    }

    public FixedWebViewFrameLayout(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }
}
