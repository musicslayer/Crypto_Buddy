package com.musicslayer.cryptobuddy.crash;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatTextView;

import com.musicslayer.cryptobuddy.dialog.CrashReporterDialog;
import com.musicslayer.cryptobuddy.dialog.CrashReporterDialogFragment;
import com.musicslayer.cryptobuddy.util.ContextUtil;
import com.musicslayer.cryptobuddy.util.ThrowableLogger;

abstract public class CrashTextView extends AppCompatTextView {
    public CrashTextView(Context context) {
        super(context);
    }

    public CrashTextView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    @Override
    final public Parcelable onSaveInstanceState() {
        try {
            return onSaveInstanceStateImpl(super.onSaveInstanceState());
        }
        catch(Exception e) {
            ThrowableLogger.processThrowable(e);

            // We cannot show the dialog here, so just save the exception.
            Bundle bundle = new Bundle();
            bundle.putParcelable("superState", super.onSaveInstanceState());
            bundle.putSerializable("!EXCEPTION!", e);
            return bundle;
        }
    }

    @Override
    final public void onRestoreInstanceState(Parcelable state) {
        // If onSaveInstanceState had an exception, just show the crash reporter and stop.
        Exception exception = null;
        if(state instanceof Bundle) {
            Bundle bundle = (Bundle)state;
            exception = (Exception)bundle.getSerializable("!EXCEPTION!");
        }

        if(exception != null) {
            CrashException crashException = new CrashException(exception);
            CrashReporterDialogFragment.showCrashDialogFragment(CrashReporterDialog.class, crashException, ContextUtil.getActivity(getContext()), "crash");
            return;
        }

        try {
            super.onRestoreInstanceState(onRestoreInstanceStateImpl(state));
        }
        catch(Exception e) {
            ThrowableLogger.processThrowable(e);

            CrashException crashException = new CrashException(e);
            crashException.appendExtraInfoFromArgument(state);

            CrashReporterDialogFragment.showCrashDialogFragment(CrashReporterDialog.class, crashException, ContextUtil.getActivity(getContext()), "crash");
        }
    }

    // These are optional for subclasses to override.
    public Parcelable onSaveInstanceStateImpl(Parcelable state) {
        Bundle bundle = new Bundle();
        bundle.putParcelable("superState", state);
        return bundle;
    }

    public Parcelable onRestoreInstanceStateImpl(Parcelable state) {
        if (state instanceof Bundle) {
            Bundle bundle = (Bundle) state;
            state = bundle.getParcelable("superState");
        }
        return state;
    }
}
