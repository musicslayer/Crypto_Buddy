package com.musicslayer.cryptobuddy.dialog;

import android.app.Activity;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.result.ActivityResult;
import androidx.annotation.RequiresApi;
import androidx.documentfile.provider.DocumentFile;

import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.activity.BaseActivity;
import com.musicslayer.cryptobuddy.app.App;
import com.musicslayer.cryptobuddy.crash.CrashDialogInterface;
import com.musicslayer.cryptobuddy.crash.CrashView;
import com.musicslayer.cryptobuddy.file.UniversalFile;
import com.musicslayer.cryptobuddy.util.HelpUtil;
import com.musicslayer.cryptobuddy.util.ToastUtil;

public class ExportDataFileNewDialog extends BaseDialog {
    public UniversalFile universalFile;

    public UniversalFile user_UNIVERSALFILE;

    public ExportDataFileNewDialog(Activity activity) {
        super(activity);
    }

    public int getBaseViewID() {
        return R.id.export_data_file_new_dialog;
    }

    public void createLayout(Bundle savedInstanceState) {
        setContentView(R.layout.dialog_export_data_file_new);

        ImageButton helpButton = findViewById(R.id.export_data_file_new_dialog_helpButton);
        helpButton.setOnClickListener(new CrashView.CrashOnClickListener(this.activity) {
            @Override
            public void onClickImpl(View view) {
                HelpUtil.showHelp(ExportDataFileNewDialog.this.activity, R.raw.help_export_data);
            }
        });

        Button B_OVERWRITE_EXISTING_FILE = findViewById(R.id.export_data_file_new_dialog_overwriteExistingFileButton);
        B_OVERWRITE_EXISTING_FILE.setOnClickListener(new CrashView.CrashOnClickListener(this.activity) {
            public void onClickImpl(View v) {
                // The ability to choose a file is only available on API 19 and above
                if(Build.VERSION.SDK_INT >= 19) {
                    Intent documentIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                    documentIntent.setType("*/*");

                    ComponentName documentApp = documentIntent.resolveActivity(activity.getPackageManager());
                    ComponentName unsupportedAction = ComponentName.unflattenFromString("com.android.fallback/.Fallback");
                    if(documentApp != null && !documentApp.equals(unsupportedAction)) {
                        ((BaseActivity)activity).activityResultLauncher.launch(documentIntent);
                    }
                    else {
                        ToastUtil.showToast(activity, "document");
                    }
                }
            }
        });

        Button B_CREATE_NEW_FILE = findViewById(R.id.export_data_file_new_dialog_createNewFileButton);
        B_CREATE_NEW_FILE.setOnClickListener(new CrashView.CrashOnClickListener(this.activity) {
            public void onClickImpl(View v) {
                // The ability to choose a file is only available on API 19 and above
                if(Build.VERSION.SDK_INT >= 19) {
                    Intent documentIntent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                    documentIntent.setType("*/*");

                    ComponentName documentApp = documentIntent.resolveActivity(activity.getPackageManager());
                    ComponentName unsupportedAction = ComponentName.unflattenFromString("com.android.fallback/.Fallback");
                    if(documentApp != null && !documentApp.equals(unsupportedAction)) {
                        ((BaseActivity)activity).activityResultLauncher.launch(documentIntent);
                    }
                    else {
                        ToastUtil.showToast(activity, "document");
                    }
                }
            }
        });

        updateLayout();
    }

    public void updateLayout() {
        TextView T_FILE = findViewById(R.id.export_data_file_new_dialog_dataFileTextView);
        if(universalFile == null) {
            T_FILE.setText("No data file selected.");
        }
        else {
            T_FILE.setText("Data File:\n" + universalFile.getDisplayPath());
        }

        BaseDialogFragment confirmFileOverwriteDialogFragment = BaseDialogFragment.newInstance(ConfirmFileOverwriteDialog.class);
        confirmFileOverwriteDialogFragment.setOnDismissListener(new CrashDialogInterface.CrashOnDismissListener(activity) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                if(((ConfirmFileOverwriteDialog)dialog).isComplete) {
                    user_UNIVERSALFILE = universalFile;

                    isComplete = true;
                    dismiss();
                }
            }
        });
        confirmFileOverwriteDialogFragment.restoreListeners(activity, "overwrite");

        Button B_CONFIRM = findViewById(R.id.export_data_file_new_dialog_confirmButton);
        B_CONFIRM.setOnClickListener(new CrashView.CrashOnClickListener(this.activity) {
            public void onClickImpl(View v) {
                if(universalFile == null) {
                    ToastUtil.showToast(activity,"no_file_selected");
                }
                else {
                    if(universalFile.exists()) {
                        // TODO This always triggers even for a new file.
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
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
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
            ToastUtil.showToast(activity,"file_selection_problem");
        }
    }

    @Override
    public Bundle onSaveInstanceStateImpl(Bundle bundle) {
        bundle.putParcelable("universalFile", universalFile);
        return bundle;
    }

    @Override
    public void onRestoreInstanceStateImpl(Bundle bundle) {
        if(bundle != null) {
            universalFile = bundle.getParcelable("universalFile");
        }
    }
}
