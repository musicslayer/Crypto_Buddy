package com.musicslayer.cryptobuddy.dialog;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.widget.Toolbar;

import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.crash.CrashView;
import com.musicslayer.cryptobuddy.util.ToastUtil;
import com.musicslayer.cryptobuddy.view.red.PageEditText;

import java.math.BigInteger;

public class ChoosePageDialog extends BaseDialog {
    public int user_PAGE;

    public int pageMin;
    public int pageMax;

    public ChoosePageDialog(Activity activity, Integer pageMin, Integer pageMax) {
        super(activity);
        this.pageMin = pageMin;
        this.pageMax = pageMax;
    }

    public int getBaseViewID() {
        return R.id.choose_page_dialog;
    }

    public void createLayout(Bundle savedInstanceState) {
        setContentView(R.layout.dialog_choose_page);

        Toolbar toolbar = findViewById(R.id.choose_page_dialog_toolbar);
        toolbar.setSubtitle("Min: " + pageMin + ", Max: " + pageMax);

        PageEditText E_PAGE = findViewById(R.id.choose_page_dialog_pageEditText);
        E_PAGE.setPageMinMax(pageMin, pageMax);

        Button B_Confirm = findViewById(R.id.choose_page_dialog_confirmButton);
        B_Confirm.setOnClickListener(new CrashView.CrashOnClickListener(this.activity) {
            @Override
            public void onClickImpl(View view) {
                boolean isValid = E_PAGE.test();

                if(!isValid) {
                    ToastUtil.showToast("must_fill_inputs");
                }
                else {
                    user_PAGE = new BigInteger(E_PAGE.getTextString()).intValue();

                    isComplete = true;
                    dismiss();
                }
            }
        });
    }
}
