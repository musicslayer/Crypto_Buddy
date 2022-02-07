package com.musicslayer.cryptobuddy.dialog;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.asset.fiatmanager.FiatManager;
import com.musicslayer.cryptobuddy.crash.CrashView;
import com.musicslayer.cryptobuddy.view.SelectAndSearchView;

public class ViewFiatsDialog extends BaseDialog {
    int LAST_CHECK = 0;

    public ViewFiatsDialog(Activity activity) {
        super(activity);
    }

    public int getBaseViewID() {
        return R.id.view_fiats_dialog;
    }

    public void createLayout(Bundle savedInstanceState) {
        setContentView(R.layout.dialog_view_fiats);

        RadioGroup radioGroup = findViewById(R.id.view_fiats_dialog_radioGroup);
        RadioButton[] rb = new RadioButton[3];

        rb[0] = findViewById(R.id.view_fiats_dialog_hardcodedRadioButton);
        rb[0].setOnClickListener(new CrashView.CrashOnClickListener(this.activity) {
            public void onClickImpl(View v) {
                LAST_CHECK = 0;
                updateLayout();
            }
        });

        rb[1] = findViewById(R.id.view_fiats_dialog_foundRadioButton);
        rb[1].setOnClickListener(new CrashView.CrashOnClickListener(this.activity) {
            public void onClickImpl(View v) {
                LAST_CHECK = 1;
                updateLayout();
            }
        });

        rb[2] = findViewById(R.id.view_fiats_dialog_customRadioButton);
        rb[2].setOnClickListener(new CrashView.CrashOnClickListener(this.activity) {
            public void onClickImpl(View v) {
                LAST_CHECK = 2;
                updateLayout();
            }
        });

        radioGroup.check(rb[LAST_CHECK].getId());
        rb[LAST_CHECK].callOnClick();

        updateLayout();
    }

    public void updateLayout() {
        SelectAndSearchView ssv = findViewById(R.id.view_fiats_dialog_selectAndSearchView);
        ssv.setIncludesFiat(true);
        ssv.setIncludesCoin(false);
        ssv.setIncludesToken(false);

        FiatManager fiatManager = FiatManager.getDefaultFiatManager();

        if(LAST_CHECK == 0) {
            ssv.setFiatOptions(fiatManager.hardcoded_fiats);
        }
        else if(LAST_CHECK == 1) {
            ssv.setFiatOptions(fiatManager.found_fiats);
        }
        else if(LAST_CHECK == 2) {
            ssv.setFiatOptions(fiatManager.custom_fiats);
        }

        ssv.chooseFiat();
    }

    @Override
    public Bundle onSaveInstanceStateImpl(Bundle bundle) {
        bundle.putInt("lastcheck", LAST_CHECK);
        return bundle;
    }

    @Override
    public void onRestoreInstanceStateImpl(Bundle bundle) {
        if(bundle != null) {
            LAST_CHECK = bundle.getInt("lastcheck");
        }
    }
}
