package com.musicslayer.cryptobuddy.dialog;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.activity.result.ActivityResult;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.AppCompatButton;
import androidx.documentfile.provider.DocumentFile;

import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.app.App;
import com.musicslayer.cryptobuddy.crash.CrashView;
import com.musicslayer.cryptobuddy.util.ToastUtil;

import java.util.ArrayList;

public class ChooseFolderDialog extends BaseDialog {
    public String user_FOLDERNAME;
    public Uri user_URI;
    public boolean user_ISURI;

    public ChooseFolderDialog(Activity activity) {
        super(activity);
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
            AppCompatButton B = new AppCompatButton(activity);
            B.setText(folderName);
            B.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_folder_24, 0, 0, 0);
            B.setOnClickListener(new CrashView.CrashOnClickListener(activity) {
                @Override
                public void onClickImpl(View view) {
                    user_FOLDERNAME = folderName;
                    user_URI = null;
                    user_ISURI = false;

                    isComplete = true;
                    dismiss();
                }
            });

            TableRow.LayoutParams TRP = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
            TRP.setMargins(80,0,0,0);

            TableRow TR = new TableRow(activity);
            TR.addView(B);
            tableInternal.addView(TR);
        }

        TableLayout tableExternal = findViewById(R.id.choose_folder_dialog_externalFolderTableLayout);
        tableExternal.removeAllViews();

        for(String folderName : App.externalFilesDirs) {
            AppCompatButton B = new AppCompatButton(activity);
            B.setText(folderName);
            B.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_folder_24, 0, 0, 0);
            B.setOnClickListener(new CrashView.CrashOnClickListener(activity) {
                @Override
                public void onClickImpl(View view) {
                    user_FOLDERNAME = folderName;
                    user_URI = null;
                    user_ISURI = false;

                    isComplete = true;
                    dismiss();
                }
            });

            TableRow.LayoutParams TRP = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
            TRP.setMargins(80,0,0,0);

            TableRow TR = new TableRow(activity);
            TR.addView(B);
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

                    // TODO This check doesn't work...?
                    ComponentName documentApp = documentIntent.resolveActivity(activity.getPackageManager());
                    ComponentName unsupportedAction = ComponentName.unflattenFromString("com.android.fallback/.Fallback");
                    //if(documentApp != null && !documentApp.equals(unsupportedAction)) {
                        ArrayList<BaseDialogFragment> bdfArrayList = BaseDialogFragment.getAllBaseDialogFragments(activity);
                        BaseDialogFragment bdf = bdfArrayList.get(bdfArrayList.size() - 1);
                        bdf.activityResultLauncher.launch(documentIntent);
                    //}
                    //else {
                    //    ToastUtil.showToast(activity,"document");
                    //}
                }
            });

            TableRow.LayoutParams TRP = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
            TRP.setMargins(80,0,0,0);

            TableRow TR = new TableRow(activity);
            TR.addView(B);
            tableOther.addView(TR);
        }
        else {
            TOther.setVisibility(View.GONE);
            tableOther.setVisibility(View.GONE);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT) // We only call this on API > 21
    @Override
    public void onActivityResultComplete(ActivityResult result) {
        boolean isSuccess = true;

        if(result.getResultCode() == Activity.RESULT_OK) {
            // Turn the returned Uri into the full folder path.
            // This should only be called on newer Android versions, so we can use the MediaStore.
            try {
                Uri uri = result.getData().getData();
                DocumentFile documentFile = DocumentFile.fromTreeUri(activity, uri);

                user_FOLDERNAME = DocumentsContract.getDocumentId(documentFile.getUri());
                user_URI = uri;
                user_ISURI = true;

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
