package com.musicslayer.cryptobuddy.dialog;

import android.app.Activity;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.widget.Toolbar;

import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.persistence.Persistence;
import com.musicslayer.cryptobuddy.util.DataDump;
import com.musicslayer.cryptobuddy.util.ExceptionLogger;
import com.musicslayer.cryptobuddy.util.File;
import com.musicslayer.cryptobuddy.util.Message;

import java.util.ArrayList;

public class CrashDialog extends BaseDialog {
    // The exception that caused the crash.
    public Exception crashException;

    public CrashDialog(Activity activity, Exception crashException) {
        super(activity);
        this.crashException = crashException;
    }

    @Override
    public void onBackPressed() {
        // User cannot hit back to dismiss. They must select an option.
    }

    public int getBaseViewID() {
        return R.id.crash_dialog;
    }

    public void createLayout() {
        setContentView(R.layout.dialog_crash);

        Toolbar toolbar = findViewById(R.id.crash_dialog_toolbar);
        toolbar.setSubtitle(activity.getLocalClassName());

        Button B_EMAIL = findViewById(R.id.crash_dialog_emailButton);
        B_EMAIL.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    // Attach two files. One has the Exception that caused the crash, and the other has the DataDump data.
                    // Some of the Exception may be obfuscated by ProGuard.
                    java.io.File fileA = File.writeFile(activity, ExceptionLogger.getExceptionText(crashException));
                    java.io.File fileB = File.writeFile(activity, DataDump.getAllData(activity));

                    ArrayList<java.io.File> fileArrayList = new ArrayList<>();
                    fileArrayList.add(fileA);
                    fileArrayList.add(fileB);

                    Message.sendEmail(activity, "musicslayer@gmail.com", "Crypto Buddy - Crash Log", "Crash information is attached.\n\n<WRITE OTHER INFO HERE>", fileArrayList);
                }
                catch(Exception e) {
                    try {
                        ExceptionLogger.processException(e);
                    }
                    catch(Exception ignored) {
                    }
                }
            }
        });

        Button B_ERASE = findViewById(R.id.crash_dialog_eraseButton);
        B_ERASE.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    Persistence.resetAllData(activity);

                    // Manually show toast "reset_everything" because we do not know if the Toast database was correctly initialized.
                    // Similarly, just hardcode a Toast duration because we don't know if the settings were correctly initialized.
                    android.widget.Toast.makeText(activity, "All stored app data has been reset.", android.widget.Toast.LENGTH_LONG).show();
                }
                catch(Exception e) {
                    try {
                        ExceptionLogger.processException(e);
                    }
                    catch(Exception ignored) {
                    }
                }
            }
        });

        Button B_EXIT = findViewById(R.id.crash_dialog_exitButton);
        B_EXIT.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                activity.finishAffinity();
                System.exit(0);
            }
        });
    }
}
