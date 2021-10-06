package com.musicslayer.cryptobuddy.dialog;

import android.app.Activity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;

import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.app.App;
import com.musicslayer.cryptobuddy.crash.CrashException;
import com.musicslayer.cryptobuddy.persistence.Persistence;
import com.musicslayer.cryptobuddy.util.DataDump;
import com.musicslayer.cryptobuddy.util.ThrowableLogger;
import com.musicslayer.cryptobuddy.util.File;
import com.musicslayer.cryptobuddy.util.Message;

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

        Toolbar toolbar = findViewById(R.id.crash_reporter_dialog_toolbar);
        toolbar.setSubtitle(activity.getLocalClassName());

        Button B_EMAIL = findViewById(R.id.crash_reporter_dialog_emailButton);
        B_EMAIL.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    // Attach two files. One has the Exception that caused the crash, and the other has the DataDump data.
                    // Some of the Exception may be obfuscated by ProGuard.
                    java.io.File fileA = File.writeFile(activity, crashException.toString());
                    java.io.File fileB = File.writeFile(activity, DataDump.getAllData(activity));

                    ArrayList<java.io.File> fileArrayList = new ArrayList<>();
                    fileArrayList.add(fileA);
                    fileArrayList.add(fileB);

                    Message.sendEmail(activity, "musicslayer@gmail.com", "Crypto Buddy - Crash Log", "Crash information is attached.\n\n<WRITE OTHER INFO HERE>", fileArrayList);
                }
                catch(Exception e) {
                    ThrowableLogger.processThrowable(e);
                }
            }
        });

        Button B_ERASE = findViewById(R.id.crash_reporter_dialog_eraseButton);
        B_ERASE.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    Persistence.resetAllData(activity);

                    // Manually show toast "reset_everything" because we do not know if the Toast database was correctly initialized.
                    // Similarly, just hardcode a Toast duration because we don't know if the settings were correctly initialized.
                    android.widget.Toast.makeText(activity, "All stored app data has been reset.", android.widget.Toast.LENGTH_LONG).show();
                }
                catch(Exception e) {
                    ThrowableLogger.processThrowable(e);
                }
            }
        });

        Button B_EXIT = findViewById(R.id.crash_reporter_dialog_exitButton);
        B_EXIT.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                activity.finishAffinity();
                System.exit(0);
            }
        });

        Button B_CRASH = findViewById(R.id.crash_reporter_dialog_crashButton);
        B_CRASH.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Rethrow the original Exception that we caught before showing CrashReporterDialog.
                crashException.throwOriginalException();
            }
        });

        TextView T_INFO = findViewById(R.id.crash_reporter_dialog_infoTextView);
        if(App.DEBUG) {
            T_INFO.setText(crashException.toString());
        }
        else {
            T_INFO.setVisibility(View.GONE);
        }
    }
}
