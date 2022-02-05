package com.musicslayer.cryptobuddy.dialog;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.musicslayer.cryptobuddy.asset.Asset;
import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.crash.CrashAdapterView;
import com.musicslayer.cryptobuddy.crash.CrashDialogInterface;
import com.musicslayer.cryptobuddy.crash.CrashView;
import com.musicslayer.cryptobuddy.transaction.Action;
import com.musicslayer.cryptobuddy.transaction.AssetQuantity;
import com.musicslayer.cryptobuddy.transaction.Timestamp;
import com.musicslayer.cryptobuddy.transaction.Transaction;
import com.musicslayer.cryptobuddy.util.DateTimeUtil;
import com.musicslayer.cryptobuddy.util.ToastUtil;
import com.musicslayer.cryptobuddy.view.BorderedSpinnerView;
import com.musicslayer.cryptobuddy.view.red.AnythingEditText;
import com.musicslayer.cryptobuddy.view.red.NumericEditText;
import com.musicslayer.cryptobuddy.view.SelectAndSearchView;

import java.util.Calendar;
import java.util.Date;

public class AddTransactionDialog extends BaseDialog {
    public Transaction user_TRANSACTION;

    int[] LASTCUSTOMDATE_INFO;
    String LASTCUSTOMDATE_TEXT;
    Date LASTCUSTOMDATE;
    Date CHOSENDATE;

    int LAST_CHECK = 0;

    public AddTransactionDialog(Activity activity) {
        super(activity);

        final Date now = new Date();
        final String nowString = DateTimeUtil.toDateString(now);

        // Don't mix calendar and date - there are two "now" times.
        Calendar calendar = Calendar.getInstance();

        LASTCUSTOMDATE_INFO = new int[]{
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH),
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            calendar.get(Calendar.SECOND)};

        LASTCUSTOMDATE = DateTimeUtil.getDateTime(LASTCUSTOMDATE_INFO[0], LASTCUSTOMDATE_INFO[1], LASTCUSTOMDATE_INFO[2], LASTCUSTOMDATE_INFO[3], LASTCUSTOMDATE_INFO[4], LASTCUSTOMDATE_INFO[5]);

