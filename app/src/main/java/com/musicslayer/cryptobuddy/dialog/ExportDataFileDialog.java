package com.musicslayer.cryptobuddy.dialog;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.app.App;
import com.musicslayer.cryptobuddy.crash.CrashAdapterView;
import com.musicslayer.cryptobuddy.crash.CrashDialogInterface;
import com.musicslayer.cryptobuddy.crash.CrashView;
import com.musicslayer.cryptobuddy.rich.RichStringBuilder;
import com.musicslayer.cryptobuddy.util.FileUtil;
import com.musicslayer.cryptobuddy.util.HelpUtil;
import com.musicslayer.cryptobuddy.util.ToastUtil;
import com.musicslayer.cryptobuddy.view.BorderedSpinnerView;
import com.musicslayer.cryptobuddy.view.red.FileEditText;

import java.io.File;
import java.util.ArrayList;

public class ExportDataFileDialog extends BaseDialog {
    public final static String DATA_FOLDER = "exports";

    String dataFolder;

    ArrayList<String> existingFileNames = new ArrayList<>();

    public String user_FILENAME;
    public String user_FOLDERNAME;

    public ExportDataFileDialog(Activity activity) {
        super(activity);
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

        TextView T_BASEFOLDER = findViewById(R.id.export_data_file_dialog_dataFolderBaseTextView);
        TextView T_FOLDER = findViewById(R.id.export_data_file_dialog_dataFolderTextView);

        ArrayList<String> dataFolderBases = App.externalFilesDirs;

        BorderedSpinnerView bsv = findViewById(R.id.export_data_file_dialog_dataFolderBaseSpinner);
        bsv.setOptions(dataFolderBases);
        bsv.setOnItemSelectedListener(new CrashAdapterView.CrashOnItemSelectedListener(activity) {
            public void onNothingSelectedImpl(AdapterView<?> parent) {}
            public void onItemSelectedImpl(AdapterView<?> parent, View view, int pos, long id) {
                dataFolder = dataFolderBases.get(pos) + DATA_FOLDER + File.separatorChar;
                T_FOLDER.setText("Data Folder:\n" + dataFolder);
                updateLayout();
            }
        });

        if(dataFolderBases.size() == 1) {
            T_BASEFOLDER.setVisibility(View.GONE);
            bsv.setVisibility(View.GONE);
        }

        if(dataFolderBases.size() == 0) {
            T_BASEFOLDER.setVisibility(View.GONE);
            bsv.setVisibility(View.GONE);
            T_FOLDER.setText(Html.fromHtml(RichStringBuilder.redText("No data folders are available.")));
        }
    }

    public void updateLayout() {
        final FileEditText E_FILE = findViewById(R.id.export_data_file_dialog_fileEditText);

        BaseDialogFragment confirmFileOverwriteDialogFragment = BaseDialogFragment.newInstance(ConfirmFileOverwriteDialog.class);
        confirmFileOverwriteDialogFragment.setOnDismissListener(new CrashDialogInterface.CrashOnDismissListener(activity) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                if(((ConfirmFileOverwriteDialog)dialog).isComplete) {
                    user_FILENAME = E_FILE.getTextString();
                    user_FOLDERNAME = dataFolder;

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
                    if(FileUtil.exists(dataFolder, fileName)) {
                        confirmFileOverwriteDialogFragment.show(activity, "overwrite");
                        return;
                    }

                    user_FILENAME = fileName;
                    user_FOLDERNAME = dataFolder;

                    isComplete = true;
                    dismiss();
                }
                else {
                    ToastUtil.showToast(activity,"must_fill_inputs");
                }
            }
        });

        ArrayList<File> existingFiles = FileUtil.getFiles(dataFolder);
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
