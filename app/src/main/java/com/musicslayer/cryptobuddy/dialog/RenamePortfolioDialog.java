package com.musicslayer.cryptobuddy.dialog;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.crash.CrashView;
import com.musicslayer.cryptobuddy.util.ToastUtil;
import com.musicslayer.cryptobuddy.view.red.PlainTextEditText;

public class RenamePortfolioDialog extends BaseDialog {
    String oldName;

    public String user_NEWNAME;

    public RenamePortfolioDialog(Activity activity, String oldName) {
        super(activity);
        this.oldName = oldName;
    }

    public int getBaseViewID() {
        return R.id.rename_portfolio_dialog;
    }

    public void createLayout(Bundle savedInstanceState) {
        setContentView(R.layout.dialog_rename_portfolio);

        TextView T = findViewById(R.id.rename_portfolio_dialog_currentTextView);
        T.setText("Current Portfolio Name: " + oldName);

        final PlainTextEditText E = findViewById(R.id.rename_portfolio_dialog_editText);

        Button B_RENAME = findViewById(R.id.rename_portfolio_dialog_renameButton);
        B_RENAME.setOnClickListener(new CrashView.CrashOnClickListener(this.activity) {
            public void onClickImpl(View v) {
                boolean isValid = E.test();

                if(!isValid) {
                    ToastUtil.showToast("must_fill_inputs");
                }
                else {
                    user_NEWNAME = E.getTextString();

                    isComplete = true;
                    dismiss();
                }
            }
        });
    }
}
