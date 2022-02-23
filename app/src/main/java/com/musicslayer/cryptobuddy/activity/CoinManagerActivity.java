package com.musicslayer.cryptobuddy.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TableRow;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;

import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.asset.coinmanager.CoinManager;
import com.musicslayer.cryptobuddy.crash.CrashDialogInterface;
import com.musicslayer.cryptobuddy.crash.CrashView;
import com.musicslayer.cryptobuddy.data.persistent.app.CoinManagerList;
import com.musicslayer.cryptobuddy.data.persistent.app.PersistentAppDataStore;
import com.musicslayer.cryptobuddy.data.persistent.user.PersistentUserDataStore;
import com.musicslayer.cryptobuddy.data.persistent.user.SettingList;
import com.musicslayer.cryptobuddy.dialog.BaseDialogFragment;
import com.musicslayer.cryptobuddy.dialog.ConfirmDeleteCoinsDialog;
import com.musicslayer.cryptobuddy.dialog.DeleteCoinsDialog;
import com.musicslayer.cryptobuddy.settings.setting.Setting;
import com.musicslayer.cryptobuddy.util.HelpUtil;
import com.musicslayer.cryptobuddy.util.ToastUtil;
import com.musicslayer.cryptobuddy.view.asset.CoinManagerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class CoinManagerActivity extends BaseActivity {
    ArrayList<CoinManagerView> coinManagerViewArrayList;
    public ArrayList<String> choices;

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

        BaseDialogFragment confirmDeleteCoinsDialogFragment = BaseDialogFragment.newInstance(ConfirmDeleteCoinsDialog.class, "All");
        confirmDeleteCoinsDialogFragment.setOnDismissListener(new CrashDialogInterface.CrashOnDismissListener(CoinManagerActivity.this) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                if(((ConfirmDeleteCoinsDialog)dialog).isComplete) {
                    if(choices.contains("found")) {
                        CoinManager.resetAllFoundCoins();
                    }
                    if(choices.contains("custom")) {
                        CoinManager.resetAllCustomCoins();
                    }

                    PersistentAppDataStore.getInstance(CoinManagerList.class).saveAllData();

                    Setting setting = Setting.getSettingFromKey("DefaultCoinSetting");
                    setting.refreshSetting();
                    PersistentUserDataStore.getInstance(SettingList.class).saveSetting(setting);

                    ToastUtil.showToast("reset_coins");

                    updateLayout();
                }
            }
        });
        confirmDeleteCoinsDialogFragment.restoreListeners(this, "confirm_delete_all_coins");

        BaseDialogFragment deleteCoinsDialogFragment = BaseDialogFragment.newInstance(DeleteCoinsDialog.class, "All");
        deleteCoinsDialogFragment.setOnDismissListener(new CrashDialogInterface.CrashOnDismissListener(CoinManagerActivity.this) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                if(((DeleteCoinsDialog)dialog).isComplete) {
                    choices = ((DeleteCoinsDialog)dialog).user_CHOICES;
                    confirmDeleteCoinsDialogFragment.show(CoinManagerActivity.this, "confirm_delete_all_coins");
                }
            }
        });
        deleteCoinsDialogFragment.restoreListeners(CoinManagerActivity.this, "delete_all_coins");

        AppCompatButton B_DELETE = findViewById(R.id.coin_manager_deleteAllCoinsButton);
        B_DELETE.setOnClickListener(new CrashView.CrashOnClickListener(CoinManagerActivity.this) {
            public void onClickImpl(View v) {
                deleteCoinsDialogFragment.show(CoinManagerActivity.this, "delete_all_coins");
            }
        });

        updateLayout();
    }

    public void updateLayout() {
        TableLayout tableLayout = findViewById(R.id.coin_manager_tableLayout);
        TableRow firstRow = findViewById(R.id.coin_manager_tableRow1);

        tableLayout.removeAllViews();
        tableLayout.addView(firstRow);

        ArrayList<String> coinTypes = CoinManager.coinManagers_coin_types;
        Collections.sort(coinTypes, Comparator.comparing(String::toLowerCase));

        coinManagerViewArrayList = new ArrayList<>();
        for(String coinType : coinTypes) {
            CoinManager coinManager = CoinManager.getCoinManagerFromCoinType(coinType);
            CoinManagerView coinManagerView = new CoinManagerView(CoinManagerActivity.this, coinManager);
            coinManagerView.updateLayout();

            coinManagerViewArrayList.add(coinManagerView);
            tableLayout.addView(coinManagerView);
        }
    }

    @Override
    public void onSaveInstanceStateImpl(@NonNull Bundle bundle) {
        for(CoinManagerView coinManagerView : coinManagerViewArrayList) {
            bundle.putParcelable("coinManagerView_" + coinManagerView.coinManager.getCoinType(), coinManagerView.onSaveInstanceState());
        }
        bundle.putStringArrayList("choices", choices);
    }

    @Override
    public void onRestoreInstanceStateImpl(Bundle bundle) {
        if(bundle != null) {
            for(CoinManagerView coinManagerView : coinManagerViewArrayList) {
                coinManagerView.onRestoreInstanceState(bundle.getParcelable("coinManagerView_" + coinManagerView.coinManager.getCoinType()));
            }
            choices = bundle.getStringArrayList("choices");
        }
    }
}