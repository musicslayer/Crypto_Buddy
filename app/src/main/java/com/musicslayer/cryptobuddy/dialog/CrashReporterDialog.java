package com.musicslayer.cryptobuddy.dialog;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.app.App;
import com.musicslayer.cryptobuddy.crash.CrashException;
import com.musicslayer.cryptobuddy.persistence.Persistence;
import com.musicslayer.cryptobuddy.util.DataDumpUtil;
import com.musicslayer.cryptobuddy.util.ScreenshotUtil;
import com.musicslayer.cryptobuddy.util.ThrowableUtil;
import com.musicslayer.cryptobuddy.util.FileUtil;
import com.musicslayer.cryptobuddy.util.MessageUtil;

import java.io.File;
import java.util.ArrayList;

// This class/file cannot use any of the "Crash" classes, because we do not want any recursive CrashReporterDialog behavior.

// This is only meant to be used with CrashReporterDialogFragment.
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

    public void createLayout(Bundle savedInstanceState) {
        setContentView(R.layout.dialog_crash_reporter);

        Button B_EMAIL = findViewById(R.id.crash_reporter_dialog_emailButton);
        B_EMAIL.setOnClickListener(v -> {
            try {
                // Attach two files. One has the Exception that caused the crash, and the other has the DataDump data.
                // Some of the Exception may be obfuscated by ProGuard.
                File fileA = FileUtil.writeFile(activity, crashException.toString());
                File fileB = FileUtil.writeFile(activity, DataDumpUtil.getAllData(activity));

                ArrayList<File> fileArrayList = new ArrayList<>();
                fileArrayList.add(fileA);
                fileArrayList.add(fileB);

                MessageUtil.sendEmail(activity, "musicslayer@gmail.com", "Crypto Buddy - Crash Info", "Crash information is attached.\n\nFeel free to add any other information below:\n\n", fileArrayList);
            }
            catch(Exception e) {
                ThrowableUtil.processThrowable(e);
            }
        });

        Button B_EMAILSCREENSHOT = findViewById(R.id.crash_reporter_dialog_emailScreenshotButton);
        B_EMAILSCREENSHOT.setOnClickListener(v -> {
            try {
                // Attach three files. One has the Exception that caused the crash, one has the DataDump data, and one has a screenshot.
                // Some of the Exception may be obfuscated by ProGuard.
                File fileA = FileUtil.writeFile(activity, crashException.toString());
                File fileB = FileUtil.writeFile(activity, DataDumpUtil.getAllData(activity));
                File fileC = ScreenshotUtil.writeScreenshotFile(activity);

                ArrayList<File> fileArrayList = new ArrayList<>();
                fileArrayList.add(fileA);
                fileArrayList.add(fileB);
                fileArrayList.add(fileC);

                MessageUtil.sendEmail(activity, "musicslayer@gmail.com", "Crypto Buddy - Crash Info", "Crash information and screenshot are attached.\n\nFeel free to add any other information below:\n\n", fileArrayList);
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
        TextView T_ERASE = findViewById(R.id.crash_reporter_dialog_resetTextView);
        Button B_SHOW = findViewById(R.id.crash_reporter_dialog_showButton);
        Button B_RECOVER = findViewById(R.id.crash_reporter_dialog_recoverButton);
        Button B_CRASH = findViewById(R.id.crash_reporter_dialog_crashButton);
        Button B_RESET = findViewById(R.id.crash_reporter_dialog_resetButton);

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
                B_RESET.setVisibility(View.VISIBLE);

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
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(activity, R.style.AlertDialogTheme);
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

        AlertDialog.Builder resetAlertDialog = new AlertDialog.Builder(activity, R.style.AlertDialogTheme);
        resetAlertDialog.setTitle("Confirmation");
        resetAlertDialog.setCancelable(true);
        resetAlertDialog.setMessage("Are you sure you want to reset ALL STORED APP DATA? This cannot be reversed.");
        resetAlertDialog.setPositiveButton("Yes", (dialog, which) -> {
            try {
                boolean isComplete = Persistence.resetAllData(activity);

                // Manually show toast because we do not know if the Toast database was correctly initialized.
                // Similarly, just hardcode a Toast duration because we don't know if the settings were correctly initialized.
                if(isComplete) {
                    android.widget.Toast.makeText(activity, "All stored app data has been reset.", android.widget.Toast.LENGTH_LONG).show(); // "reset_everything"
                }
                else {
                    android.widget.Toast.makeText(activity, "Could not reset all stored app data.", android.widget.Toast.LENGTH_LONG).show(); // "reset_everything_fail"
                }
            }
            catch(Exception e) {
                ThrowableUtil.processThrowable(e);
            }
        });
        // No-op, but we need this so the button appears.
        resetAlertDialog.setNeutralButton("No", (dialog, which) -> {});

        B_RESET.setOnClickListener(v -> {
            try {
                resetAlertDialog.show();
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
        B_RESET.setVisibility(View.GONE);
    }
}
