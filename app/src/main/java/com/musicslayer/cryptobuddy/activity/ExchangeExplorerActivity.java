package com.musicslayer.cryptobuddy.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.api.exchange.ExchangeAPI;
import com.musicslayer.cryptobuddy.api.exchange.ExchangeData;
import com.musicslayer.cryptobuddy.asset.exchange.Exchange;
import com.musicslayer.cryptobuddy.crash.CrashDialogInterface;
import com.musicslayer.cryptobuddy.crash.CrashView;
import com.musicslayer.cryptobuddy.dialog.AuthorizeExchangeDialog;
import com.musicslayer.cryptobuddy.dialog.BaseDialogFragment;
import com.musicslayer.cryptobuddy.dialog.ConfirmBackDialog;
import com.musicslayer.cryptobuddy.dialog.CryptoConverterDialog;
import com.musicslayer.cryptobuddy.dialog.CryptoPricesDialog;
import com.musicslayer.cryptobuddy.dialog.DownloadExchangeDataDialog;
import com.musicslayer.cryptobuddy.dialog.ExchangeInfoDialog;
import com.musicslayer.cryptobuddy.dialog.ProgressDialog;
import com.musicslayer.cryptobuddy.dialog.ProgressDialogFragment;
import com.musicslayer.cryptobuddy.dialog.ReportFeedbackDialog;
import com.musicslayer.cryptobuddy.dialog.TotalDialog;
import com.musicslayer.cryptobuddy.persistence.TokenManagerList;
import com.musicslayer.cryptobuddy.serialize.Serialization;
import com.musicslayer.cryptobuddy.state.StateObj;
import com.musicslayer.cryptobuddy.util.HashMapUtil;
import com.musicslayer.cryptobuddy.util.HelpUtil;
import com.musicslayer.cryptobuddy.util.InfoUtil;
import com.musicslayer.cryptobuddy.util.ToastUtil;
import com.musicslayer.cryptobuddy.view.table.ExchangeTable;

import java.util.ArrayList;

public class ExchangeExplorerActivity extends BaseActivity {
    public BaseDialogFragment confirmBackDialogFragment;

    ExchangeTable table;

    ArrayList<Exchange> exchangeArrayList = new ArrayList<>();

    public ArrayList<Boolean> includeBalances;
    public ArrayList<Boolean> includeTransactions;

    public int getAdLayoutViewID() {
        return R.id.exchange_explorer_adLayout;
    }

    @Override
    public void onBackPressedImpl() {
        confirmBackDialogFragment.show(ExchangeExplorerActivity.this, "back");
    }

