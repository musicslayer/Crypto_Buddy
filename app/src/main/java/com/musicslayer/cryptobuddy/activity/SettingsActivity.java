package com.musicslayer.cryptobuddy.activity;

import android.content.Intent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;

import androidx.appcompat.widget.Toolbar;

import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.crash.CrashOnItemSelectedListener;
import com.musicslayer.cryptobuddy.persistence.Purchases;
import com.musicslayer.cryptobuddy.util.DateTime;
import com.musicslayer.cryptobuddy.view.BorderedSpinnerView;
import com.musicslayer.cryptobuddy.view.setting.DarkSettingsView;
import com.musicslayer.cryptobuddy.view.setting.DatetimeLocaleSettingsView;
import com.musicslayer.cryptobuddy.view.setting.DeleteAllAddressHistorySettingsView;
import com.musicslayer.cryptobuddy.view.setting.DeleteCustomTokensSettingsView;
import com.musicslayer.cryptobuddy.view.setting.DeleteDownloadedTokensSettingsView;
import com.musicslayer.cryptobuddy.view.setting.DeleteFoundTokensSettingsView;
import com.musicslayer.cryptobuddy.view.setting.MessageSettingsView;
import com.musicslayer.cryptobuddy.view.setting.NumericLocaleSettingsView;
import com.musicslayer.cryptobuddy.view.setting.OrientationSettingsView;
import com.musicslayer.cryptobuddy.view.setting.DeleteAllPortfoliosSettingsView;
import com.musicslayer.cryptobuddy.view.setting.ResetAllSettingsSettingsView;
import com.musicslayer.cryptobuddy.view.setting.ResetEverythingSettingsView;
import com.musicslayer.cryptobuddy.view.setting.SettingsView;
import com.musicslayer.cryptobuddy.view.setting.TimeZoneSettingsView;

import java.time.format.FormatStyle;

public class SettingsActivity extends BaseActivity {
    final String[] settingNames = {
        "datetime",
        "decimal",
        "price",
        "network",
        "timeout",
        "loss",
        "dark",
        "message",
        "orientation",
        "confirm",
        "asset",
        "max_transactions",
        "locale_numeric",
        "locale_datetime",
        "time_zone"
    };

    final String[] settingDisplayNames = {
        "Datetime Format",
        "Number of Decimal Places",
        "Price Display",
        "Networks",
        "Timeout",
        "Loss Values",
        "Dark Mode",
        "Message Length",
        "Orientation",
        "Confirmation",
        "Asset Display",
        "Max Number of Transactions",
        "Numeric Locale",
        "Datetime Locale",
        "Time Zone"
    };

    final String[][] settingOptions = {
        {"Short", "Medium", "Long", "Full"},
        {"Fixed", "Truncated"},
        {"Forward", "Forward and Backward"},
        {"All", "Mainnet"},
        {"10 Seconds", "30 Seconds", "60 Seconds"},
        {"Match Locale", "Negative", "Red", "Parenthesis", "Red Negative", "Red Parenthesis"},
        {"Match System", "On", "Off"},
        {"Short", "Long"},
        {"Match System", "Landscape", "Portrait"},
        {"Use Confirmations", "Do Not Use Confirmations"},
        {"Use Full Asset Names", "Use Asset Symbols"},
        {"500", "1000", "5000"},
        {"Match System", "No Locale"},
        {"Match System", "No Locale"},
        {"Match System"}
    };

    final String[][] settingStrings = {
        {
            "Short format datetime:\n" + DateTime.toDateString(FormatStyle.SHORT),
            "Medium format datetime:\n" + DateTime.toDateString(FormatStyle.MEDIUM),
            "Long format datetime:\n" + DateTime.toDateString(FormatStyle.LONG),
            "Full format datetime:\n" + DateTime.toDateString(FormatStyle.FULL)
        },
        {
            "Show all decimal places expected for the crypto.\n0.00010000 BTC.",
            "Start with all decimal places expected for the crypto, and then truncate trailing zeroes.\n0.0001 BTC."
        },
        {
            "Show price in forward direction. For example, if you buy or sell 1 BTC for 20000 USD, the forward price is 1 BTC / 20000 USD.",
            "Show price in both forward and backward directions. For example, if you buy or sell 1 BTC for 20000 USD, the forward price is 1 BTC / 20000 USD, and the backward price is 1 USD / 0.00005 BTC."
        },
        {
            "Recognize all addresses.",
            "Only recognize Mainnet addresses."
        },
        {
            "10 seconds timeout for each API call.",
            "30 seconds timeout for each API call.",
            "60 seconds timeout for each API call."
        },
        {
            "Show lost assets using the negative format of the chosen numeric locale.",
            "Show lost assets using negative numbers.",
            "Show lost assets using red positive numbers.",
            "Show lost assets using positive numbers in parenthesis.",
            "Show lost assets using red negative numbers.",
            "Show lost assets using red positive numbers in parenthesis."
        },
        {
            "Match system setting for dark mode.",
            "Always use dark mode for this app.",
            "Never use dark mode for this app."
        },
        {
            "Show messages for a shorter time.",
            "Show messages for a longer time."
        },
        {
            "Match system setting for orientation.",
            "Always use landscape for this app.",
            "Always use portrait for this app."
        },
        {
            "Confirm deletes and resets to prevent accidental removals.",
            "Do not use any additional confirmations for deletes and resets."
        },
        {
            "Use full asset names.\nUS Dollar, Bitcoin, Litecoin, Dogecoin\n(Searches always allow choice of symbol or name.)",
            "Use asset symbols.\nUSD, BTC, LTC, DOGE\n(Searches always allow choice of symbol or name.)"
        },
        {
            "Analyze up to 500 transactions per address.",
            "Analyze up to 1000 transactions per address.",
            "Analyze up to 5000 transactions per address.\n** May cause crashes **"
        },
        {
            "Match system setting for numeric locale.",
            "Do not use a numeric locale.\nNumbers will appear in a raw decimal format (e.g., 12345.099)."
        },
        {
            "Match system setting for datetime locale.",
            "Do not use a datetime locale.\nDatetimes will display as the number of milliseconds since January 1, 1970, 00:00:00 GMT"
        },
        {
            "Match system setting for the time zone."
        }
    };

