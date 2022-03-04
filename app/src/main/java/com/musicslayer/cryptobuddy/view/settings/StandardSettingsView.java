package com.musicslayer.cryptobuddy.view.settings;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatTextView;

import com.musicslayer.cryptobuddy.crash.CrashAdapterView;
import com.musicslayer.cryptobuddy.data.persistent.user.PersistentUserDataStore;
import com.musicslayer.cryptobuddy.data.persistent.user.SettingList;
import com.musicslayer.cryptobuddy.settings.setting.Setting;
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

        final TextView T=new AppCompatTextView(context);
        T.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        T.setText(setting.getDisplayName() + ":");

        BorderedSpinnerView bsv = new BorderedSpinnerView(context);
        bsv.setOptions(setting.getModifiedOptionNames());
        bsv.setMargins(10, 0, 10, 0);

        final TextView prefText=new AppCompatTextView(context);
        prefText.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        prefText.setText(setting.chosenOptionDisplay);

        bsv.spinner.setOnItemSelectedListener(new CrashAdapterView.CrashOnItemSelectedListener(context) {
            public void onNothingSelectedImpl(AdapterView<?> parent){}
            public void onItemSelectedImpl(AdapterView<?> parent, View view, int pos, long id) {
                setting.setSetting(pos);
                prefText.setText(setting.chosenOptionDisplay);
                PersistentUserDataStore.getInstance(SettingList.class).saveSetting(setting);
            }
        });

        bsv.spinner.setSelection(setting.getChosenOptionNameIndex());

        prefText.setPadding(0,0,0, 50);

        L_Row.addView(T);
        L_Row.addView(bsv);

        this.addView(L_Row);
        this.addView(prefText);
    }
}
