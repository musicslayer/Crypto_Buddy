package com.musicslayer.cryptobuddy.activity;

import android.content.Intent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;

import androidx.appcompat.widget.Toolbar;

import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.crash.CrashAdapterView;
import com.musicslayer.cryptobuddy.persistence.Purchases;
import com.musicslayer.cryptobuddy.settings.Setting;
import com.musicslayer.cryptobuddy.view.BorderedSpinnerView;
import com.musicslayer.cryptobuddy.view.settings.DeleteAllAddressHistorySettingsView;
import com.musicslayer.cryptobuddy.view.settings.DeleteCustomTokensSettingsView;
import com.musicslayer.cryptobuddy.view.settings.DeleteDownloadedTokensSettingsView;
import com.musicslayer.cryptobuddy.view.settings.DeleteFoundTokensSettingsView;
import com.musicslayer.cryptobuddy.view.settings.DeleteAllPortfoliosSettingsView;
import com.musicslayer.cryptobuddy.view.settings.MessageSettingsView;
import com.musicslayer.cryptobuddy.view.settings.ResetAllSettingsSettingsView;
import com.musicslayer.cryptobuddy.view.settings.ResetEverythingSettingsView;
import com.musicslayer.cryptobuddy.view.settings.SettingsView;

public class SettingsActivity extends BaseActivity {
    public int getAdLayoutViewID() {
        return -1;
    }

    @Override
    public void onBackPressedImpl() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    public void createLayout () {
        setContentView(R.layout.activity_settings);

        Toolbar toolbar = findViewById(R.id.settings_toolbar);
        setSupportActionBar(toolbar);

        LinearLayout L = findViewById(R.id.settings_linearLayout);

        BorderedSpinnerView bsv = findViewById(R.id.settings_category_spinner);
        bsv.setOptions(new String[] {"Formatting", "Display Localization", "API", "Appearance", "Confirmations", "Reset"});
        bsv.setOnItemSelectedListener(new CrashAdapterView.CrashOnItemSelectedListener(this) {
            public void onNothingSelectedImpl(AdapterView<?> parent){}
            public void onItemSelectedImpl(AdapterView<?> parent, View view, int pos, long id) {
                L.removeAllViews();

                if(pos == 0) {
                    L.addView(new SettingsView(SettingsActivity.this, Setting.getSettingFromKey("DatetimeFormatSetting")));
                    L.addView(new SettingsView(SettingsActivity.this, Setting.getSettingFromKey("NumberDecimalPlacesSetting")));
                    L.addView(new SettingsView(SettingsActivity.this, Setting.getSettingFromKey("PriceDisplaySetting")));
                    L.addView(new SettingsView(SettingsActivity.this, Setting.getSettingFromKey("LossValuesSetting")));
                    L.addView(new SettingsView(SettingsActivity.this, Setting.getSettingFromKey("AssetDisplaySetting")));
                }
                else if(pos == 1) {
                    L.addView(new SettingsView(SettingsActivity.this, Setting.getSettingFromKey("NumericLocaleSetting")));
                    L.addView(new SettingsView(SettingsActivity.this, Setting.getSettingFromKey("DatetimeLocaleSetting")));
                    L.addView(new SettingsView(SettingsActivity.this, Setting.getSettingFromKey("TimeZoneSetting")));
                }
                else if(pos == 2) {
                    L.addView(new SettingsView(SettingsActivity.this, Setting.getSettingFromKey("NetworksSetting")));
                    L.addView(new SettingsView(SettingsActivity.this, Setting.getSettingFromKey("TimeoutSetting")));
                    L.addView(new SettingsView(SettingsActivity.this, Setting.getSettingFromKey("MaxNumberTransactionsSetting")));
                    L.addView(new SettingsView(SettingsActivity.this, Setting.getSettingFromKey("NumberTransactionsPerPageSetting")));
                }
                else if(pos == 3) {
                    L.addView(new MessageSettingsView(SettingsActivity.this, Setting.getSettingFromKey("MessageLengthSetting")));
                    L.addView(new SettingsView(SettingsActivity.this, Setting.getSettingFromKey("DarkModeSetting")));
                    L.addView(new SettingsView(SettingsActivity.this, Setting.getSettingFromKey("OrientationSetting")));
                }
                else if(pos == 4) {
                    L.addView(new SettingsView(SettingsActivity.this, Setting.getSettingFromKey("ConfirmationSetting")));
                }
                else {
                    L.addView(new ResetAllSettingsSettingsView(SettingsActivity.this));
                    L.addView(new DeleteAllAddressHistorySettingsView(SettingsActivity.this));
                    L.addView(new DeleteAllPortfoliosSettingsView(SettingsActivity.this));

                    if(Purchases.isUnlockTokensPurchased()) {
                        L.addView(new DeleteDownloadedTokensSettingsView(SettingsActivity.this));
                        L.addView(new DeleteFoundTokensSettingsView(SettingsActivity.this));
                        L.addView(new DeleteCustomTokensSettingsView(SettingsActivity.this));
                    }

                    // This must be last, as it resets everything.
                    L.addView(new ResetEverythingSettingsView(SettingsActivity.this));
                }
            }
        });
    }
}