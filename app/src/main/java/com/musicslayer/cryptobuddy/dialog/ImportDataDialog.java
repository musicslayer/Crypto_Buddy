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
import com.musicslayer.cryptobuddy.util.ToastUtil;
import com.musicslayer.cryptobuddy.view.red.FileEditText;

import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;

// TODO Let user select what kinds of data to import.
// TODO Rename to be ImportFileDialog?

public class ImportDataDialog extends BaseDialog {
    String externalFolder;

    ArrayList<String> existingFileNames = new ArrayList<>();

    public ImportDataDialog(Activity activity, String externalFolder) {
        super(activity);
        this.externalFolder = externalFolder;
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

                    String fileText;

                    try {
                        // Check if file text can be parsed as JSON.
                        fileText = FileUtil.readExternalFile(externalFolder, fileName);
                        new JSONObject(fileText);
                        // TODO Check some sort of marker or checksum or version number?
                    }
                    catch(Exception ignored) {
                        ToastUtil.showToast(activity,"import_file_failed");
                        return;
                    }

                    isComplete = true;
                    dismiss();

                    // We must do this after the dismissal because of the recreate.
                    Persistence.importAllFromJSON(activity, fileText);
                    ToastUtil.showToast(activity,"import_file_success");

                }
                else {
                    ToastUtil.showToast(activity,"must_fill_inputs");
                }
            }
        });

        ArrayList<File> existingFiles = FileUtil.getExternalFiles(externalFolder);
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