    public void createLayout(Bundle savedInstanceState) {
        setContentView(R.layout.activity_exchange_explorer);

        confirmBackDialogFragment = BaseDialogFragment.newInstance(ConfirmBackDialog.class);
        confirmBackDialogFragment.setOnDismissListener(new CrashDialogInterface.CrashOnDismissListener(this) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                if(((ConfirmBackDialog)dialog).isComplete) {
                    startActivity(new Intent(ExchangeExplorerActivity.this, MainActivity.class));
                    finish();
                }
            }
        });
        confirmBackDialogFragment.restoreListeners(this, "back");

        Exchange exchange = getIntent().getParcelableExtra("Exchange");
        exchangeArrayList.add(exchange);
        if(savedInstanceState == null) {
            HashMapUtil.putValueInMap(StateObj.exchangeDataMap, exchange, ExchangeData.getNoData(exchange,null));
        }

        TextView T_INFO = findViewById(R.id.exchange_explorer_infoTextView);
        T_INFO.setText("Exchange = " + exchangeArrayList.get(0).toString());

        Toolbar toolbar = findViewById(R.id.exchange_explorer_toolbar);
        setSupportActionBar(toolbar);

        ImageButton problemInfoButton = findViewById(R.id.exchange_explorer_problemInfoButton);
        problemInfoButton.setOnClickListener(new CrashView.CrashOnClickListener(this) {
            @Override
            public void onClickImpl(View view) {
                InfoUtil.showInfo_Exchange(ExchangeExplorerActivity.this, exchangeArrayList);
            }
        });

        if(!InfoUtil.hasInfo_Exchange(exchangeArrayList)) {
            problemInfoButton.setVisibility(View.GONE);
        }

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

        FloatingActionButton fab_info = findViewById(R.id.exchange_explorer_infoButton);
        fab_info.setOnClickListener(new CrashView.CrashOnClickListener(this) {
            @Override
            public void onClickImpl(View view) {
                BaseDialogFragment.newInstance(ExchangeInfoDialog.class, exchangeArrayList).show(ExchangeExplorerActivity.this, "info");
            }
        });

        FloatingActionButton fab_total = findViewById(R.id.exchange_explorer_totalButton);
        fab_total.setOnClickListener(new CrashView.CrashOnClickListener(this) {
            @Override
            public void onClickImpl(View view) {
                StateObj.filteredMaskedTransactionArrayList = table.getFilteredMaskedTransactionArrayList();
                BaseDialogFragment.newInstance(TotalDialog.class).show(ExchangeExplorerActivity.this, "total");
            }
        });

        FloatingActionButton fab_authorize = findViewById(R.id.exchange_explorer_authorizeButton);
        fab_authorize.setOnClickListener(new CrashView.CrashOnClickListener(this) {
            @Override
            public void onClickImpl(View view) {
                BaseDialogFragment.newInstance(AuthorizeExchangeDialog.class, exchangeArrayList).show(ExchangeExplorerActivity.this, "authorize_exchange");
            }
        });

        ProgressDialogFragment progressDialogFragment = ProgressDialogFragment.newInstance(ProgressDialog.class);
        progressDialogFragment.setOnShowListener(new CrashDialogInterface.CrashOnShowListener(this) {
            @Override
            public void onShowImpl(DialogInterface dialog) {
                Exchange exchange = exchangeArrayList.get(0);
                ExchangeAPI exchangeAPI = HashMapUtil.getValueFromMap(StateObj.exchangeAPIMap, exchange);

                ExchangeData newExchangeData;

                if(includeBalances.get(0) && includeTransactions.get(0)) {
                    newExchangeData = ExchangeData.getAllData(exchange, exchangeAPI);
                }
                else if(includeBalances.get(0)) {
                    newExchangeData = ExchangeData.getCurrentBalanceData(exchange, exchangeAPI);
                }
                else if(includeTransactions.get(0)) {
                    newExchangeData = ExchangeData.getTransactionsData(exchange, exchangeAPI);
                }
                else {
                    newExchangeData = ExchangeData.getNoData(exchange, exchangeAPI);
                }

                // Save found tokens, potentially from multiple TokenManagers.
                TokenManagerList.saveAllData(ExchangeExplorerActivity.this);

                ProgressDialogFragment.setValue(Serialization.serialize(newExchangeData));
            }
        });
        progressDialogFragment.setOnDismissListener(new CrashDialogInterface.CrashOnDismissListener(this) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                ExchangeData newExchangeData = Serialization.deserialize(ProgressDialogFragment.getValue(), ExchangeData.class);

                boolean isComplete;
                if(includeBalances.get(0) && includeTransactions.get(0)) {
                    isComplete = newExchangeData.isComplete();
                }
                else if(includeBalances.get(0)) {
                    isComplete = newExchangeData.isCurrentBalanceComplete();
                }
                else if(includeTransactions.get(0)) {
                    isComplete = newExchangeData.isTransactionsComplete();
                }
                else {
                    isComplete = true;
                }

                if(!isComplete) {
                    ToastUtil.showToast(ExchangeExplorerActivity.this,"incomplete_exchange_data");
                }

                Exchange exchange = exchangeArrayList.get(0);
                ExchangeData oldExchangeData = HashMapUtil.getValueFromMap(StateObj.exchangeDataMap, exchange);
                ExchangeData mergedExchangeData = ExchangeData.merge(oldExchangeData, newExchangeData);
                HashMapUtil.putValueInMap(StateObj.exchangeDataMap, exchange, mergedExchangeData);

                updateLayout();
                ToastUtil.showToast(ExchangeExplorerActivity.this,"exchange_downloaded");
            }
        });
        progressDialogFragment.restoreListeners(this, "progress");

        BaseDialogFragment downloadDialogFragment = BaseDialogFragment.newInstance(DownloadExchangeDataDialog.class, exchangeArrayList);
        downloadDialogFragment.setOnDismissListener(new CrashDialogInterface.CrashOnDismissListener(this) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                if(((DownloadExchangeDataDialog)dialog).isComplete) {
                    includeBalances = ((DownloadExchangeDataDialog)dialog).user_BALANCES;
                    includeTransactions = ((DownloadExchangeDataDialog)dialog).user_TRANSACTIONS;
                    progressDialogFragment.show(ExchangeExplorerActivity.this, "progress");
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
        table.addRowsFromExchangeDataArray(new ArrayList<>(StateObj.exchangeDataMap.values()));
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
            StateObj.tableInfo = table.getInfo();
            BaseDialogFragment.newInstance(ReportFeedbackDialog.class, type).show(ExchangeExplorerActivity.this, "feedback");
            return true;
        }

        return false;
    }
}