package com.musicslayer.cryptobuddy.dialog;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.activity.result.ActivityResult;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.AppCompatButton;
import androidx.documentfile.provider.DocumentFile;

import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.activity.BaseActivity;
import com.musicslayer.cryptobuddy.app.App;
import com.musicslayer.cryptobuddy.crash.CrashView;
import com.musicslayer.cryptobuddy.file.UniversalFile;
import com.musicslayer.cryptobuddy.util.ToastUtil;

import java.io.File;

public class ChooseFolderDialog extends BaseDialog {
    String subfolder;

    public UniversalFile user_UNIVERSALFOLDER;

    public ChooseFolderDialog(Activity activity, String subfolder) {
        super(activity);

        // Pass in "" if you do not want to use a subfolder (null cannot be passed in).
        this.subfolder = subfolder;
    }

    public int getBaseViewID() {
        return R.id.choose_folder_dialog;
    }

    public void createLayout(Bundle savedInstanceState) {
        setContentView(R.layout.dialog_choose_folder);

        updateLayout();
    }

    public void updateLayout() {
        TableLayout tableInternal = findViewById(R.id.choose_folder_dialog_internalFolderTableLayout);
        tableInternal.removeAllViews();

        for(String folderName : App.internalFilesDirs) {
            // Add in the subfolder if one was supplied.
            if(!subfolder.isEmpty()) {
                folderName = folderName + subfolder;
            }

            final String folderName_final = folderName;

            AppCompatButton B = new AppCompatButton(activity);
            B.setText(folderName);
            B.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_folder_24, 0, 0, 0);
            B.setOnClickListener(new CrashView.CrashOnClickListener(activity) {
                @Override
                public void onClickImpl(View view) {
                    user_UNIVERSALFOLDER = UniversalFile.fromFile(new File(folderName_final));

                    isComplete = true;
                    dismiss();
                }
            });

            TableRow.LayoutParams TRP = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);

            TableRow TR = new TableRow(activity);
            TR.addView(B, TRP);
            tableInternal.addView(TR);
        }

        TableLayout tableExternal = findViewById(R.id.choose_folder_dialog_externalFolderTableLayout);
        tableExternal.removeAllViews();

        for(String folderName : App.externalFilesDirs) {
            // Add in the subfolder if one was supplied.
            if(subfolder != null && !"".equals(subfolder)) {
                folderName = folderName + subfolder;
            }

            final String folderName_final = folderName;

            AppCompatButton B = new AppCompatButton(activity);
            B.setText(folderName);
            B.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_folder_24, 0, 0, 0);
            B.setOnClickListener(new CrashView.CrashOnClickListener(activity) {
                @Override
                public void onClickImpl(View view) {
                    user_UNIVERSALFOLDER = UniversalFile.fromFile(new File(folderName_final));

                    isComplete = true;
                    dismiss();
                }
            });

            TableRow.LayoutParams TRP = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);

            TableRow TR = new TableRow(activity);
            TR.addView(B, TRP);
            tableExternal.addView(TR);
        }

        TextView TOther = findViewById(R.id.choose_folder_dialog_otherFolderTextView);
        TableLayout tableOther = findViewById(R.id.choose_folder_dialog_otherFolderTableLayout);
        tableOther.removeAllViews();

        // The ability to choose a folder is only available on API 21 and above
        if(Build.VERSION.SDK_INT >= 21) {
            TOther.setVisibility(View.VISIBLE);
            tableOther.setVisibility(View.VISIBLE);

            AppCompatButton B = new AppCompatButton(activity);
            B.setText("(Choose Other Folder)");
            B.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_folder_24, 0, 0, 0);
            B.setOnClickListener(new CrashView.CrashOnClickListener(activity) {
                @Override
                public void onClickImpl(View view) {
                    Intent documentIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);

                    ComponentName documentApp = documentIntent.resolveActivity(activity.getPackageManager());
                    ComponentName unsupportedAction = ComponentName.unflattenFromString("com.android.fallback/.Fallback");
                    if(documentApp != null && !documentApp.equals(unsupportedAction)) {
                        ((BaseActivity)activity).activityResultLauncher.launch(documentIntent);
                    }
                    else {
                        ToastUtil.showToast(activity, "document");
                    }
                }
            });

            TableRow.LayoutParams TRP = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);

            TableRow TR = new TableRow(activity);
            TR.addView(B, TRP);
            tableOther.addView(TR);
        }
        else {
            TOther.setVisibility(View.GONE);
            tableOther.setVisibility(View.GONE);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onActivityResultImpl(ActivityResult result) {
        boolean isSuccess = true;

        if(result.getResultCode() == Activity.RESULT_OK) {
            try {
                Uri uri = result.getData().getData();
                DocumentFile documentFile = DocumentFile.fromTreeUri(App.applicationContext, uri);

                user_UNIVERSALFOLDER = UniversalFile.fromDocumentFile(documentFile);

                isComplete = true;
                dismiss();
            }
            catch(Exception ignored) {
                isSuccess = false;
            }
        }
        else {
            isSuccess = false;
        }

        if(!isSuccess) {
            ToastUtil.showToast(activity,"folder_selection_problem");
        }
    }
}
