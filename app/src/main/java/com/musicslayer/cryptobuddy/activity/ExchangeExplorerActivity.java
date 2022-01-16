package com.musicslayer.cryptobuddy.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;

import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.api.address.AddressData;
import com.musicslayer.cryptobuddy.api.address.CryptoAddress;
import com.musicslayer.cryptobuddy.crash.CrashDialogInterface;
import com.musicslayer.cryptobuddy.crash.CrashView;
import com.musicslayer.cryptobuddy.dialog.AddTransactionDialog;
import com.musicslayer.cryptobuddy.dialog.BaseDialogFragment;
import com.musicslayer.cryptobuddy.dialog.ConfirmBackDialog;
import com.musicslayer.cryptobuddy.dialog.ConfirmResetTableDialog;
import com.musicslayer.cryptobuddy.dialog.CryptoConverterDialog;
import com.musicslayer.cryptobuddy.dialog.CryptoPricesDialog;
import com.musicslayer.cryptobuddy.dialog.DownloadDataDialog;
import com.musicslayer.cryptobuddy.dialog.DownloadExchangeDataDialog;
import com.musicslayer.cryptobuddy.dialog.ProgressDialog;
import com.musicslayer.cryptobuddy.dialog.ProgressDialogFragment;
import com.musicslayer.cryptobuddy.dialog.ReportFeedbackDialog;
import com.musicslayer.cryptobuddy.dialog.TotalDialog;
import com.musicslayer.cryptobuddy.persistence.TokenManagerList;
import com.musicslayer.cryptobuddy.serialize.Serialization;
import com.musicslayer.cryptobuddy.state.ActivityStateObj;
import com.musicslayer.cryptobuddy.state.TableStateObj;
import com.musicslayer.cryptobuddy.util.HashMapUtil;
import com.musicslayer.cryptobuddy.util.HelpUtil;
import com.musicslayer.cryptobuddy.util.ToastUtil;
import com.musicslayer.cryptobuddy.view.table.ExchangeTable;
import com.musicslayer.cryptobuddy.view.table.TransactionTable;

import java.util.ArrayList;

public class ExchangeExplorerActivity extends BaseActivity {
    public BaseDialogFragment confirmBackDialogFragment;

    ExchangeTable table;
    public final static ActivityStateObj[] activityStateObj = new ActivityStateObj[1];

    public ArrayList<Boolean> includeBalances;
    public ArrayList<Boolean> includeTransactions;

    public int getAdLayoutViewID() {
        return R.id.exchange_explorer_adLayout;
    }

    @Override
    public void onBackPressedImpl() {
        confirmBackDialogFragment.show(ExchangeExplorerActivity.this, "back");
    }

    public void createLayout () {
        setContentView(R.layout.activity_exchange_explorer);

        if(isFirstCreate) {
            activityStateObj[0] = new ActivityStateObj();
        }

        confirmBackDialogFragment = BaseDialogFragment.newInstance(ConfirmBackDialog.class);
        confirmBackDialogFragment.setOnDismissListener(new CrashDialogInterface.CrashOnDismissListener(this) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                if(((ConfirmBackDialog)dialog).isComplete) {
                    // Clean State Objects.
                    activityStateObj[0] = null;
                    table.tableStateObj[0] = null;

                    startActivity(new Intent(ExchangeExplorerActivity.this, MainActivity.class));
                    finish();
                }
            }
        });
        confirmBackDialogFragment.restoreListeners(this, "back");

        String exchange = getIntent().getStringExtra("Exchange");
        String token = getIntent().getStringExtra("Token");

        Toolbar toolbar = findViewById(R.id.exchange_explorer_toolbar);
        setSupportActionBar(toolbar);

        ImageButton helpButton = findViewById(R.id.exchange_explorer_helpButton);
        helpButton.setOnClickListener(new CrashView.CrashOnClickListener(this) {
            @Override
            public void onClickImpl(View view) {
                HelpUtil.showHelp(ExchangeExplorerActivity.this, R.raw.help_exchange_explorer);
            }
        });

        table = findViewById(R.id.exchange_explorer_table);
        table.pageView = findViewById(R.id.exchange_explorer_tablePageView);
        table.pageView.setTable(table);
        table.pageView.updateLayout();
        if(isFirstCreate) {
            table.tableStateObj[0] = new TableStateObj();
            table.tableStateObj[0].table = table;
        }

