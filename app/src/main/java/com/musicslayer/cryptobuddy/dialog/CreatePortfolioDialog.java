package com.musicslayer.cryptobuddy.dialog;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.crash.CrashView;
import com.musicslayer.cryptobuddy.view.red.PlainTextEditText;

public class CreatePortfolioDialog extends BaseDialog {
    public String user_NAME;

    public CreatePortfolioDialog(Activity activity) {
        super(activity);
    }

    public int getBaseViewID() {
        return R.id.create_portfolio_dialog;
    }

    public void createLayout(Bundle savedInstanceState) {
        setContentView(R.layout.dialog_create_portfolio);

        final PlainTextEditText E = findViewById(R.id.create_portfolio_dialog_editText);

        Button B_CREATE = findViewById(R.id.create_portfolio_dialog_createButton);
        B_CREATE.setOnClickListener(new CrashView.CrashOnClickListener(this.activity) {
            public void onClickImpl(View v) {
                if(E.test()) {
                    user_NAME = E.getTextString();

                    isComplete = true;
                    dismiss();
                }
            }
        });
    }
}
