package com.musicslayer.cryptobuddy.dialog;

import android.app.Activity;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResult;
import androidx.documentfile.provider.DocumentFile;

import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.activity.BaseActivity;
import com.musicslayer.cryptobuddy.app.App;
import com.musicslayer.cryptobuddy.crash.CrashDialogInterface;
import com.musicslayer.cryptobuddy.crash.CrashView;
import com.musicslayer.cryptobuddy.file.UniversalFile;
import com.musicslayer.cryptobuddy.util.HelpUtil;
import com.musicslayer.cryptobuddy.util.ToastUtil;
import com.musicslayer.cryptobuddy.view.ToggleButton;
import com.musicslayer.cryptobuddy.view.red.FileEditText;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class ExportDataFileDialog extends BaseDialog {
    public final static String DATA_FOLDER = "exports";

    public UniversalFile universalFile;
    public UniversalFile universalFolder;

    public UniversalFile user_UNIVERSALFILE;
    public UniversalFile user_UNIVERSALFOLDER;
    public String user_FILENAME;

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
                HelpUtil.showHelp(ExportDataFileDialog.this.activity, R.raw.help_export_data_file);
            }
        });

        Button B_OVERWRITE_EXISTING_FILE = findViewById(R.id.export_data_file_dialog_overwriteExistingFileButton);
        B_OVERWRITE_EXISTING_FILE.setOnClickListener(new CrashView.CrashOnClickListener(this.activity) {
            public void onClickImpl(View v) {
                Intent documentIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                documentIntent.setType("*/*");

                ComponentName documentApp = documentIntent.resolveActivity(activity.getPackageManager());
                ComponentName unsupportedAction = ComponentName.unflattenFromString("com.android.fallback/.Fallback");
                if(documentApp != null && !documentApp.equals(unsupportedAction)) {
                    ((BaseActivity)activity).activityResultLauncher.launch(documentIntent);
                }
                else {
                    ToastUtil.showToast("document");
                }
            }
        });

        Button B_CREATE_NEW_FILE = findViewById(R.id.export_data_file_dialog_createNewFileButton);
        B_CREATE_NEW_FILE.setOnClickListener(new CrashView.CrashOnClickListener(this.activity) {
            public void onClickImpl(View v) {
                Intent documentIntent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                documentIntent.setType("*/*");

                ComponentName documentApp = documentIntent.resolveActivity(activity.getPackageManager());
                ComponentName unsupportedAction = ComponentName.unflattenFromString("com.android.fallback/.Fallback");
                if(documentApp != null && !documentApp.equals(unsupportedAction)) {
                    ((BaseActivity)activity).activityResultLauncher.launch(documentIntent);
                }
                else {
                    ToastUtil.showToast("document");
                }
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

        Button B_CHOOSE_FOLDER = findViewById(R.id.export_data_file_dialog_chooseFolderButton);
        B_CHOOSE_FOLDER.setOnClickListener(new CrashView.CrashOnClickListener(this.activity) {
            public void onClickImpl(View v) {
                chooseFolderDialogFragment.show(activity, "choose_folder");
            }
        });

        ToggleButton B_TOGGLE = findViewById(R.id.export_data_file_dialog_toggleButton);
        B_TOGGLE.setOptions("File View", "Folder View");

        B_TOGGLE.setVisibility(View.VISIBLE);
        B_TOGGLE.setAdditionalOnClickListener(new CrashView.CrashOnClickListener(this.activity) {
            public void onClickImpl(View v) {
                updateLayout();
            }
        });

        updateLayout();
    }

    public void updateLayout() {
        ToggleButton B_TOGGLE = findViewById(R.id.export_data_file_dialog_toggleButton);
        LinearLayout L_FILE = findViewById(R.id.export_data_file_dialog_fileLinearLayout);
        LinearLayout L_FOLDER = findViewById(R.id.export_data_file_dialog_folderLinearLayout);
        if(B_TOGGLE.toggleState) {
            L_FILE.setVisibility(View.GONE);
            L_FOLDER.setVisibility(View.VISIBLE);
        }
        else {
            L_FILE.setVisibility(View.VISIBLE);
            L_FOLDER.setVisibility(View.GONE);
        }

        TextView T_FILE = findViewById(R.id.export_data_file_dialog_dataFileTextView);
        if(universalFile == null) {
            T_FILE.setText("No data file selected.");
        }
        else {
            T_FILE.setText("Data File:\n" + universalFile.getDisplayPath());
        }

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
                    // Assign all of these, even though one set is null.
                    user_UNIVERSALFILE = universalFile;
                    user_UNIVERSALFOLDER = universalFolder;
                    user_FILENAME = E_FILE.getTextString();

                    isComplete = true;
                    dismiss();
                }
            }
        });
        confirmFileOverwriteDialogFragment.restoreListeners(activity, "overwrite");

        Button B_CONFIRM_FILE = findViewById(R.id.export_data_file_dialog_confirmFileButton);
        B_CONFIRM_FILE.setOnClickListener(new CrashView.CrashOnClickListener(this.activity) {
            public void onClickImpl(View v) {
                if(universalFile == null) {
                    ToastUtil.showToast("no_file_selected");
                }
                else {
                    if(universalFile.exists()) {
                        // Since the document browser may create the file before returning, we may end up here even for a "new file".
                        confirmFileOverwriteDialogFragment.show(activity, "overwrite");
                    }
                    else {
                        user_UNIVERSALFILE = universalFile;

                        isComplete = true;
                        dismiss();
                    }
                }
            }
        });

        Button B_CONFIRM_FOLDER = findViewById(R.id.export_data_file_dialog_confirmFolderButton);
        B_CONFIRM_FOLDER.setOnClickListener(new CrashView.CrashOnClickListener(this.activity) {
            public void onClickImpl(View v) {
                boolean isValid = E_FILE.test();

                if(universalFolder == null) {
                    ToastUtil.showToast("no_folder_selected");
                }
                else if(!isValid) {
                    ToastUtil.showToast("must_fill_inputs");
                }
                else {
                    String fileName = E_FILE.getTextString();

                    if(universalFolder.contains(fileName)) {
                        if(universalFolder.containsFile(fileName)) {
                            confirmFileOverwriteDialogFragment.show(activity, "overwrite");
                        }
                        else {
                            ToastUtil.showToast("cannot_overwrite_folder");
                        }
                    }
                    else {
                        user_UNIVERSALFOLDER = universalFolder;
                        user_FILENAME = fileName;

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
    public void onActivityResultImpl(ActivityResult result) {
        boolean isSuccess = true;

        if(result.getResultCode() == Activity.RESULT_OK) {
            try {
                Uri uri = result.getData().getData();
                DocumentFile documentFile = DocumentFile.fromSingleUri(App.applicationContext, uri);

                universalFile = UniversalFile.fromDocumentFile(documentFile);

                updateLayout();
            }
            catch(Exception ignored) {
                isSuccess = false;
            }
        }
        else {
            isSuccess = false;
        }

        if(!isSuccess) {
            ToastUtil.showToast("file_selection_problem");
        }
    }

    @Override
    public Bundle onSaveInstanceStateImpl(Bundle bundle) {
        bundle.putParcelable("universalFile", universalFile);
        bundle.putParcelable("universalFolder", universalFolder);
        return bundle;
    }

    @Override
    public void onRestoreInstanceStateImpl(Bundle bundle) {
        if(bundle != null) {
            universalFile = bundle.getParcelable("universalFile");
            universalFolder = bundle.getParcelable("universalFolder");
        }
    }
}
