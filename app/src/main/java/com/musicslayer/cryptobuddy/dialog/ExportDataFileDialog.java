package com.musicslayer.cryptobuddy.dialog;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.crash.CrashDialogInterface;
import com.musicslayer.cryptobuddy.crash.CrashView;
import com.musicslayer.cryptobuddy.persistence.Persistence;
import com.musicslayer.cryptobuddy.rich.RichStringBuilder;
import com.musicslayer.cryptobuddy.util.FileUtil;
import com.musicslayer.cryptobuddy.util.HelpUtil;
import com.musicslayer.cryptobuddy.util.ToastUtil;
import com.musicslayer.cryptobuddy.view.red.FileEditText;

import java.io.File;
import java.util.ArrayList;

public class ExportDataFileDialog extends BaseDialog {
    String externalFolder;

    ArrayList<String> existingFileNames = new ArrayList<>();

    public String user_FILENAME;

    public ExportDataFileDialog(Activity activity, String externalFolder) {
        super(activity);
        this.externalFolder = externalFolder;
    }

    public int getBaseViewID() {
        return R.id.export_data_file_dialog;
    }

    public void createLayout(Bundle savedInstanceState) {
        setContentView(R.layout.dialog_export_data_file);

        ImageButton helpButton = findViewById(R.id.export_data_file_dialog_helpButton);
        helpButton.setOnClickListener(new CrashView.CrashOnClickListener(this.activity) {
            @Override
            public void onClickImpl(View view) {
                HelpUtil.showHelp(ExportDataFileDialog.this.activity, R.raw.help_export_data);
            }
        });

        final FileEditText E_FILE = findViewById(R.id.export_data_file_dialog_fileEditText);

        BaseDialogFragment confirmFileOverwriteDialogFragment = BaseDialogFragment.newInstance(ConfirmFileOverwriteDialog.class);
        confirmFileOverwriteDialogFragment.setOnDismissListener(new CrashDialogInterface.CrashOnDismissListener(activity) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                if(((ConfirmFileOverwriteDialog)dialog).isComplete) {
                    user_FILENAME = E_FILE.getTextString();
                    isComplete = true;
                    dismiss();
                }
            }
        });
        confirmFileOverwriteDialogFragment.restoreListeners(activity, "overwrite");

        Button B_CONFIRM = findViewById(R.id.export_data_file_dialog_confirmButton);
        B_CONFIRM.setOnClickListener(new CrashView.CrashOnClickListener(this.activity) {
            public void onClickImpl(View v) {
                boolean isValid = E_FILE.test();

                if(isValid) {
                    String fileName = E_FILE.getTextString();
                    if(FileUtil.isExternalFileExisting(externalFolder, fileName)) {
                        confirmFileOverwriteDialogFragment.show(activity, "overwrite");
                        return;
                    }

                    user_FILENAME = fileName;
                    isComplete = true;
                    dismiss();
                }
                else {
                    ToastUtil.showToast(activity,"must_fill_inputs");
                }
            }
        });

        ArrayList<File> existingFiles = FileUtil.getExternalFiles(externalFolder);
        TextView T = findViewById(R.id.export_data_file_dialog_existingFilesTextView);
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
