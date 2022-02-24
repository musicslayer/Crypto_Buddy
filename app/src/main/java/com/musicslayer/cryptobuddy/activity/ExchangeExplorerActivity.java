package com.musicslayer.cryptobuddy.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.api.exchange.CryptoExchange;
import com.musicslayer.cryptobuddy.api.exchange.ExchangeData;
import com.musicslayer.cryptobuddy.crash.CrashDialogInterface;
import com.musicslayer.cryptobuddy.crash.CrashView;
import com.musicslayer.cryptobuddy.data.bridge.DataBridge;
import com.musicslayer.cryptobuddy.data.persistent.app.PersistentAppDataStore;
import com.musicslayer.cryptobuddy.dialog.AuthorizeExchangeDialog;
import com.musicslayer.cryptobuddy.dialog.BaseDialogFragment;
import com.musicslayer.cryptobuddy.dialog.ConfirmBackDialog;
import com.musicslayer.cryptobuddy.dialog.CryptoConverterDialog;
import com.musicslayer.cryptobuddy.dialog.CryptoPricesDialog;
import com.musicslayer.cryptobuddy.dialog.DownloadExchangeDataDialog;
import com.musicslayer.cryptobuddy.dialog.ExchangeDiscrepancyDialog;
import com.musicslayer.cryptobuddy.dialog.ExchangeInfoDialog;
import com.musicslayer.cryptobuddy.dialog.ExchangeProblemDialog;
import com.musicslayer.cryptobuddy.dialog.ProgressDialog;
import com.musicslayer.cryptobuddy.dialog.ProgressDialogFragment;
import com.musicslayer.cryptobuddy.dialog.ReportFeedbackDialog;
import com.musicslayer.cryptobuddy.dialog.TotalDialog;
import com.musicslayer.cryptobuddy.data.persistent.app.TokenManagerList;
import com.musicslayer.cryptobuddy.state.StateObj;
import com.musicslayer.cryptobuddy.util.HashMapUtil;
import com.musicslayer.cryptobuddy.util.HelpUtil;
import com.musicslayer.cryptobuddy.util.ToastUtil;
import com.musicslayer.cryptobuddy.view.table.ExchangeTable;

import java.util.ArrayList;

public class ExchangeExplorerActivity extends BaseActivity {
    public BaseDialogFragment confirmBackDialogFragment;

    ExchangeTable table;

    ArrayList<CryptoExchange> cryptoExchangeArrayList = new ArrayList<>();

    public ArrayList<Boolean> includeBalances;
    public ArrayList<Boolean> includeTransactions;

    public boolean hasDiscrepancy = false;
    public boolean hasProblem = false;

    @Override
    public int getAdLayoutViewID() {
        return R.id.exchange_explorer_adLayout;
    }

    @Override
    public int getProgressViewID() {
        return R.id.exchange_explorer_progressBar;
    }

    @Override
    public void onBackPressedImpl() {
        confirmBackDialogFragment.show(ExchangeExplorerActivity.this, "back");
    }

    @Override
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

        CryptoExchange cryptoExchange = getIntent().getParcelableExtra("CryptoExchange");
        cryptoExchangeArrayList.add(cryptoExchange);
        if(savedInstanceState == null) {
            HashMapUtil.putValueInMap(StateObj.exchangeDataMap, cryptoExchange, ExchangeData.getNoData(cryptoExchange));
        }

        TextView T_INFO = findViewById(R.id.exchange_explorer_infoTextView);
        T_INFO.setText("Exchange = " + cryptoExchangeArrayList.get(0).toString());

        Toolbar toolbar = findViewById(R.id.exchange_explorer_toolbar);
        setSupportActionBar(toolbar);

        ImageButton discrepancyButton = findViewById(R.id.exchange_explorer_discrepancyButton);
        discrepancyButton.setOnClickListener(new CrashView.CrashOnClickListener(this) {
            @Override
            public void onClickImpl(View view) {
                // Only pass in cryptoExchanges that have discrepancies.
                ArrayList<CryptoExchange> cryptoExchangeDiscrepancyArrayList = new ArrayList<>();
                for(CryptoExchange cryptoExchange : cryptoExchangeArrayList) {
                    ExchangeData exchangeData = HashMapUtil.getValueFromMap(StateObj.exchangeDataMap, cryptoExchange);
                    if(exchangeData.hasDiscrepancy()) {
                        cryptoExchangeDiscrepancyArrayList.add(cryptoExchange);
                    }
                }

                BaseDialogFragment discrepancyDialogFragment = BaseDialogFragment.newInstance(ExchangeDiscrepancyDialog.class, cryptoExchangeDiscrepancyArrayList);
                discrepancyDialogFragment.show(ExchangeExplorerActivity.this, "discrepancy");
            }
        });

        ImageButton problemButton = findViewById(R.id.exchange_explorer_problemButton);
        problemButton.setOnClickListener(new CrashView.CrashOnClickListener(this) {
            @Override
            public void onClickImpl(View view) {
                BaseDialogFragment problemDialogFragment = BaseDialogFragment.newInstance(ExchangeProblemDialog.class, cryptoExchangeArrayList);
                problemDialogFragment.show(ExchangeExplorerActivity.this, "problem");
            }
        });

