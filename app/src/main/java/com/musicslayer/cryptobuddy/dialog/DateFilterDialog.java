package com.musicslayer.cryptobuddy.dialog;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.crash.CrashOnClickListener;
import com.musicslayer.cryptobuddy.crash.CrashOnDismissListener;
import com.musicslayer.cryptobuddy.filter.DateFilter;
import com.musicslayer.cryptobuddy.util.DateTime;

import java.util.Calendar;

public class DateFilterDialog extends BaseDialog {
    public DateFilter filter;

    int[] LASTCUSTOMDATE_START_INFO;
    int[] LASTCUSTOMDATE_END_INFO;

    public DateFilterDialog(Activity activity, DateFilter filter) {
        super(activity);
        this.filter = filter;

        Calendar calendar = Calendar.getInstance();

        LASTCUSTOMDATE_START_INFO = new int[]{calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH),calendar.get(Calendar.DAY_OF_MONTH),0,0,0};
        LASTCUSTOMDATE_END_INFO = new int[]{calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH),calendar.get(Calendar.DAY_OF_MONTH),23,59,59};
    }

    public int getBaseViewID() {
        return R.id.date_filter_dialog;
    }

    public void createLayout() {
        setContentView(R.layout.dialog_date_filter);

        Button B_FILTER = findViewById(R.id.date_filter_dialog_applyFilterButton);
        B_FILTER.setOnClickListener(new CrashOnClickListener(this.activity) {
            public void onClickImpl(View v) {
                isComplete = true;
                dismiss();
            }
        });

        Button B_CLEAR = findViewById(R.id.date_filter_dialog_clearDatesButton);
        B_CLEAR.setOnClickListener(new CrashOnClickListener(this.activity) {
            public void onClickImpl(View v) {
                filter.user_startDate = null;
                filter.user_endDate = null;
                updateLayout();
            }
        });

        BaseDialogFragment chooseStartDateDialogFragment = BaseDialogFragment.newInstance(ChooseDateDialog.class);
        chooseStartDateDialogFragment.setOnDismissListener(new CrashOnDismissListener(this.activity) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                if(((ChooseDateDialog)dialog).isComplete) {
                    int year  = ((ChooseDateDialog) dialog).user_YEAR;
                    int month = ((ChooseDateDialog) dialog).user_MONTH;
                    int day = ((ChooseDateDialog) dialog).user_DAY;

                    LASTCUSTOMDATE_START_INFO[0] = year;
                    LASTCUSTOMDATE_START_INFO[1] = month;
                    LASTCUSTOMDATE_START_INFO[2] = day;

                    filter.user_startDate = DateTime.getDateTime(LASTCUSTOMDATE_START_INFO[0], LASTCUSTOMDATE_START_INFO[1], LASTCUSTOMDATE_START_INFO[2], LASTCUSTOMDATE_START_INFO[3], LASTCUSTOMDATE_START_INFO[4], LASTCUSTOMDATE_START_INFO[5]);
                    updateLayout();
                }
            }
        });
        chooseStartDateDialogFragment.restoreListeners(this.activity, "start_date");

        Button B_START_DATE = findViewById(R.id.date_filter_dialog_startDateButton);
        B_START_DATE.setOnClickListener(new CrashOnClickListener(this.activity) {
            public void onClickImpl(View v) {
                chooseStartDateDialogFragment.show(DateFilterDialog.this.activity, "start_date");
            }
        });

        BaseDialogFragment chooseStartTimeDialogFragment = BaseDialogFragment.newInstance(ChooseTimeDialog.class);
        chooseStartTimeDialogFragment.setOnDismissListener(new CrashOnDismissListener(this.activity) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                if(((ChooseTimeDialog)dialog).isComplete) {
                    int hour = ((ChooseTimeDialog) dialog).user_HOUR;
                    int minute = ((ChooseTimeDialog) dialog).user_MINUTE;
                    int second  = ((ChooseTimeDialog) dialog).user_SECOND;

                    LASTCUSTOMDATE_START_INFO[3] = hour;
                    LASTCUSTOMDATE_START_INFO[4] = minute;
                    LASTCUSTOMDATE_START_INFO[5] = second;

                    filter.user_startDate = DateTime.getDateTime(LASTCUSTOMDATE_START_INFO[0], LASTCUSTOMDATE_START_INFO[1], LASTCUSTOMDATE_START_INFO[2], LASTCUSTOMDATE_START_INFO[3], LASTCUSTOMDATE_START_INFO[4], LASTCUSTOMDATE_START_INFO[5]);
                    updateLayout();
                }
            }
        });
        chooseStartTimeDialogFragment.restoreListeners(this.activity, "start_time");

        Button B_START_TIME = findViewById(R.id.date_filter_dialog_startTimeButton);
        B_START_TIME.setOnClickListener(new CrashOnClickListener(this.activity) {
            public void onClickImpl(View v) {
                chooseStartTimeDialogFragment.show(DateFilterDialog.this.activity, "start_time");
            }
        });

        BaseDialogFragment chooseEndDateDialogFragment = BaseDialogFragment.newInstance(ChooseDateDialog.class);
        chooseEndDateDialogFragment.setOnDismissListener(new CrashOnDismissListener(this.activity) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                if(((ChooseDateDialog)dialog).isComplete) {
                    int year  = ((ChooseDateDialog) dialog).user_YEAR;
                    int month = ((ChooseDateDialog) dialog).user_MONTH;
                    int day = ((ChooseDateDialog) dialog).user_DAY;

                    LASTCUSTOMDATE_END_INFO[0] = year;
                    LASTCUSTOMDATE_END_INFO[1] = month;
                    LASTCUSTOMDATE_END_INFO[2] = day;

                    filter.user_endDate = DateTime.getDateTime(LASTCUSTOMDATE_END_INFO[0], LASTCUSTOMDATE_END_INFO[1], LASTCUSTOMDATE_END_INFO[2], LASTCUSTOMDATE_END_INFO[3], LASTCUSTOMDATE_END_INFO[4], LASTCUSTOMDATE_END_INFO[5]);
                    updateLayout();
                }
            }
        });
        chooseEndDateDialogFragment.restoreListeners(this.activity, "end_date");

        Button B_END_DATE = findViewById(R.id.date_filter_dialog_endDateButton);
        B_END_DATE.setOnClickListener(new CrashOnClickListener(this.activity) {
            public void onClickImpl(View v) {
                chooseEndDateDialogFragment.show(DateFilterDialog.this.activity, "end_date");
            }
        });

        BaseDialogFragment chooseEndTimeDialogFragment = BaseDialogFragment.newInstance(ChooseTimeDialog.class);
        chooseEndTimeDialogFragment.setOnDismissListener(new CrashOnDismissListener(this.activity) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                if(((ChooseTimeDialog)dialog).isComplete) {
                    int hour = ((ChooseTimeDialog) dialog).user_HOUR;
                    int minute = ((ChooseTimeDialog) dialog).user_MINUTE;
                    int second  = ((ChooseTimeDialog) dialog).user_SECOND;

                    LASTCUSTOMDATE_END_INFO[3] = hour;
                    LASTCUSTOMDATE_END_INFO[4] = minute;
                    LASTCUSTOMDATE_END_INFO[5] = second;

                    filter.user_endDate = DateTime.getDateTime(LASTCUSTOMDATE_END_INFO[0], LASTCUSTOMDATE_END_INFO[1], LASTCUSTOMDATE_END_INFO[2], LASTCUSTOMDATE_END_INFO[3], LASTCUSTOMDATE_END_INFO[4], LASTCUSTOMDATE_END_INFO[5]);
                    updateLayout();
                }
            }
        });
        chooseEndTimeDialogFragment.restoreListeners(this.activity, "end_time");

        Button B_END_TIME = findViewById(R.id.date_filter_dialog_endTimeButton);
        B_END_TIME.setOnClickListener(new CrashOnClickListener(this.activity) {
            public void onClickImpl(View v) {
                chooseEndTimeDialogFragment.show(DateFilterDialog.this.activity, "end_time");
            }
        });

        updateLayout();
    }

    public void updateLayout() {
        TextView T_START = findViewById(R.id.date_filter_dialog_startDateTextView);

        if(filter.user_startDate == null) {
            T_START.setText("No Lower Limit");
        }
        else {
            T_START.setText(DateTime.toDateString(filter.user_startDate));
        }

        TextView T_END = findViewById(R.id.date_filter_dialog_endDateTextView);

        if(filter.user_endDate == null) {
            T_END.setText("No Upper Limit");
        }
        else {
            T_END.setText(DateTime.toDateString(filter.user_endDate));
        }
    }

    @Override
    public Bundle onSaveInstanceStateImpl(Bundle bundle) {
        bundle.putIntArray("lastcustomdate_start_info", LASTCUSTOMDATE_START_INFO);
        bundle.putIntArray("lastcustomdate_end_info", LASTCUSTOMDATE_END_INFO);
        return bundle;
    }

    @Override
    public void onRestoreInstanceStateImpl(Bundle bundle) {
        if(bundle != null) {
            LASTCUSTOMDATE_START_INFO = bundle.getIntArray("lastcustomdate_start_info");
            LASTCUSTOMDATE_END_INFO = bundle.getIntArray("lastcustomdate_end_info");
        }
    }
}