/*
        ProgressDialogFragment progressDialogFragment = ProgressDialogFragment.newInstance(ProgressDialog.class);
        progressDialogFragment.setOnShowListener(new CrashDialogInterface.CrashOnShowListener(this) {
            @Override
            public void onShowImpl(DialogInterface dialog) {
                AddressData newAddressData;

                if(includeBalances.get(0) && includeTransactions.get(0)) {
                    newAddressData = AddressData.getAllData(cryptoAddressArrayList.get(0));
                }
                else if(includeBalances.get(0)) {
                    newAddressData = AddressData.getCurrentBalanceData(cryptoAddressArrayList.get(0));
                }
                else if(includeTransactions.get(0)) {
                    newAddressData = AddressData.getTransactionsData(cryptoAddressArrayList.get(0));
                }
                else {
                    newAddressData = AddressData.getNoData(cryptoAddressArrayList.get(0));
                }

                // Save found tokens, potentially from multiple TokenManagers.
                TokenManagerList.saveAllData(AddressExplorerActivity.this);

                ProgressDialogFragment.setValue(Serialization.serialize(newAddressData));
            }
        });
        progressDialogFragment.setOnDismissListener(new CrashDialogInterface.CrashOnDismissListener(this) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                AddressData newAddressData = Serialization.deserialize(ProgressDialogFragment.getValue(), AddressData.class);

                boolean isComplete;
                if(includeBalances.get(0) && includeTransactions.get(0)) {
                    isComplete = newAddressData.isComplete();
                }
                else if(includeBalances.get(0)) {
                    isComplete = newAddressData.isCurrentBalanceComplete();
                }
                else if(includeTransactions.get(0)) {
                    isComplete = newAddressData.isTransactionsComplete();
                }
                else {
                    isComplete = true;
                }

                if(!isComplete) {
                    ToastUtil.showToast(AddressExplorerActivity.this,"incomplete_address_data");
                }

                CryptoAddress cryptoAddress = cryptoAddressArrayList.get(0);
                AddressData oldAddressData = HashMapUtil.getValueFromMap(activityStateObj[0].addressDataMap, cryptoAddress);
                AddressData mergedAddressData = AddressData.merge(oldAddressData, newAddressData);
                HashMapUtil.putValueInMap(activityStateObj[0].addressDataMap, cryptoAddress, mergedAddressData);

                updateLayout();
                ToastUtil.showToast(AddressExplorerActivity.this,"address_data_downloaded");
            }
        });
        progressDialogFragment.restoreListeners(this, "progress");

 */

        BaseDialogFragment downloadDialogFragment = BaseDialogFragment.newInstance(DownloadExchangeDataDialog.class);
        downloadDialogFragment.setOnDismissListener(new CrashDialogInterface.CrashOnDismissListener(this) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                if(((DownloadDataDialog)dialog).isComplete) {
                    includeBalances = ((DownloadDataDialog)dialog).user_BALANCES;
                    includeTransactions = ((DownloadDataDialog)dialog).user_TRANSACTIONS;
                    //progressDialogFragment.show(ExchangeExplorerActivity.this, "progress");
                }
            }
        });
        downloadDialogFragment.restoreListeners(this, "download");

        AppCompatButton downloadDataButton = findViewById(R.id.exchange_explorer_downloadDataButton);
        downloadDataButton.setOnClickListener(new CrashView.CrashOnClickListener(this) {
            @Override
            public void onClickImpl(View view) {
                downloadDialogFragment.show(ExchangeExplorerActivity.this, "download");
            }
        });
    }

    public void updateLayout() {
        table.resetTable();
        table.addRowsFromExchangeDataArray(new ArrayList<>(activityStateObj[0].exchangeDataMap.values()));
    }

    @Override
    public boolean onCreateOptionsMenuImpl(Menu menu) {
        menu.add(0, 1, 100, "Prices");
        menu.add(0, 2, 200, "Converter");
        menu.add(0, 3, 300, "Report Feedback");
        return true;
    }

    @Override
    public boolean onOptionsItemSelectedImpl(MenuItem item) {
        int id = item.getItemId();

        if (id == 1) {
            BaseDialogFragment.newInstance(CryptoPricesDialog.class).show(ExchangeExplorerActivity.this, "price");
            return true;
        }
        else if (id == 2) {
            BaseDialogFragment.newInstance(CryptoConverterDialog.class).show(ExchangeExplorerActivity.this, "converter");
            return true;
        }
        else if (id == 3) {
            String type = "Exchange";
            BaseDialogFragment.newInstance(ReportFeedbackDialog.class, type).show(ExchangeExplorerActivity.this, "feedback");
            return true;
        }

        return false;
    }
}