        if(savedInstanceState == null) {
            hasDiscrepancy = ExchangeData.hasDiscrepancy(new ArrayList<>(StateObj.exchangeDataMap.values()));
            hasProblem = ExchangeData.hasProblem(new ArrayList<>(StateObj.exchangeDataMap.values()));
            updateInfoButtons();
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
                BaseDialogFragment.newInstance(ExchangeInfoDialog.class, cryptoExchangeArrayList).show(ExchangeExplorerActivity.this, "info");
            }
        });

        FloatingActionButton fab_total = findViewById(R.id.exchange_explorer_totalButton);
        fab_total.setOnClickListener(new CrashView.CrashOnClickListener(this) {
            @Override
            public void onClickImpl(View view) {
                StateObj.filteredTransactionArrayList = table.getFilteredTransactionArrayList();
                BaseDialogFragment.newInstance(TotalDialog.class).show(ExchangeExplorerActivity.this, "total");
            }
        });

        FloatingActionButton fab_authorize = findViewById(R.id.exchange_explorer_authorizeButton);
        fab_authorize.setOnClickListener(new CrashView.CrashOnClickListener(this) {
            @Override
            public void onClickImpl(View view) {
                BaseDialogFragment.newInstance(AuthorizeExchangeDialog.class, cryptoExchangeArrayList).show(ExchangeExplorerActivity.this, "authorize_exchange");
            }
        });

        ProgressDialogFragment progressDialogFragment = ProgressDialogFragment.newInstance(ProgressDialog.class);
        progressDialogFragment.setOnShowListener(new CrashDialogInterface.CrashOnShowListener(this) {
            @Override
            public void onShowImpl(DialogInterface dialog) {
                ProgressDialogFragment.updateProgressTitle("Downloading Exchange Data...");

                CryptoExchange cryptoExchange = cryptoExchangeArrayList.get(0);

                ExchangeData newExchangeData;

                if(includeBalances.get(0) && includeTransactions.get(0)) {
                    newExchangeData = ExchangeData.getAllData(cryptoExchange);
                }
                else if(includeBalances.get(0)) {
                    newExchangeData = ExchangeData.getCurrentBalanceData(cryptoExchange);
                }
                else if(includeTransactions.get(0)) {
                    newExchangeData = ExchangeData.getTransactionsData(cryptoExchange);
                }
                else {
                    newExchangeData = ExchangeData.getNoData(cryptoExchange);
                }

                // Save found tokens, potentially from multiple TokenManagers.
                PersistentAppDataStore.getInstance(TokenManagerList.class).saveAllData();

                ProgressDialogFragment.setValue(DataBridge.serialize(newExchangeData, ExchangeData.class));
            }
        });
        progressDialogFragment.setOnDismissListener(new CrashDialogInterface.CrashOnDismissListener(this) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                ExchangeData newExchangeData = DataBridge.deserialize(ProgressDialogFragment.getValue(), ExchangeData.class);

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
                    ToastUtil.showToast("incomplete_exchange_data");
                }

                CryptoExchange cryptoExchange = cryptoExchangeArrayList.get(0);
                ExchangeData oldExchangeData = HashMapUtil.getValueFromMap(StateObj.exchangeDataMap, cryptoExchange);
                ExchangeData mergedExchangeData = ExchangeData.merge(oldExchangeData, newExchangeData);
                HashMapUtil.putValueInMap(StateObj.exchangeDataMap, cryptoExchange, mergedExchangeData);

                hasDiscrepancy = ExchangeData.hasDiscrepancy(new ArrayList<>(StateObj.exchangeDataMap.values()));
                hasProblem = ExchangeData.hasProblem(new ArrayList<>(StateObj.exchangeDataMap.values()));

                updateLayout();
                updateInfoButtons();

                ToastUtil.showToast("exchange_data_downloaded");
            }
        });
        progressDialogFragment.restoreListeners(this, "progress");

        BaseDialogFragment downloadDialogFragment = BaseDialogFragment.newInstance(DownloadExchangeDataDialog.class, cryptoExchangeArrayList);
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

    public void updateInfoButtons() {
        ImageButton discrepancyButton = findViewById(R.id.exchange_explorer_discrepancyButton);
        discrepancyButton.setVisibility(hasDiscrepancy ? View.VISIBLE : View.GONE);

        ImageButton problemInfoButton = findViewById(R.id.exchange_explorer_problemButton);
        problemInfoButton.setVisibility(hasProblem ? View.VISIBLE : View.GONE);
    }

    @Override
    public boolean onCreateOptionsMenuImpl(Menu menu) {
        menu.add(0, 1, 100, "Crypto Prices");
        menu.add(0, 2, 200, "Crypto Converter");
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

    @Override
    public void onSaveInstanceStateImpl(@NonNull Bundle bundle) {
        super.onSaveInstanceStateImpl(bundle);
        bundle.putSerializable("includeBalances", includeBalances);
        bundle.putSerializable("includeTransactions", includeTransactions);
        bundle.putBoolean("hasDiscrepancy", hasDiscrepancy);
        bundle.putBoolean("hasProblem", hasProblem);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onRestoreInstanceStateImpl(Bundle bundle) {
        super.onRestoreInstanceStateImpl(bundle);
        if(bundle != null) {
            includeBalances = (ArrayList<Boolean>)bundle.getSerializable("includeBalances");
            includeTransactions = (ArrayList<Boolean>)bundle.getSerializable("includeTransactions");
            hasDiscrepancy = bundle.getBoolean("hasDiscrepancy");
            hasProblem = bundle.getBoolean("hasProblem");

            updateInfoButtons();
        }
    }
}