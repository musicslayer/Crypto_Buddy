package com.musicslayer.cryptobuddy.dialog;

import android.app.Activity;
import android.content.ComponentName;
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
import com.musicslayer.cryptobuddy.crash.CrashView;
import com.musicslayer.cryptobuddy.file.UniversalFile;
import com.musicslayer.cryptobuddy.util.HelpUtil;
import com.musicslayer.cryptobuddy.util.ToastUtil;

public class ImportDataFileNewDialog extends BaseDialog {
    public UniversalFile universalFile;

    public UniversalFile user_UNIVERSALFILE;

    public ImportDataFileNewDialog(Activity activity) {
        super(activity);
    }

    public int getBaseViewID() {
        return R.id.import_data_file_new_dialog;
    }

    public void createLayout(Bundle savedInstanceState) {
        setContentView(R.layout.dialog_import_data_file_new);

        ImageButton helpButton = findViewById(R.id.import_data_file_new_dialog_helpButton);
        helpButton.setOnClickListener(new CrashView.CrashOnClickListener(this.activity) {
            @Override
            public void onClickImpl(View view) {
                HelpUtil.showHelp(ImportDataFileNewDialog.this.activity, R.raw.help_import_data);
            }
        });

        Button B_CHOOSE_FILE = findViewById(R.id.import_data_file_new_dialog_chooseFileButton);
        B_CHOOSE_FILE.setOnClickListener(new CrashView.CrashOnClickListener(this.activity) {
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

        updateLayout();
    }

    public void updateLayout() {
        TextView T_FILE = findViewById(R.id.import_data_file_new_dialog_dataFileTextView);
        if(universalFile == null) {
            T_FILE.setText("No data file selected.");
        }
        else {
            T_FILE.setText("Data File:\n" + universalFile.getDisplayPath());
        }

        Button B_CONFIRM = findViewById(R.id.import_data_file_new_dialog_confirmButton);
        B_CONFIRM.setOnClickListener(new CrashView.CrashOnClickListener(this.activity) {
            public void onClickImpl(View v) {
                if(universalFile == null) {
                    ToastUtil.showToast(activity,"no_file_selected");
                }
                else {
                    if(universalFile.exists()) {
                        user_UNIVERSALFILE = universalFile;

                        isComplete = true;
                        dismiss();
                    }
                    else {
                        ToastUtil.showToast(activity, "file_does_not_exist");
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
