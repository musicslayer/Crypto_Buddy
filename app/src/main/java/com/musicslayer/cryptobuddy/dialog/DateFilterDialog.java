package com.musicslayer.cryptobuddy.dialog;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.crash.CrashDialogInterface;
import com.musicslayer.cryptobuddy.crash.CrashView;
import com.musicslayer.cryptobuddy.filter.DateFilter;
import com.musicslayer.cryptobuddy.util.DateTimeUtil;

import java.util.Calendar;

public class DateFilterDialog extends BaseDialog {
    public DateFilter filter;

    int[] LASTCUSTOMDATE_START_INFO;
    int[] LASTCUSTOMDATE_END_INFO;

    boolean isStartDate;
    boolean isStartTime;
    boolean isEndDate;
    boolean isEndTime;

    public DateFilterDialog(Activity activity, DateFilter filter) {
        super(activity);
        this.filter = filter;

        Calendar calendarStart = Calendar.getInstance();
        Calendar calendarEnd = Calendar.getInstance();

        // Use the filter's data if it was previously used, otherwise fill in defaults.
        if(filter.user_startDate == null) {
            isStartDate = false;
            isStartTime = false;

            calendarStart.set(Calendar.HOUR_OF_DAY, 0);
            calendarStart.set(Calendar.MINUTE, 0);
            calendarStart.set(Calendar.SECOND, 0);
        }
        else {
            isStartDate = true;
            isStartTime = true;

            calendarStart.setTime(filter.user_startDate);
        }

        if(filter.user_endDate == null) {
            isEndDate = false;
            isEndTime = false;

            calendarEnd.set(Calendar.HOUR_OF_DAY, 23);
            calendarEnd.set(Calendar.MINUTE, 59);
            calendarEnd.set(Calendar.SECOND, 59);
        }
        else {
            isEndDate = true;
            isEndTime = true;

            calendarEnd.setTime(filter.user_endDate);
        }

        LASTCUSTOMDATE_START_INFO = new int[]{calendarStart.get(Calendar.YEAR),calendarStart.get(Calendar.MONTH),calendarStart.get(Calendar.DAY_OF_MONTH),calendarStart.get(Calendar.HOUR_OF_DAY),calendarStart.get(Calendar.MINUTE),calendarStart.get(Calendar.SECOND)};
        LASTCUSTOMDATE_END_INFO = new int[]{calendarEnd.get(Calendar.YEAR),calendarEnd.get(Calendar.MONTH),calendarEnd.get(Calendar.DAY_OF_MONTH),calendarEnd.get(Calendar.HOUR_OF_DAY),calendarEnd.get(Calendar.MINUTE),calendarEnd.get(Calendar.SECOND)};
    }

    public int getBaseViewID() {
        return R.id.date_filter_dialog;
    }

