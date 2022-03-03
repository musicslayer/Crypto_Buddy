package com.musicslayer.cryptobuddy.dialog;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TimePicker;

import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.crash.CrashView;
import com.musicslayer.cryptobuddy.view.ToggleButton;

public class ChooseTimeDialog extends BaseDialog {
    public int user_HOUR;
    public int user_MINUTE;
    public int user_SECOND;

    ToggleButton B_TOGGLE;
    TimePicker timePicker;

    public ChooseTimeDialog(Activity activity) {
        super(activity);
    }

    public int getBaseViewID() {
        return R.id.choose_time_dialog;
    }

    public void createLayout(Bundle savedInstanceState) {
        setContentView(R.layout.dialog_choose_time);

        LinearLayout timePickerLinearLayout = findViewById(R.id.choose_time_dialog_timePickerLinearLayout);
        timePicker = new TimePicker(this.activity);
        timePicker.setSaveEnabled(true);
        timePicker.setSaveFromParentEnabled(true); // Must explicitly set this to the default value of "true" again.

        timePickerLinearLayout.addView(timePicker);

        Button B_CONFIRM = findViewById(R.id.choose_time_dialog_confirmButton);
        B_CONFIRM.setOnClickListener(new CrashView.CrashOnClickListener(this.activity) {
            public void onClickImpl(View v) {
                isComplete = true;

                // These methods are deprecated, but needed to support older Android versions.
                user_HOUR = timePicker.getCurrentHour();
                user_MINUTE = timePicker.getCurrentMinute();

                user_SECOND = 0; // TimePicker doesn't allow you to manually specifying this level of precision
                dismiss();
            }
        });

        B_TOGGLE = findViewById(R.id.choose_time_dialog_toggleButton);
        B_TOGGLE.setOptions("AM/PM", "24 Hour");
        B_TOGGLE.setAdditionalOnClickListener(new CrashView.CrashOnClickListener(this.activity) {
            public void onClickImpl(View v) {
                // setIs24HourView doesn't perform operations in the correct order, so we need to get/set the time ourselves
                int h = timePicker.getCurrentHour();
                int m = timePicker.getCurrentMinute();
                timePicker.setIs24HourView(B_TOGGLE.toggleState);
                timePicker.setCurrentHour(h);
                timePicker.setCurrentMinute(m);

                // We need this to deal with a drawing bug when dynamically changing a TimePicker.
                timePicker = recreate(ChooseTimeDialog.this.activity, timePicker);

                timePickerLinearLayout.removeAllViews();
                timePickerLinearLayout.addView(timePicker);
            }
        });

        timePicker.setIs24HourView(B_TOGGLE.toggleState);
    }

    public TimePicker recreate(Context context, TimePicker timePicker) {
        // Create a copy and return it.
        TimePicker newTimePicker = new TimePicker(context);
        newTimePicker.setIs24HourView(timePicker.is24HourView());
        newTimePicker.setCurrentHour(timePicker.getCurrentHour());
        newTimePicker.setCurrentMinute(timePicker.getCurrentMinute());

        return newTimePicker;
    }
}
