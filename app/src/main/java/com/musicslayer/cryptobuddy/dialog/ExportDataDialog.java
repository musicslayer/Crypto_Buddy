package com.musicslayer.cryptobuddy.dialog;

import android.app.Activity;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.crash.CrashView;
import com.musicslayer.cryptobuddy.persistence.Persistence;
import com.musicslayer.cryptobuddy.rich.RichStringBuilder;
import com.musicslayer.cryptobuddy.util.FileUtil;
import com.musicslayer.cryptobuddy.util.HelpUtil;
import com.musicslayer.cryptobuddy.util.PermissionUtil;
import com.musicslayer.cryptobuddy.util.ToastUtil;
import com.musicslayer.cryptobuddy.view.red.FileEditText;

import java.io.File;
import java.util.ArrayList;

public class ExportDataDialog extends BaseDialog {
    public final static String EXPORT_FOLDER = "exports";

    ArrayList<String> existingFileNames = new ArrayList<>();

    public ExportDataDialog(Activity activity) {
        super(activity);
    }

    public int getBaseViewID() {
        return R.id.export_data_dialog;
    }

    public void createLayout(Bundle savedInstanceState) {
        setContentView(R.layout.dialog_export_data);

        ImageButton helpButton = findViewById(R.id.export_data_dialog_helpButton);
        helpButton.setOnClickListener(new CrashView.CrashOnClickListener(this.activity) {
            @Override
            public void onClickImpl(View view) {
                HelpUtil.showHelp(ExportDataDialog.this.activity, R.raw.help_export_data);
            }
        });

        final FileEditText E_FILE = findViewById(R.id.export_data_dialog_fileEditText);

        Button B_CONFIRM = findViewById(R.id.export_data_dialog_confirmButton);
        B_CONFIRM.setOnClickListener(new CrashView.CrashOnClickListener(this.activity) {
            public void onClickImpl(View v) {
                boolean isValid = E_FILE.test();

                if(!PermissionUtil.requestExternalWritePermission(activity)) {
                    return;
                }

                if(isValid) {
                    String fileName = E_FILE.getTextString();
                    if(existingFileNames.contains(fileName)) {
                        // TODO Open confirmation dialog and ask to overwrite.
                        ToastUtil.showToast(activity, "file_already_exists");
                        return;
                    }

                    String json = Persistence.exportAllToJSON();
                    File externalFile = FileUtil.writeExternalFile(activity, EXPORT_FOLDER, fileName, json);

                    if(externalFile != null) {
                        ToastUtil.showToast(activity,"export_success");

                        isComplete = true;
                        dismiss();
                    }
                    else {
                        ToastUtil.showToast(activity,"export_failed");
                    }
                }
                else {
                    ToastUtil.showToast(activity,"must_fill_inputs");
                }
            }
        });

        ArrayList<File> existingFiles = FileUtil.getExternalFiles(activity, EXPORT_FOLDER);
        TextView T = findViewById(R.id.export_data_dialog_existingFilesTextView);
        if(existingFiles == null) {
            String redText = RichStringBuilder.redText("Problem accessing existing files.");
            T.setText(Html.fromHtml(redText));
        }
        else if(existingFiles.isEmpty()) {
            T.setText("No existing files.");
        }
        else {
            StringBuilder s = new StringBuilder();
            s.append("Existing files:");
            for(File existingFile : existingFiles) {
                existingFileNames.add(existingFile.getName());
                s.append("\n").append(existingFile.getName());
            }

            T.setText(s.toString());
        }
    }
}
