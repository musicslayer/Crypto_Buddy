package com.musicslayer.cryptobuddy.dialog;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;

import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.crash.CrashView;
import com.musicslayer.cryptobuddy.util.DateTimeUtil;

public class ChooseDateDialog extends BaseDialog {
    public int user_DAY;
    public int user_MONTH;
    public int user_YEAR;

    public ChooseDateDialog(Activity activity) {
        super(activity);
    }

    public int getBaseViewID() {
        return R.id.choose_date_dialog;
    }

    public void createLayout(Bundle savedInstanceState) {
        setContentView(R.layout.dialog_choose_date);

        DatePicker datePicker = findViewById(R.id.choose_date_dialog_datePicker);
        datePicker.setMinDate(DateTimeUtil.getMinDateTime().getTime());
        datePicker.setMaxDate(DateTimeUtil.getMaxDateTime().getTime());

        Button B_CONFIRM = findViewById(R.id.choose_date_dialog_confirmButton);
        B_CONFIRM.setOnClickListener(new CrashView.CrashOnClickListener(this.activity) {
            public void onClickImpl(View v) {
                isComplete = true;
                user_YEAR =  datePicker.getYear();
                user_MONTH = datePicker.getMonth();
                user_DAY = datePicker.getDayOfMonth();

                dismiss();
            }
        });
    }
}
