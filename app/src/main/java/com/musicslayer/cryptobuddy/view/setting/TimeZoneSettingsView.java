package com.musicslayer.cryptobuddy.view.setting;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.musicslayer.cryptobuddy.crash.CrashLinearLayout;
import com.musicslayer.cryptobuddy.crash.CrashOnItemSelectedListener;
import com.musicslayer.cryptobuddy.persistence.Settings;
import com.musicslayer.cryptobuddy.util.TimeZoneManager;
import com.musicslayer.cryptobuddy.view.BorderedSpinnerView;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

public class TimeZoneSettingsView extends CrashLinearLayout {
    public TimeZoneSettingsView(Context context) {
        super(context);
    }

    public TimeZoneSettingsView(Context context, String settingName, String settingDisplayName, String[] settingOptions, String[] settingDescriptions) {
        super(context);

        // One options will be passed in, but we must dynamically add the rest.
        ArrayList<String> settingOptionsArrayList = new ArrayList<>();
        Collections.addAll(settingOptionsArrayList, settingOptions);

        ArrayList<String> settingDescriptionsArrayList = new ArrayList<>();
        Collections.addAll(settingDescriptionsArrayList, settingDescriptions);

        // Offsets may change based on the date, so let's just use "now" whenever the user is looking.
        // This only affects the display, not the actual properties of the chosen time zone.
        Instant nowInstant = new Date().toInstant();

        for(String zoneIdString : TimeZoneManager.getAvailableTimeZoneStrings()) {
            settingOptionsArrayList.add(zoneIdString);
            settingDescriptionsArrayList.add("Use " + zoneIdString + " time zone.");
        }

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
        bsv.setOptions(settingOptionsArrayList);
        bsv.setMargins(10, 0, 10, 0);

        final TextView prefText=new TextView(context);
        prefText.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        prefText.setText(settingDescriptionsArrayList.get(Settings.getSettingValue(settingName)));


        bsv.spinner.setOnItemSelectedListener(new CrashOnItemSelectedListener(context) {
            public void onNothingSelectedImpl(AdapterView<?> parent){}
            public void onItemSelectedImpl(AdapterView<?> parent, View view, int pos, long id) {
                // The first option is always the same, but others may change.
                // Just use the zoneID String.
                prefText.setText(settingDescriptionsArrayList.get(pos));
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