        LASTCUSTOMDATE_TEXT = nowString;
        CHOSENDATE = now;
    }

    public int getBaseViewID() {
        return R.id.add_transaction_dialog;
    }

    public void createLayout(Bundle savedInstanceState) {
        setContentView(R.layout.dialog_add_transaction);

        final Date now = new Date();
        final String nowString = DateTimeUtil.toDateString(now);

        final TextView T_DATE_CHOICE = findViewById(R.id.add_transaction_dialog_dateTimeTextView);
        T_DATE_CHOICE.setText(nowString);

        BaseDialogFragment chooseDateDialogFragment = BaseDialogFragment.newInstance(ChooseDateDialog.class);
        chooseDateDialogFragment.setOnDismissListener(new CrashDialogInterface.CrashOnDismissListener(this.activity) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                if(((ChooseDateDialog)dialog).isComplete) {
                    int year  = ((ChooseDateDialog) dialog).user_YEAR;
                    int month = ((ChooseDateDialog) dialog).user_MONTH;
                    int day = ((ChooseDateDialog) dialog).user_DAY;


                    LASTCUSTOMDATE_INFO[0] = year;
                    LASTCUSTOMDATE_INFO[1] = month;
                    LASTCUSTOMDATE_INFO[2] = day;

                    LASTCUSTOMDATE = DateTimeUtil.getDateTime(LASTCUSTOMDATE_INFO[0], LASTCUSTOMDATE_INFO[1], LASTCUSTOMDATE_INFO[2], LASTCUSTOMDATE_INFO[3], LASTCUSTOMDATE_INFO[4], LASTCUSTOMDATE_INFO[5]);
                    CHOSENDATE = LASTCUSTOMDATE;

                    String newDateText = DateTimeUtil.toDateString(LASTCUSTOMDATE);
                    T_DATE_CHOICE.setText(newDateText);
                    LASTCUSTOMDATE_TEXT = newDateText;
                }
            }
        });
        chooseDateDialogFragment.restoreListeners(this.activity, "date");

        Button B_CHOOSEDATE = findViewById(R.id.add_transaction_dialog_chooseDateButton);
        B_CHOOSEDATE.setOnClickListener(new CrashView.CrashOnClickListener(this.activity) {
            public void onClickImpl(View v) {
                chooseDateDialogFragment.show(AddTransactionDialog.this.activity, "date");
            }
        });

        BaseDialogFragment chooseTimeDialogFragment = BaseDialogFragment.newInstance(ChooseTimeDialog.class);
        chooseTimeDialogFragment.setOnDismissListener(new CrashDialogInterface.CrashOnDismissListener(this.activity) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                if(((ChooseTimeDialog)dialog).isComplete) {
                    int hour = ((ChooseTimeDialog) dialog).user_HOUR;
                    int minute = ((ChooseTimeDialog) dialog).user_MINUTE;
                    int second  = ((ChooseTimeDialog) dialog).user_SECOND;

                    LASTCUSTOMDATE_INFO[3] = hour;
                    LASTCUSTOMDATE_INFO[4] = minute;
                    LASTCUSTOMDATE_INFO[5] = second;

                    LASTCUSTOMDATE = DateTimeUtil.getDateTime(LASTCUSTOMDATE_INFO[0], LASTCUSTOMDATE_INFO[1], LASTCUSTOMDATE_INFO[2], LASTCUSTOMDATE_INFO[3], LASTCUSTOMDATE_INFO[4], LASTCUSTOMDATE_INFO[5]);
                    CHOSENDATE = LASTCUSTOMDATE;

                    String newDateText = DateTimeUtil.toDateString(LASTCUSTOMDATE);
                    T_DATE_CHOICE.setText(newDateText);
                    LASTCUSTOMDATE_TEXT = newDateText;
                }
            }
        });
        chooseTimeDialogFragment.restoreListeners(this.activity, "time");

        Button B_CHOOSETIME = findViewById(R.id.add_transaction_dialog_chooseTimeButton);
        B_CHOOSETIME.setOnClickListener(new CrashView.CrashOnClickListener(this.activity) {
            public void onClickImpl(View v) {
                chooseTimeDialogFragment.show(AddTransactionDialog.this.activity, "time");
            }
        });

        final BorderedSpinnerView bsv_action = findViewById(R.id.add_transaction_dialog_actionSpinner);
        bsv_action.setOptions(Action.actions);

        final NumericEditText E_PRIMARYASSET = findViewById(R.id.add_transaction_dialog_primaryEditText);

        SelectAndSearchView ssvPrimary = findViewById(R.id.add_transaction_dialog_primarySelectAndSearchView);
        ssvPrimary.setIncludesFiat(true);
        ssvPrimary.setIncludesCoin(true);
        ssvPrimary.setIncludesToken(true);
        ssvPrimary.setCompleteOptions();
        ssvPrimary.chooseCoin();

        final NumericEditText E_SECONDARYASSET = findViewById(R.id.add_transaction_dialog_secondaryEditText);

        SelectAndSearchView ssvSecondary = findViewById(R.id.add_transaction_dialog_secondarySelectAndSearchView);
        ssvSecondary.setIncludesFiat(true);
        ssvSecondary.setIncludesCoin(true);
        ssvSecondary.setIncludesToken(true);
        ssvSecondary.setCompleteOptions();
        ssvSecondary.chooseFiat();

        final TextView T = findViewById(R.id.add_transaction_dialog_forTextView);

        final AnythingEditText E_INFO = findViewById(R.id.add_transaction_dialog_infoEditText);

        RadioGroup radioGroup = findViewById(R.id.add_transaction_dialog_radioGroup);
        RadioButton[] rb = new RadioButton[3];

        rb[0] = findViewById(R.id.add_transaction_dialog_radioButton1);
        rb[0].setOnClickListener(new CrashView.CrashOnClickListener(this.activity) {
            public void onClickImpl(View v) {
                B_CHOOSEDATE.setVisibility(View.INVISIBLE);
                B_CHOOSETIME.setVisibility(View.INVISIBLE);
                T_DATE_CHOICE.setText(nowString);
                CHOSENDATE = now;

                LAST_CHECK = 0;
            }
        });

        rb[1] = findViewById(R.id.add_transaction_dialog_radioButton2);
        rb[1].setOnClickListener(new CrashView.CrashOnClickListener(this.activity) {
            public void onClickImpl(View v) {
                B_CHOOSEDATE.setVisibility(View.VISIBLE);
                B_CHOOSETIME.setVisibility(View.VISIBLE);
                T_DATE_CHOICE.setText(LASTCUSTOMDATE_TEXT);
                CHOSENDATE = LASTCUSTOMDATE;

                LAST_CHECK = 1;
            }
        });

        rb[2] = findViewById(R.id.add_transaction_dialog_radioButton3);
        rb[2].setOnClickListener(new CrashView.CrashOnClickListener(this.activity) {
            public void onClickImpl(View v) {
                B_CHOOSEDATE.setVisibility(View.INVISIBLE);
                B_CHOOSETIME.setVisibility(View.INVISIBLE);
                T_DATE_CHOICE.setText("-");
                CHOSENDATE = null;

                LAST_CHECK = 2;
            }
        });

        radioGroup.check(rb[LAST_CHECK].getId());
        rb[LAST_CHECK].callOnClick();

        Button B_CONFIRM = findViewById(R.id.add_transaction_dialog_confirmButton);
        B_CONFIRM.setOnClickListener(new CrashView.CrashOnClickListener(this.activity) {
            public void onClickImpl(View v) {
                Action action = new Action(Action.actions.get(bsv_action.spinner.getSelectedItemPosition()));

                // Don't short circuit - we want to test everything.
                boolean isValid1 = action.numAssets() == 1 & E_PRIMARYASSET.test();
                boolean isValid2 = E_PRIMARYASSET.test() & E_SECONDARYASSET.test();

                if(ssvPrimary.getChosenAsset() == null || (action.numAssets() == 2 && ssvSecondary.getChosenAsset() == null)) {
                    ToastUtil.showToast(activity,"must_choose_assets");
                    return;
                }

                if(action.numAssets() == 2 && ssvPrimary.getChosenAsset() == ssvSecondary.getChosenAsset()) {
                    ToastUtil.showToast(activity,"assets_same");
                    return;
                }

                if(!isValid1 && !isValid2) {
                    return;
                }

                Asset primaryAsset = ssvPrimary.getChosenAsset();
                AssetQuantity primaryAssetQuantity = new AssetQuantity(E_PRIMARYASSET.getTextString(), primaryAsset);

                AssetQuantity secondaryAssetQuantity;
                if(action.numAssets() == 2) {
                    Asset secondaryAsset = ssvSecondary.getChosenAsset();
                    secondaryAssetQuantity = new AssetQuantity(E_SECONDARYASSET.getTextString(), secondaryAsset);
                }
                else {
                    secondaryAssetQuantity = null;
                }

                // Info can be anything.
                String info = E_INFO.getTextString();
                user_TRANSACTION = new Transaction(action, primaryAssetQuantity, secondaryAssetQuantity, new Timestamp(CHOSENDATE), info);

                isComplete = true;
                dismiss();
            }
        });

        FloatingActionButton fab_swap = findViewById(R.id.add_transaction_dialog_swapButton);
        fab_swap.setOnClickListener(new CrashView.CrashOnClickListener(this.activity) {
            @Override
            public void onClickImpl(View view) {
                SelectAndSearchView.swap(ssvPrimary, ssvSecondary);

                String textPrimary = E_PRIMARYASSET.getTextString();
                String textSecondary = E_SECONDARYASSET.getTextString();

                E_PRIMARYASSET.setText(textSecondary);
                E_SECONDARYASSET.setText(textPrimary);
            }
        });

        bsv_action.setOnItemSelectedListener(new CrashAdapterView.CrashOnItemSelectedListener(this.activity) {
            public void onNothingSelectedImpl(AdapterView<?> parent){}
            public void onItemSelectedImpl(AdapterView<?> parent, View view, int pos, long id) {
                if(pos == 0 || pos == 1) {
                    T.setVisibility(View.VISIBLE);
                    E_SECONDARYASSET.setVisibility(View.VISIBLE);
                    ssvSecondary.setVisibility(View.VISIBLE);
                    fab_swap.setVisibility(View.VISIBLE);
                }
                else {
                    T.setVisibility(View.GONE);
                    E_SECONDARYASSET.setVisibility(View.GONE);
                    ssvSecondary.setVisibility(View.GONE);
                    fab_swap.setVisibility(View.INVISIBLE);
                }
            }
        });
    }

    @Override
    public Bundle onSaveInstanceStateImpl(Bundle bundle) {
        bundle.putIntArray("lastcustomdate_info", LASTCUSTOMDATE_INFO);
        bundle.putString("lastcustomdate", LASTCUSTOMDATE_TEXT);
        bundle.putInt("lastcheck", LAST_CHECK);
        return bundle;
    }

    @Override
    public void onRestoreInstanceStateImpl(Bundle bundle) {
        if(bundle != null) {
            LASTCUSTOMDATE_INFO = bundle.getIntArray("lastcustomdate_info");
            LASTCUSTOMDATE = DateTimeUtil.getDateTime(LASTCUSTOMDATE_INFO[0], LASTCUSTOMDATE_INFO[1], LASTCUSTOMDATE_INFO[2], LASTCUSTOMDATE_INFO[3], LASTCUSTOMDATE_INFO[4], LASTCUSTOMDATE_INFO[5]);

            LASTCUSTOMDATE_TEXT = bundle.getString("lastcustomdate");
            LAST_CHECK = bundle.getInt("lastcheck");
        }
    }
}
