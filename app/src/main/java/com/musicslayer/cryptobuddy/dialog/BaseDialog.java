package com.musicslayer.cryptobuddy.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ScrollView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.musicslayer.cryptobuddy.activity.BaseActivity;
import com.musicslayer.cryptobuddy.crash.CrashException;
import com.musicslayer.cryptobuddy.util.ThrowableLogger;
import com.musicslayer.cryptobuddy.util.Window;

// TODO Many common dialogs can be merged.

abstract public class BaseDialog extends Dialog {
    public BaseActivity activity;

    // Tells whether the user deliberately completed this instance.
    public boolean isComplete = false;

    // Non-null if a crash occurred.
    public Exception originalException;

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

        try {
            // Needed for older versions of Android.
            requestWindowFeature(android.view.Window.FEATURE_NO_TITLE);

            createLayout();
        }
        catch(Exception e) {
            originalException = e;
            ThrowableLogger.processThrowable(e);

            this.dismiss();
        }
    }

    @Override
    public void dismiss() {
        super.dismiss();

        // In dialogs, we want the dialog that crashed to have already been dismissed before trying to show CrashDialog.
        if(originalException != null && !(this instanceof CrashDialog) && activity.getSupportFragmentManager().findFragmentByTag("crash") == null) {
            CrashException crashException = new CrashException(originalException);
            CrashDialogFragment.showCrashDialogFragment(CrashDialog.class, crashException, activity, "crash");
        }
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
