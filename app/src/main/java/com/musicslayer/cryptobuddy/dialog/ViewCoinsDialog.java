package com.musicslayer.cryptobuddy.dialog;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.asset.coinmanager.CoinManager;
import com.musicslayer.cryptobuddy.crash.CrashView;
import com.musicslayer.cryptobuddy.view.SelectAndSearchView;

public class ViewCoinsDialog extends BaseDialog {
    int LAST_CHECK = 0;

    public ViewCoinsDialog(Activity activity) {
        super(activity);
    }

    public int getBaseViewID() {
        return R.id.view_coins_dialog;
    }

    public void createLayout(Bundle savedInstanceState) {
        setContentView(R.layout.dialog_view_coins);

        RadioGroup radioGroup = findViewById(R.id.view_coins_dialog_radioGroup);
        RadioButton[] rb = new RadioButton[3];

        rb[0] = findViewById(R.id.view_coins_dialog_hardcodedRadioButton);
        rb[0].setOnClickListener(new CrashView.CrashOnClickListener(this.activity) {
            public void onClickImpl(View v) {
                LAST_CHECK = 0;
                updateLayout();
            }
        });

        rb[1] = findViewById(R.id.view_coins_dialog_foundRadioButton);
        rb[1].setOnClickListener(new CrashView.CrashOnClickListener(this.activity) {
            public void onClickImpl(View v) {
                LAST_CHECK = 1;
                updateLayout();
            }
        });

        rb[2] = findViewById(R.id.view_coins_dialog_customRadioButton);
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
        SelectAndSearchView ssv = findViewById(R.id.view_coins_dialog_selectAndSearchView);
        ssv.setIncludesFiat(false);
        ssv.setIncludesCoin(true);
        ssv.setIncludesToken(false);

        CoinManager coinManager = CoinManager.getDefaultCoinManager();

        if(LAST_CHECK == 0) {
            ssv.setCoinOptions(coinManager.hardcoded_coins);
        }
        else if(LAST_CHECK == 1) {
            ssv.setCoinOptions(coinManager.found_coins);
        }
        else if(LAST_CHECK == 2) {
            ssv.setCoinOptions(coinManager.custom_coins);
        }

        ssv.chooseCoin();
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