    public int getAdLayoutViewID() {
        return -1;
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    public void createLayout () {
        setContentView(R.layout.activity_settings);

        Toolbar toolbar = findViewById(R.id.settings_toolbar);
        setSupportActionBar(toolbar);

        LinearLayout L = findViewById(R.id.settings_linearLayout);

        BorderedSpinnerView bsv = findViewById(R.id.settings_category_spinner);
        bsv.setOptions(new String[] {"Formatting", "Display Localization", "API", "Appearance", "Other", "Reset"});
        bsv.setOnItemSelectedListener(new CrashOnItemSelectedListener(this) {
            public void onNothingSelectedImpl(AdapterView<?> parent){}
            public void onItemSelectedImpl(AdapterView<?> parent, View view, int pos, long id) {
                L.removeAllViews();

                if(pos == 0) {
                    L.addView(new SettingsView(SettingsActivity.this, settingNames[0], settingDisplayNames[0], settingOptions[0], settingStrings[0]));
                    L.addView(new SettingsView(SettingsActivity.this, settingNames[1], settingDisplayNames[1], settingOptions[1], settingStrings[1]));
                    L.addView(new SettingsView(SettingsActivity.this, settingNames[2], settingDisplayNames[2], settingOptions[2], settingStrings[2]));
                    L.addView(new SettingsView(SettingsActivity.this, settingNames[5], settingDisplayNames[5], settingOptions[5], settingStrings[5]));
                    L.addView(new SettingsView(SettingsActivity.this, settingNames[10], settingDisplayNames[10], settingOptions[10], settingStrings[10]));
                }
                else if(pos == 1) {
                    L.addView(new NumericLocaleSettingsView(SettingsActivity.this, settingNames[12], settingDisplayNames[12], settingOptions[12], settingStrings[12]));
                    L.addView(new DatetimeLocaleSettingsView(SettingsActivity.this, settingNames[13], settingDisplayNames[13], settingOptions[13], settingStrings[13]));
                    L.addView(new TimeZoneSettingsView(SettingsActivity.this, settingNames[14], settingDisplayNames[14], settingOptions[14], settingStrings[14]));
                }
                else if(pos == 2) {
                    L.addView(new SettingsView(SettingsActivity.this, settingNames[3], settingDisplayNames[3], settingOptions[3], settingStrings[3]));
                    L.addView(new SettingsView(SettingsActivity.this, settingNames[4], settingDisplayNames[4], settingOptions[4], settingStrings[4]));
                    L.addView(new SettingsView(SettingsActivity.this, settingNames[11], settingDisplayNames[11], settingOptions[11], settingStrings[11]));
                }
                else if(pos == 3) {
                    L.addView(new MessageSettingsView(SettingsActivity.this, settingNames[7], settingDisplayNames[7], settingOptions[7], settingStrings[7]));
                    L.addView(new DarkSettingsView(SettingsActivity.this, settingNames[6], settingDisplayNames[6], settingOptions[6], settingStrings[6]));
                    L.addView(new OrientationSettingsView(SettingsActivity.this, settingNames[8], settingDisplayNames[8], settingOptions[8], settingStrings[8]));
                }
                else if(pos == 4) {
                    L.addView(new SettingsView(SettingsActivity.this, settingNames[9], settingDisplayNames[9], settingOptions[9], settingStrings[9]));
                }
                else {
                    L.addView(new ResetAllSettingsSettingsView(SettingsActivity.this));
                    L.addView(new DeleteAllAddressHistorySettingsView(SettingsActivity.this));
                    L.addView(new DeleteAllPortfoliosSettingsView(SettingsActivity.this));

                    if(Purchases.isUnlockTokensPurchased) {
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