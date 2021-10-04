package com.musicslayer.cryptobuddy.view.setting;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.musicslayer.cryptobuddy.crash.CrashOnItemSelectedListener;
import com.musicslayer.cryptobuddy.persistence.Settings;
import com.musicslayer.cryptobuddy.view.BorderedSpinnerView;

public class SettingsView extends LinearLayout {
    public SettingsView(Context context) {
        super(context);
    }

    public SettingsView(Context context, String settingName, String settingDisplayName, String[] settingOptions, String[] settingDescriptions) {
        super(context);

        this.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        this.setOrientation(LinearLayout.VERTICAL);

        final LinearLayout L_Row=new LinearLayout(context);
        L_Row.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        L_Row.setOrientation(LinearLayout.HORIZONTAL);
        L_Row.setGravity(Gravity.CENTER_VERTICAL);

        final TextView T=new TextView(context);
        T.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        T.setText(settingDisplayName + ":");

        BorderedSpinnerView bsv = new BorderedSpinnerView(context);
        bsv.setOptions(settingOptions);
        bsv.setMargins(10, 0, 10, 0);

        final TextView prefText=new TextView(context);
        prefText.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        prefText.setText(settingDescriptions[Settings.getSettingValue(settingName)]);


        bsv.spinner.setOnItemSelectedListener(new CrashOnItemSelectedListener(context) {
            public void onNothingSelectedImpl(AdapterView<?> parent){}
            public void onItemSelectedImpl(AdapterView<?> parent, View view, int pos, long id) {
                prefText.setText(settingDescriptions[pos]);
                Settings.setSetting(context, settingName, pos);
            }
        });

        bsv.spinner.setSelection(Settings.getSettingValue(settingName));

        prefText.setPadding(0,0,0, 50);

        L_Row.addView(T);
        L_Row.addView(bsv);

        this.addView(L_Row);
        this.addView(prefText);
    }
}
