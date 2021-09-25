package com.musicslayer.cryptobuddy.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ScrollView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.musicslayer.cryptobuddy.activity.BaseActivity;
import com.musicslayer.cryptobuddy.util.Window;

// TODO Many common dialogs can be merged.

abstract public class BaseDialog extends Dialog {
    public BaseActivity activity;

    // Tells whether the user deliberately completed this instance.
    public boolean isComplete = false;

    abstract public void createLayout();
    abstract public int getBaseViewID();

    public BaseDialog(Activity activity) {
        super(activity);
        this.activity = (BaseActivity)activity;
        this.setCanceledOnTouchOutside(false);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Needed for older versions of Android.
        requestWindowFeature(android.view.Window.FEATURE_NO_TITLE);

        createLayout();
    }

    @Override
    public void show() {
        super.show();
        adjustDialog();
    }

    public void adjustDialog() {
        ViewGroup v = findViewById(getBaseViewID());
        ViewGroup p = (ViewGroup)v.getParent();

        // Stretch to 90% width. This is needed to see any dialog at all.
        int[] dimensions = Window.getDimensions(this.activity);
        v.setLayoutParams(new FrameLayout.LayoutParams((int)(dimensions[0] * 0.9), FrameLayout.LayoutParams.WRAP_CONTENT));

        // Add a ScrollView
        ScrollView s = new ScrollView(this.activity);
        s.setLayoutParams(new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.MATCH_PARENT));

        p.removeView(v);
        s.addView(v);
        p.addView(s);
    }
}
