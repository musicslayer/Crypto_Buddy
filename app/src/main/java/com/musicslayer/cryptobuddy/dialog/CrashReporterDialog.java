package com.musicslayer.cryptobuddy.dialog;

import android.app.Activity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.app.App;
import com.musicslayer.cryptobuddy.crash.CrashException;
import com.musicslayer.cryptobuddy.persistence.Persistence;
import com.musicslayer.cryptobuddy.util.DataDumpUtil;
import com.musicslayer.cryptobuddy.util.ThrowableUtil;
import com.musicslayer.cryptobuddy.util.FileUtil;
import com.musicslayer.cryptobuddy.util.MessageUtil;

import java.util.ArrayList;

// This Dialog cannot use any of the "Crash" classes, because we do not want any recursive CrashReporterDialog behavior.

public class CrashReporterDialog extends BaseDialog {
    public CrashException crashException;

    public CrashReporterDialog(Activity activity, CrashException crashException) {
        super(activity);
        this.crashException = crashException;
    }

    @Override
    public void onBackPressedImpl() {
        // User cannot hit back to dismiss. They must select an option.
    }

    public int getBaseViewID() {
        return R.id.crash_reporter_dialog;
    }

    public void createLayout() {
        setContentView(R.layout.dialog_crash_reporter);

        Button B_EMAIL = findViewById(R.id.crash_reporter_dialog_emailButton);
        B_EMAIL.setOnClickListener(v -> {
            try {
                // Attach two files. One has the Exception that caused the crash, and the other has the DataDump data.
                // Some of the Exception may be obfuscated by ProGuard.
                java.io.File fileA = FileUtil.writeFile(activity, crashException.toString());
                java.io.File fileB = FileUtil.writeFile(activity, DataDumpUtil.getAllData(activity));

                ArrayList<java.io.File> fileArrayList = new ArrayList<>();
                fileArrayList.add(fileA);
                fileArrayList.add(fileB);

                MessageUtil.sendEmail(activity, "musicslayer@gmail.com", "Crypto Buddy - Crash Log", "Crash information is attached.\n\n<WRITE OTHER INFO HERE>", fileArrayList);
            }
            catch(Exception e) {
                ThrowableUtil.processThrowable(e);
            }
        });

        Button B_EXIT = findViewById(R.id.crash_reporter_dialog_exitButton);
        B_EXIT.setOnClickListener(v -> {
            // Don't use try/catch. An exception will end the app anyway.
            activity.finishAffinity();
            System.exit(0);
        });

        TextView T_SHOW = findViewById(R.id.crash_reporter_dialog_showTextView);
        TextView T_RECOVER = findViewById(R.id.crash_reporter_dialog_recoverTextView);
        TextView T_CRASH = findViewById(R.id.crash_reporter_dialog_crashTextView);
        TextView T_ERASE = findViewById(R.id.crash_reporter_dialog_eraseTextView);
        Button B_SHOW = findViewById(R.id.crash_reporter_dialog_showButton);
        Button B_RECOVER = findViewById(R.id.crash_reporter_dialog_recoverButton);
        Button B_CRASH = findViewById(R.id.crash_reporter_dialog_crashButton);
        Button B_ERASE = findViewById(R.id.crash_reporter_dialog_eraseButton);

        Button B_ADVANCED = findViewById(R.id.crash_reporter_dialog_advancedButton);
        B_ADVANCED.setOnClickListener(v -> {
            try {
                T_SHOW.setVisibility(View.VISIBLE);
                T_RECOVER.setVisibility(View.VISIBLE);
                T_CRASH.setVisibility(View.VISIBLE);
                T_ERASE.setVisibility(View.VISIBLE);
                B_SHOW.setVisibility(View.VISIBLE);
                B_RECOVER.setVisibility(View.VISIBLE);
                B_CRASH.setVisibility(View.VISIBLE);
                B_ERASE.setVisibility(View.VISIBLE);

                // Don't offer any hide option. This is one and done!
                B_ADVANCED.setVisibility(View.GONE);
            }
            catch(Exception e) {
                ThrowableUtil.processThrowable(e);
            }
        });

        B_SHOW.setOnClickListener(v -> {
            try {
                // Simplest way to show user information. This also has built-in scrolling.
                AlertDialog alertDialog = new AlertDialog.Builder(activity).create();
                alertDialog.setTitle("Error Information");
                alertDialog.setMessage(crashException.toString());
                alertDialog.show();
            }
            catch(Exception e) {
                ThrowableUtil.processThrowable(e);
            }
        });

        B_RECOVER.setOnClickListener(v -> {
            // Just dismiss this dialog and let the user try to continue.
            // Don't use try/catch. We already told the user that unpredictable behavior may happen.
            // Also reset the flag and allow for crashes again.
            CrashReporterDialogFragment.LAUNCHED[0] = false;
            dismiss();
        });

        B_CRASH.setOnClickListener(v -> {
            // Rethrow the original Exception that we caught before showing CrashReporterDialog.
            crashException.throwOriginalException();
        });

        B_ERASE.setOnClickListener(v -> {
            try {
                Persistence.resetAllData(activity);

                // Manually show toast "reset_everything" because we do not know if the Toast database was correctly initialized.
                // Similarly, just hardcode a Toast duration because we don't know if the settings were correctly initialized.
                android.widget.Toast.makeText(activity, "All stored app data has been reset.", android.widget.Toast.LENGTH_LONG).show();
            }
            catch(Exception e) {
                ThrowableUtil.processThrowable(e);
            }
        });

        TextView T_INFO = findViewById(R.id.crash_reporter_dialog_infoTextView);
        if(App.DEBUG) {
            T_INFO.setText(crashException.toString());
        }
        else {
            T_INFO.setVisibility(View.GONE);
        }

        // Advanced options start off hidden.
        T_SHOW.setVisibility(View.GONE);
        T_RECOVER.setVisibility(View.GONE);
        T_CRASH.setVisibility(View.GONE);
        T_ERASE.setVisibility(View.GONE);
        B_SHOW.setVisibility(View.GONE);
        B_RECOVER.setVisibility(View.GONE);
        B_CRASH.setVisibility(View.GONE);
        B_ERASE.setVisibility(View.GONE);
    }
}