    public void createLayout() {
        setContentView(R.layout.dialog_date_filter);

        Button B_FILTER = findViewById(R.id.date_filter_dialog_applyFilterButton);
        B_FILTER.setOnClickListener(new CrashView.CrashOnClickListener(this.activity) {
            public void onClickImpl(View v) {
                if(isStartDate || isStartTime) {
                    filter.user_startDate = DateTimeUtil.getDateTime(LASTCUSTOMDATE_START_INFO[0], LASTCUSTOMDATE_START_INFO[1], LASTCUSTOMDATE_START_INFO[2], LASTCUSTOMDATE_START_INFO[3], LASTCUSTOMDATE_START_INFO[4], LASTCUSTOMDATE_START_INFO[5]);
                }

                if(isEndDate || isEndTime) {
                    filter.user_endDate = DateTimeUtil.getDateTime(LASTCUSTOMDATE_END_INFO[0], LASTCUSTOMDATE_END_INFO[1], LASTCUSTOMDATE_END_INFO[2], LASTCUSTOMDATE_END_INFO[3], LASTCUSTOMDATE_END_INFO[4], LASTCUSTOMDATE_END_INFO[5]);
                }

                isComplete = true;
                dismiss();
            }
        });

        Button B_CLEAR = findViewById(R.id.date_filter_dialog_clearDatesButton);
        B_CLEAR.setOnClickListener(new CrashView.CrashOnClickListener(this.activity) {
            public void onClickImpl(View v) {
                filter.user_startDate = null;
                filter.user_endDate = null;

                isStartDate = false;
                isStartTime = false;
                isEndDate = false;
                isEndTime = false;

                updateLayout();
            }
        });

        BaseDialogFragment chooseStartDateDialogFragment = BaseDialogFragment.newInstance(ChooseDateDialog.class);
        chooseStartDateDialogFragment.setOnDismissListener(new CrashDialogInterface.CrashOnDismissListener(this.activity) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                if(((ChooseDateDialog)dialog).isComplete) {
                    isStartDate = true;

                    int year  = ((ChooseDateDialog) dialog).user_YEAR;
                    int month = ((ChooseDateDialog) dialog).user_MONTH;
                    int day = ((ChooseDateDialog) dialog).user_DAY;

                    LASTCUSTOMDATE_START_INFO[0] = year;
                    LASTCUSTOMDATE_START_INFO[1] = month;
                    LASTCUSTOMDATE_START_INFO[2] = day;

                    updateLayout();
                }
            }
        });
        chooseStartDateDialogFragment.restoreListeners(this.activity, "start_date");

        Button B_START_DATE = findViewById(R.id.date_filter_dialog_startDateButton);
        B_START_DATE.setOnClickListener(new CrashView.CrashOnClickListener(this.activity) {
            public void onClickImpl(View v) {
                chooseStartDateDialogFragment.show(DateFilterDialog.this.activity, "start_date");
            }
        });

        BaseDialogFragment chooseStartTimeDialogFragment = BaseDialogFragment.newInstance(ChooseTimeDialog.class);
        chooseStartTimeDialogFragment.setOnDismissListener(new CrashDialogInterface.CrashOnDismissListener(this.activity) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                if(((ChooseTimeDialog)dialog).isComplete) {
                    isStartTime = true;

                    int hour = ((ChooseTimeDialog) dialog).user_HOUR;
                    int minute = ((ChooseTimeDialog) dialog).user_MINUTE;
                    int second  = ((ChooseTimeDialog) dialog).user_SECOND;

                    LASTCUSTOMDATE_START_INFO[3] = hour;
                    LASTCUSTOMDATE_START_INFO[4] = minute;
                    LASTCUSTOMDATE_START_INFO[5] = second;

                    updateLayout();
                }
            }
        });
        chooseStartTimeDialogFragment.restoreListeners(this.activity, "start_time");

        Button B_START_TIME = findViewById(R.id.date_filter_dialog_startTimeButton);
        B_START_TIME.setOnClickListener(new CrashView.CrashOnClickListener(this.activity) {
            public void onClickImpl(View v) {
                chooseStartTimeDialogFragment.show(DateFilterDialog.this.activity, "start_time");
            }
        });

        BaseDialogFragment chooseEndDateDialogFragment = BaseDialogFragment.newInstance(ChooseDateDialog.class);
        chooseEndDateDialogFragment.setOnDismissListener(new CrashDialogInterface.CrashOnDismissListener(this.activity) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                if(((ChooseDateDialog)dialog).isComplete) {
                    isEndDate = true;

                    int year  = ((ChooseDateDialog) dialog).user_YEAR;
                    int month = ((ChooseDateDialog) dialog).user_MONTH;
                    int day = ((ChooseDateDialog) dialog).user_DAY;

                    LASTCUSTOMDATE_END_INFO[0] = year;
                    LASTCUSTOMDATE_END_INFO[1] = month;
                    LASTCUSTOMDATE_END_INFO[2] = day;

                    updateLayout();
                }
            }
        });
        chooseEndDateDialogFragment.restoreListeners(this.activity, "end_date");

        Button B_END_DATE = findViewById(R.id.date_filter_dialog_endDateButton);
        B_END_DATE.setOnClickListener(new CrashView.CrashOnClickListener(this.activity) {
            public void onClickImpl(View v) {
                chooseEndDateDialogFragment.show(DateFilterDialog.this.activity, "end_date");
            }
        });

        BaseDialogFragment chooseEndTimeDialogFragment = BaseDialogFragment.newInstance(ChooseTimeDialog.class);
        chooseEndTimeDialogFragment.setOnDismissListener(new CrashDialogInterface.CrashOnDismissListener(this.activity) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                if(((ChooseTimeDialog)dialog).isComplete) {
                    isEndTime = true;

                    int hour = ((ChooseTimeDialog) dialog).user_HOUR;
                    int minute = ((ChooseTimeDialog) dialog).user_MINUTE;
                    int second  = ((ChooseTimeDialog) dialog).user_SECOND;

                    LASTCUSTOMDATE_END_INFO[3] = hour;
                    LASTCUSTOMDATE_END_INFO[4] = minute;
                    LASTCUSTOMDATE_END_INFO[5] = second;

                    updateLayout();
                }
            }
        });
        chooseEndTimeDialogFragment.restoreListeners(this.activity, "end_time");

        Button B_END_TIME = findViewById(R.id.date_filter_dialog_endTimeButton);
        B_END_TIME.setOnClickListener(new CrashView.CrashOnClickListener(this.activity) {
            public void onClickImpl(View v) {
                chooseEndTimeDialogFragment.show(DateFilterDialog.this.activity, "end_time");
            }
        });

        updateLayout();
    }

    public void updateLayout() {
        TextView T_START = findViewById(R.id.date_filter_dialog_startDateTextView);

        if(!(isStartDate || isStartTime)) {
            T_START.setText("No Lower Limit");
        }
        else {
            T_START.setText(DateTimeUtil.toDateString(DateTimeUtil.getDateTime(LASTCUSTOMDATE_START_INFO[0], LASTCUSTOMDATE_START_INFO[1], LASTCUSTOMDATE_START_INFO[2], LASTCUSTOMDATE_START_INFO[3], LASTCUSTOMDATE_START_INFO[4], LASTCUSTOMDATE_START_INFO[5])));
        }

        TextView T_END = findViewById(R.id.date_filter_dialog_endDateTextView);

        if(!(isEndDate || isEndTime)) {
            T_END.setText("No Upper Limit");
        }
        else {
            T_END.setText(DateTimeUtil.toDateString(DateTimeUtil.getDateTime(LASTCUSTOMDATE_END_INFO[0], LASTCUSTOMDATE_END_INFO[1], LASTCUSTOMDATE_END_INFO[2], LASTCUSTOMDATE_END_INFO[3], LASTCUSTOMDATE_END_INFO[4], LASTCUSTOMDATE_END_INFO[5])));
        }
    }

    @Override
    public Bundle onSaveInstanceStateImpl(Bundle bundle) {
        bundle.putIntArray("lastcustomdate_start_info", LASTCUSTOMDATE_START_INFO);
        bundle.putIntArray("lastcustomdate_end_info", LASTCUSTOMDATE_END_INFO);

        bundle.putBoolean("isStartDate", isStartDate);
        bundle.putBoolean("isStartTime", isStartTime);
        bundle.putBoolean("isEndDate", isEndDate);
        bundle.putBoolean("isEndTime", isEndTime);
        return bundle;
    }

    @Override
    public void onRestoreInstanceStateImpl(Bundle bundle) {
        if(bundle != null) {
            LASTCUSTOMDATE_START_INFO = bundle.getIntArray("lastcustomdate_start_info");
            LASTCUSTOMDATE_END_INFO = bundle.getIntArray("lastcustomdate_end_info");

            isStartDate = bundle.getBoolean("isStartDate");
            isStartTime = bundle.getBoolean("isStartTime");
            isEndDate = bundle.getBoolean("isEndDate");
            isEndTime = bundle.getBoolean("isEndTime");
        }
    }
}