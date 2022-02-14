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

// TODO Cleanup permissions.

public class ImportDataDialog extends BaseDialog {
    public final static String EXPORT_FOLDER = "exports";

    ArrayList<String> existingFileNames = new ArrayList<>();

    public ImportDataDialog(Activity activity) {
        super(activity);
    }

    public int getBaseViewID() {
        return R.id.import_data_dialog;
    }

    public void createLayout(Bundle savedInstanceState) {
        setContentView(R.layout.dialog_import_data);

        ImageButton helpButton = findViewById(R.id.import_data_dialog_helpButton);
        helpButton.setOnClickListener(new CrashView.CrashOnClickListener(this.activity) {
            @Override
            public void onClickImpl(View view) {
                HelpUtil.showHelp(ImportDataDialog.this.activity, R.raw.help_import_data);
            }
        });

        final FileEditText E_FILE = findViewById(R.id.import_data_dialog_fileEditText);

        Button B_CONFIRM = findViewById(R.id.import_data_dialog_confirmButton);
        B_CONFIRM.setOnClickListener(new CrashView.CrashOnClickListener(this.activity) {
            public void onClickImpl(View v) {
                boolean isValid = E_FILE.test();

                if(isValid) {
                    String fileName = E_FILE.getTextString();
                    if(!existingFileNames.contains(fileName)) {
                        ToastUtil.showToast(activity, "file_does_not_exist");
                        return;
                    }

                    String json = FileUtil.readExternalFile(activity, EXPORT_FOLDER, fileName);

                    if(json != null) {
                        isComplete = true;
                        dismiss();

                        // We must do this after the dismissal because of the recreate.
                        Persistence.importAllFromJSON(activity, json);
                        ToastUtil.showToast(activity,"import_success");
                    }
                    else {
                        ToastUtil.showToast(activity,"import_failed");
                    }
                }
                else {
                    ToastUtil.showToast(activity,"must_fill_inputs");
                }
            }
        });

        ArrayList<File> existingFiles = FileUtil.getExternalFiles(activity, EXPORT_FOLDER);
        TextView T = findViewById(R.id.import_data_dialog_existingFilesTextView);
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
