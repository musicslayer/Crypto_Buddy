package com.musicslayer.cryptobuddy.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.TextView;

import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.app.App;
import com.musicslayer.cryptobuddy.crash.CrashAdapterView;
import com.musicslayer.cryptobuddy.crash.CrashView;
import com.musicslayer.cryptobuddy.dialog.BaseDialogFragment;
import com.musicslayer.cryptobuddy.dialog.ExportDataFileDialog;
import com.musicslayer.cryptobuddy.dialog.ImportDataFileDialog;
import com.musicslayer.cryptobuddy.persistence.Persistence;
import com.musicslayer.cryptobuddy.rich.RichStringBuilder;
import com.musicslayer.cryptobuddy.util.ClipboardUtil;
import com.musicslayer.cryptobuddy.util.FileUtil;
import com.musicslayer.cryptobuddy.util.MessageUtil;
import com.musicslayer.cryptobuddy.util.ToastUtil;
import com.musicslayer.cryptobuddy.view.BorderedSpinnerView;

import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;

// TODO Export by copying to clipboard.
// TODO Export/Import need Progress Dialog?

public class DataManagementActivity extends BaseActivity {
    public final static String EXPORT_FOLDER = "exports";

    String externalFolder;

    @Override
    public int getAdLayoutViewID() {
        return R.id.data_management_adLayout;
    }

    @Override
    public void onBackPressedImpl() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    @Override
    public void createLayout(Bundle savedInstanceState) {
        setContentView(R.layout.activity_data_management);

        TextView T_BASEFOLDER = findViewById(R.id.data_management_exportFolderBaseTextView);
        TextView T_FOLDER = findViewById(R.id.data_management_exportFolderTextView);

        ArrayList<String> externalFolderBases = App.externalFilesDirs;

        BorderedSpinnerView bsv = findViewById(R.id.data_management_exportFolderBaseSpinner);
        bsv.setOptions(externalFolderBases);
        bsv.setOnItemSelectedListener(new CrashAdapterView.CrashOnItemSelectedListener(this) {
            public void onNothingSelectedImpl(AdapterView<?> parent) {}
            public void onItemSelectedImpl(AdapterView<?> parent, View view, int pos, long id) {
                externalFolder = externalFolderBases.get(pos) + EXPORT_FOLDER + File.separatorChar;
                T_FOLDER.setText("Export Folder:\n" + externalFolder);
            }
        });

        if(externalFolderBases.size() == 1) {
            T_BASEFOLDER.setVisibility(View.GONE);
            bsv.setVisibility(View.GONE);
        }

        if(externalFolderBases.size() == 0) {
            T_BASEFOLDER.setVisibility(View.GONE);
            bsv.setVisibility(View.GONE);
            T_FOLDER.setText(Html.fromHtml(RichStringBuilder.redText("No export folders are available.")));
        }

        Button B_EXPORT = findViewById(R.id.data_management_exportButton);
        B_EXPORT.setOnClickListener(new CrashView.CrashOnClickListener(this) {
            @Override
            public void onClickImpl(View view) {
                BaseDialogFragment.newInstance(ExportDataFileDialog.class, externalFolder).show(DataManagementActivity.this, "export");
            }
        });

        Button B_IMPORT = findViewById(R.id.data_management_importButton);
        B_IMPORT.setOnClickListener(new CrashView.CrashOnClickListener(this) {
            @Override
            public void onClickImpl(View view) {
                BaseDialogFragment.newInstance(ImportDataFileDialog.class, externalFolder).show(DataManagementActivity.this, "import");
            }
        });

        Button B_EMAIL = findViewById(R.id.data_management_emailButton);
        B_EMAIL.setOnClickListener(new CrashView.CrashOnClickListener(this) {
            @Override
            public void onClickImpl(View view) {
                // Create temp file with exported data and email it.
                ArrayList<File> fileArrayList = new ArrayList<>();
                fileArrayList.add(FileUtil.writeTempFile(Persistence.exportAllToJSON(DataManagementActivity.this)));
                MessageUtil.sendEmail(DataManagementActivity.this, "", "Crypto Buddy - Exported Data", "Exported data is attached.", fileArrayList);
            }
        });

        Button B_CLIPBOARD = findViewById(R.id.data_management_clipboardButton);
        B_CLIPBOARD.setOnClickListener(new CrashView.CrashOnClickListener(this) {
            @Override
            public void onClickImpl(View view) {
                String clipboardText;

                try {
                    // Check if clipboard text can be parsed as JSON.
                    clipboardText = String.valueOf(ClipboardUtil.getText(DataManagementActivity.this));
                    new JSONObject(clipboardText);
                    // TODO Check some sort of marker or checksum or version number?
                }
                catch(Exception ignored) {
                    ToastUtil.showToast(activity,"import_clipboard_failed");
                    return;
                }

                Persistence.importAllFromJSON(activity, clipboardText);
                ToastUtil.showToast(activity,"import_clipboard_success");
            }
        });

        updateLayout();
    }

    public void updateLayout() {
    }
}