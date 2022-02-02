package com.musicslayer.cryptobuddy.view.settings;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.musicslayer.cryptobuddy.crash.CrashAdapterView;
import com.musicslayer.cryptobuddy.persistence.SettingList;
import com.musicslayer.cryptobuddy.settings.setting.Setting;
import com.musicslayer.cryptobuddy.util.ContextUtil;
import com.musicslayer.cryptobuddy.view.BorderedSpinnerView;

public class StandardSettingsView extends SettingsView {
    public StandardSettingsView(Context context) {
        super(context);
    }

    public StandardSettingsView(Context context, Setting setting) {
        super(context);

        this.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        this.setOrientation(LinearLayout.VERTICAL);

        final LinearLayout L_Row=new LinearLayout(context);
        L_Row.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        L_Row.setOrientation(LinearLayout.HORIZONTAL);
        L_Row.setGravity(Gravity.CENTER_VERTICAL);

        final TextView T=new TextView(context);
        T.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        T.setText(setting.getDisplayName() + ":");

        BorderedSpinnerView bsv = new BorderedSpinnerView(context);
        bsv.setOptions(setting.getModifiedOptionNames());
        bsv.setMargins(10, 0, 10, 0);

        final TextView prefText=new TextView(context);
        prefText.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        int idx = setting.getOptionNames().indexOf(setting.chosenOptionName);
        if(idx == -1) {
            // If saved option choice no longer exists, use default value.
            idx = setting.getOptionNames().indexOf(setting.getDefaultOptionName());
        }
        prefText.setText(setting.getOptionDisplays().get(idx));

        bsv.spinner.setOnItemSelectedListener(new CrashAdapterView.CrashOnItemSelectedListener(context) {
            public void onNothingSelectedImpl(AdapterView<?> parent){}
            public void onItemSelectedImpl(AdapterView<?> parent, View view, int pos, long id) {
                int oldSetting = setting.chosenOptionPosition;

                setting.setSetting(pos);
                prefText.setText(setting.getOptionDisplays().get(pos));
                SettingList.saveSetting(context, setting);

                if(oldSetting != pos && setting.needsRefresh()) {
                    ContextUtil.getActivityFromContext(context).recreate();
                }
            }
        });

        bsv.spinner.setSelection(idx);

        prefText.setPadding(0,0,0, 50);

        L_Row.addView(T);
        L_Row.addView(bsv);

        this.addView(L_Row);
        this.addView(prefText);
    }
}
