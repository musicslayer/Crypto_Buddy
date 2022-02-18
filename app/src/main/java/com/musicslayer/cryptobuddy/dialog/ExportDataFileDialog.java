package com.musicslayer.cryptobuddy.dialog;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.crash.CrashDialogInterface;
import com.musicslayer.cryptobuddy.crash.CrashView;
import com.musicslayer.cryptobuddy.file.UniversalFile;
import com.musicslayer.cryptobuddy.util.HelpUtil;
import com.musicslayer.cryptobuddy.util.ToastUtil;
import com.musicslayer.cryptobuddy.view.red.FileEditText;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class ExportDataFileDialog extends BaseDialog {
    public final static String DATA_FOLDER = "exports";

    public UniversalFile universalFolder;

    public String user_FILENAME;
    public UniversalFile user_UNIVERSALFOLDER;

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

        BaseDialogFragment chooseFolderDialogFragment = BaseDialogFragment.newInstance(ChooseFolderDialog.class, DATA_FOLDER);
        chooseFolderDialogFragment.setOnDismissListener(new CrashDialogInterface.CrashOnDismissListener(activity) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                if(((ChooseFolderDialog)dialog).isComplete) {
                    universalFolder = ((ChooseFolderDialog)dialog).user_UNIVERSALFOLDER;

                    updateLayout();
                }
            }
        });
        chooseFolderDialogFragment.restoreListeners(activity, "choose_folder");

        Button B_CHOOSE = findViewById(R.id.export_data_file_dialog_chooseFolderButton);
        B_CHOOSE.setOnClickListener(new CrashView.CrashOnClickListener(this.activity) {
            public void onClickImpl(View v) {
                chooseFolderDialogFragment.show(activity, "choose_folder");
            }
        });

        updateLayout();
    }

    public void updateLayout() {
        TextView T_FOLDER = findViewById(R.id.export_data_file_dialog_dataFolderTextView);
        if(universalFolder == null) {
            T_FOLDER.setText("No data folder selected.");
        }
        else {
            T_FOLDER.setText("Data Folder:\n" + universalFolder.getDisplayPath());
        }

        final FileEditText E_FILE = findViewById(R.id.export_data_file_dialog_fileEditText);

        BaseDialogFragment confirmFileOverwriteDialogFragment = BaseDialogFragment.newInstance(ConfirmFileOverwriteDialog.class);
        confirmFileOverwriteDialogFragment.setOnDismissListener(new CrashDialogInterface.CrashOnDismissListener(activity) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                if(((ConfirmFileOverwriteDialog)dialog).isComplete) {
                    user_FILENAME = E_FILE.getTextString();
                    user_UNIVERSALFOLDER = universalFolder;

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

                if(universalFolder == null) {
                    ToastUtil.showToast(activity,"no_folder_selected");
                }
                else if(!isValid) {
                    ToastUtil.showToast(activity,"must_fill_inputs");
                }
                else {
                    String fileName = E_FILE.getTextString();

                    if(universalFolder.contains(fileName)) {
                        if(universalFolder.containsFile(fileName)) {
                            confirmFileOverwriteDialogFragment.show(activity, "overwrite");
                        }
                        else {
                            ToastUtil.showToast(activity,"cannot_overwrite_folder");
                        }
                    }
                    else {
                        user_FILENAME = fileName;
                        user_UNIVERSALFOLDER = universalFolder;

                        isComplete = true;
                        dismiss();
                    }
                }
            }
        });

        TextView T_EXISTINGFILES = findViewById(R.id.export_data_file_dialog_existingFilesTextView);
        if(universalFolder == null) {
            T_EXISTINGFILES.setVisibility(View.GONE);
        }
        else {
            T_EXISTINGFILES.setVisibility(View.VISIBLE);

            ArrayList<String> existingFileNames = universalFolder.getFileNames();
            ArrayList<String> existingFolderNames = universalFolder.getFolderNames();

            StringBuilder s = new StringBuilder();
            if(existingFolderNames == null) {
                s.append("(Problem accessing existing folders)");
            }
            else if(existingFolderNames.isEmpty()) {
                s.append("(No existing folders)");
            }
            else {
                s.append("Existing folders:");

                Collections.sort(existingFolderNames, Comparator.comparing(String::toLowerCase));

                for(String existingFolderName : existingFolderNames) {
                    s.append("\n").append(existingFolderName);
                }
            }

            s.append("\n\n");

            if(existingFileNames == null) {
                s.append("(Problem accessing existing files)");
            }
            else if(existingFileNames.isEmpty()) {
                s.append("(No existing files)");
            }
            else {
                s.append("Existing files:");

                Collections.sort(existingFileNames, Comparator.comparing(String::toLowerCase));

                for(String existingFileName : existingFileNames) {
                    s.append("\n").append(existingFileName);
                }
            }

            T_EXISTINGFILES.setText(s.toString());
        }
    }

    @Override
    public Bundle onSaveInstanceStateImpl(Bundle bundle) {
        bundle.putParcelable("universalFolder", universalFolder);
        return bundle;
    }

    @Override
    public void onRestoreInstanceStateImpl(Bundle bundle) {
        if(bundle != null) {
            universalFolder = bundle.getParcelable("universalFolder");
        }
    }
}
