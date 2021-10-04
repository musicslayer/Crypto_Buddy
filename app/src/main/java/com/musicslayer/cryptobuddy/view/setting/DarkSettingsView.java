package com.musicslayer.cryptobuddy.view.setting;

import android.app.Activity;
import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.musicslayer.cryptobuddy.crash.CrashOnItemSelectedListener;
import com.musicslayer.cryptobuddy.persistence.Settings;
import com.musicslayer.cryptobuddy.view.BorderedSpinnerView;

public class DarkSettingsView extends LinearLayout {
    public DarkSettingsView(Context context) {
        super(context);
    }

    public DarkSettingsView(Context context, String settingName, String settingDisplayName, String[] settingOptions, String[] settingDescriptions) {
        super(context);

        this.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        this.setOrientation(LinearLayout.VERTICAL);

        final LinearLayout L_Row=new LinearLayout(context);
        L_Row.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        L_Row.setOrientation(LinearLayout.HORIZONTAL);
        L_Row.setGravity(Gravity.CENTER_VERTICAL);

        final TextView T=new TextView(context);
        T.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        T.setText(settingDisplayName + ":");

        BorderedSpinnerView bsv = new BorderedSpinnerView(context);
        bsv.setOptions(settingOptions);
        bsv.setMargins(10, 0, 10, 0);

        final TextView prefText=new TextView(context);
        prefText.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        prefText.setText(settingDescriptions[Settings.getSettingValue(settingName)]);

        bsv.spinner.setOnItemSelectedListener(new CrashOnItemSelectedListener(context) {
            public void onNothingSelectedImpl(AdapterView<?> parent){}
            public void onItemSelectedImpl(AdapterView<?> parent, View view, int pos, long id) {
                prefText.setText(settingDescriptions[pos]);

                int oldSetting = Settings.getSettingValue(settingName);
                Settings.setSetting(context, settingName, pos);

                if(oldSetting != pos) {
                    ((Activity)context).recreate();
                }
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
