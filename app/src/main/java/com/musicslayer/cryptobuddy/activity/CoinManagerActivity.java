package com.musicslayer.cryptobuddy.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TableLayout;

import androidx.annotation.NonNull;

import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.asset.coinmanager.CoinManager;
import com.musicslayer.cryptobuddy.crash.CrashDialogInterface;
import com.musicslayer.cryptobuddy.crash.CrashView;
import com.musicslayer.cryptobuddy.dialog.AddCustomCoinDialog;
import com.musicslayer.cryptobuddy.dialog.BaseDialogFragment;
import com.musicslayer.cryptobuddy.util.HelpUtil;
import com.musicslayer.cryptobuddy.view.CoinManagerView;

import java.util.ArrayList;

public class CoinManagerActivity extends BaseActivity {
    ArrayList<CoinManagerView> coinManagerViewArrayList;

    @Override
    public int getAdLayoutViewID() {
        return R.id.coin_manager_adLayout;
    }

    @Override
    public void onBackPressedImpl() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    @Override
    public void createLayout(Bundle savedInstanceState) {
        setContentView(R.layout.activity_coin_manager);

        ImageButton helpButton = findViewById(R.id.coin_manager_helpButton);
        helpButton.setOnClickListener(new CrashView.CrashOnClickListener(this) {
            @Override
            public void onClickImpl(View view) {
                HelpUtil.showHelp(CoinManagerActivity.this, R.raw.help_coin_manager);
            }
        });

        TableLayout tableLayout = findViewById(R.id.coin_manager_tableLayout);

        // For now, there is only one type of Coin.
        coinManagerViewArrayList = new ArrayList<>();
        CoinManager coinManager = CoinManager.getCoinManagerFromKey("BaseCoinManager");
        CoinManagerView coinManagerView = new CoinManagerView(CoinManagerActivity.this, coinManager);
        coinManagerView.updateLayout();

        coinManagerViewArrayList.add(coinManagerView);
        tableLayout.addView(coinManagerView);

        BaseDialogFragment addCustomCoinDialogFragment = BaseDialogFragment.newInstance(AddCustomCoinDialog.class);
        addCustomCoinDialogFragment.setOnDismissListener(new CrashDialogInterface.CrashOnDismissListener(this) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                if(((AddCustomCoinDialog)dialog).isComplete) {
                    // It's easier to just update all of them.
                    for(CoinManagerView coinManagerView : coinManagerViewArrayList) {
                        coinManagerView.updateLayout();
                    }
                }
            }
        });
        addCustomCoinDialogFragment.restoreListeners(this, "add_custom_coin");

        Button B_CustomCoin = findViewById(R.id.coin_manager_customCoinButton);
        B_CustomCoin.setOnClickListener(new CrashView.CrashOnClickListener(this) {
            @Override
            public void onClickImpl(View view) {
                addCustomCoinDialogFragment.show(CoinManagerActivity.this, "add_custom_coin");
            }
        });
    }

    @Override
    public void onSaveInstanceStateImpl(@NonNull Bundle bundle) {
        bundle.putParcelable("coinManagerView", coinManagerViewArrayList.get(0).onSaveInstanceState());
    }

    @Override
    public void onRestoreInstanceStateImpl(Bundle bundle) {
        if(bundle != null) {
            coinManagerViewArrayList.get(0).onRestoreInstanceState(bundle.getParcelable("coinManagerView"));
        }
    }
}