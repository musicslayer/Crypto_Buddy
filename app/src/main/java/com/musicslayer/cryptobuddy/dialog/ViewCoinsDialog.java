package com.musicslayer.cryptobuddy.dialog;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.appcompat.widget.Toolbar;

import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.activity.CoinManagerActivity;
import com.musicslayer.cryptobuddy.asset.Asset;
import com.musicslayer.cryptobuddy.asset.coinmanager.CoinManager;
import com.musicslayer.cryptobuddy.asset.crypto.coin.Coin;
import com.musicslayer.cryptobuddy.crash.CrashView;
import com.musicslayer.cryptobuddy.persistence.CoinManagerList;
import com.musicslayer.cryptobuddy.view.asset.CoinView;
import com.musicslayer.cryptobuddy.view.asset.SelectAndSearchView;

import java.util.ArrayList;

public class ViewCoinsDialog extends BaseDialog {
    public String coinType;

    int LAST_CHECK = 0;

    public ViewCoinsDialog(Activity activity, String coinType) {
        super(activity);
        this.coinType = coinType;
    }

    public int getBaseViewID() {
        return R.id.view_coins_dialog;
    }

    public void createLayout(Bundle savedInstanceState) {
        setContentView(R.layout.dialog_view_coins);

        Toolbar toolbar = findViewById(R.id.view_coins_dialog_toolbar);
        toolbar.setTitle("View " + coinType + " Coins");

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
        CoinView coinView = findViewById(R.id.view_coins_dialog_coinView);

        SelectAndSearchView ssv = findViewById(R.id.view_coins_dialog_selectAndSearchView);
        ssv.setChooseAssetListener(new SelectAndSearchView.ChooseAssetListener() {
            @Override
            public void onAssetChosen(Asset asset) {
                coinView.setCoin((Coin)asset, LAST_CHECK != 0);
            }
        });

        ssv.setIncludesFiat(false);
        ssv.setIncludesCoin(true);
        ssv.setIncludesToken(false);

        CoinManager coinManager = CoinManager.getCoinManagerFromCoinType(coinType);

        coinView.setOnDeleteListener(new CoinView.OnDeleteListener() {
            @Override
            public void onDelete(Asset asset) {
                ssv.removeAsset(asset);

                if(LAST_CHECK == 0) {
                    coinManager.removeHardcodedCoin((Coin)asset);
                }
                else if(LAST_CHECK == 1) {
                    coinManager.removeFoundCoin((Coin)asset);
                }
                else if(LAST_CHECK == 2) {
                    coinManager.removeCustomCoin((Coin)asset);
                }

                CoinManagerList.updateCoinManager(activity, coinManager);

                ((CoinManagerActivity)activity).updateLayout();
            }
        });

        if(LAST_CHECK == 0) {
            ssv.setCoinOptions(coinManager.hardcoded_coins);
        }
        else if(LAST_CHECK == 1) {
            ssv.setCoinOptions(coinManager.found_coins);
        }
        else if(LAST_CHECK == 2) {
            ssv.setCoinOptions(coinManager.custom_coins);
        }

        ArrayList<CoinManager> coinManagerArrayList = new ArrayList<>();
        coinManagerArrayList.add(coinManager);
        ssv.setCoinManagerOptions(coinManagerArrayList);

        ssv.chooseCoin(coinType);
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
