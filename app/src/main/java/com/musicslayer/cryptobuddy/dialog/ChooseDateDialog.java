package com.musicslayer.cryptobuddy.dialog;

import android.app.Activity;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;

import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.crash.CrashOnClickListener;
import com.musicslayer.cryptobuddy.util.DateTime;

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

    public void createLayout() {
        setContentView(R.layout.dialog_choose_date);

        DatePicker datePicker = findViewById(R.id.choose_date_dialog_datePicker);
        datePicker.setMinDate(DateTime.getMinDateTime().getTime());
        datePicker.setMaxDate(DateTime.getMaxDateTime().getTime());

        Button B_CONFIRM = findViewById(R.id.choose_date_dialog_confirmButton);
        B_CONFIRM.setOnClickListener(new CrashOnClickListener(this.activity) {
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
