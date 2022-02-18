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
import com.musicslayer.cryptobuddy.dialog.AuthorizeExchangeDialog;
import com.musicslayer.cryptobuddy.dialog.BaseDialogFragment;
import com.musicslayer.cryptobuddy.dialog.ChooseExchangeDialog;
import com.musicslayer.cryptobuddy.dialog.ConfirmBackDialog;
import com.musicslayer.cryptobuddy.dialog.CryptoConverterDialog;
import com.musicslayer.cryptobuddy.dialog.CryptoPricesDialog;
import com.musicslayer.cryptobuddy.dialog.DiscreteFilterDialog;
import com.musicslayer.cryptobuddy.dialog.DownloadExchangeDataDialog;
import com.musicslayer.cryptobuddy.dialog.ExchangeDiscrepancyDialog;
import com.musicslayer.cryptobuddy.dialog.ExchangeInfoDialog;
import com.musicslayer.cryptobuddy.dialog.ExchangeProblemDialog;
import com.musicslayer.cryptobuddy.dialog.ProgressDialog;
import com.musicslayer.cryptobuddy.dialog.ProgressDialogFragment;
import com.musicslayer.cryptobuddy.dialog.RemoveExchangeDialog;
import com.musicslayer.cryptobuddy.dialog.ReportFeedbackDialog;
import com.musicslayer.cryptobuddy.dialog.TotalDialog;
import com.musicslayer.cryptobuddy.filter.DiscreteFilter;
import com.musicslayer.cryptobuddy.persistence.ExchangePortfolio;
import com.musicslayer.cryptobuddy.persistence.TokenManagerList;
import com.musicslayer.cryptobuddy.data.Serialization;
import com.musicslayer.cryptobuddy.state.StateObj;
import com.musicslayer.cryptobuddy.util.HashMapUtil;
import com.musicslayer.cryptobuddy.util.HelpUtil;
import com.musicslayer.cryptobuddy.util.ToastUtil;
import com.musicslayer.cryptobuddy.view.table.ExchangeTable;

import java.util.ArrayList;

public class ExchangePortfolioExplorerActivity extends BaseActivity {
    public BaseDialogFragment confirmBackDialogFragment;

    ExchangeTable table;

    DiscreteFilter exchangeFilter = new DiscreteFilter();

    public ArrayList<Boolean> includeBalances;
    public ArrayList<Boolean> includeTransactions;

    public boolean hasDiscrepancy = false;
    public boolean hasProblem = false;

    @Override
    public int getAdLayoutViewID() {
        return R.id.exchange_portfolio_explorer_adLayout;
    }

    @Override
    public void onBackPressedImpl() {
        confirmBackDialogFragment.show(ExchangePortfolioExplorerActivity.this, "back");
    }

    @Override
    public void createLayout(Bundle savedInstanceState) {
        setContentView(R.layout.activity_exchange_portfolio_explorer);

        confirmBackDialogFragment = BaseDialogFragment.newInstance(ConfirmBackDialog.class);
        confirmBackDialogFragment.setOnDismissListener(new CrashDialogInterface.CrashOnDismissListener(this) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                if(((ConfirmBackDialog)dialog).isComplete) {
                    startActivity(new Intent(ExchangePortfolioExplorerActivity.this, ExchangePortfolioViewerActivity.class));
                    finish();
                }
            }
        });
        confirmBackDialogFragment.restoreListeners(this, "back");

        if(savedInstanceState == null) {
            StateObj.exchangePortfolioObj = ExchangePortfolio.getFromName(ExchangePortfolioExplorerActivity.this, getIntent().getStringExtra("ExchangePortfolioName"));
        }

        updateFilter();

        for(CryptoExchange cryptoExchange : StateObj.exchangePortfolioObj.cryptoExchangeArrayList) {
            if(savedInstanceState == null) {
                HashMapUtil.putValueInMap(StateObj.exchangeDataMap, cryptoExchange, ExchangeData.getNoData(cryptoExchange));
                HashMapUtil.putValueInMap(StateObj.exchangeDataFilterMap, cryptoExchange, ExchangeData.getNoData(cryptoExchange));
            }
        }

        TextView T_INFO = findViewById(R.id.exchange_portfolio_explorer_infoTextView);
        T_INFO.setText("Portfolio = " + StateObj.exchangePortfolioObj.name);

        Toolbar toolbar = findViewById(R.id.exchange_portfolio_explorer_toolbar);
        setSupportActionBar(toolbar);

        ImageButton discrepancyButton = findViewById(R.id.exchange_portfolio_explorer_discrepancyButton);
        discrepancyButton.setOnClickListener(new CrashView.CrashOnClickListener(this) {
            @Override
            public void onClickImpl(View view) {
                // Only pass in cryptoExchanges that have discrepancies.
                ArrayList<CryptoExchange> cryptoExchangeDiscrepancyArrayList = new ArrayList<>();
                for(CryptoExchange cryptoExchange : StateObj.exchangePortfolioObj.cryptoExchangeArrayList) {
                    ExchangeData exchangeData = HashMapUtil.getValueFromMap(StateObj.exchangeDataMap, cryptoExchange);
                    if(exchangeData.hasDiscrepancy()) {
                        cryptoExchangeDiscrepancyArrayList.add(cryptoExchange);
                    }
                }

                BaseDialogFragment discrepancyDialogFragment = BaseDialogFragment.newInstance(ExchangeDiscrepancyDialog.class, cryptoExchangeDiscrepancyArrayList);
                discrepancyDialogFragment.show(ExchangePortfolioExplorerActivity.this, "discrepancy");
            }
        });

        ImageButton problemButton = findViewById(R.id.exchange_portfolio_explorer_problemButton);
        problemButton.setOnClickListener(new CrashView.CrashOnClickListener(this) {
            @Override
            public void onClickImpl(View view) {
                BaseDialogFragment infoDialogFragment = BaseDialogFragment.newInstance(ExchangeProblemDialog.class, StateObj.exchangePortfolioObj.cryptoExchangeArrayList);
                infoDialogFragment.show(ExchangePortfolioExplorerActivity.this, "problem");
            }
        });

        if(savedInstanceState == null) {
            hasDiscrepancy = ExchangeData.hasDiscrepancy(new ArrayList<>(StateObj.exchangeDataMap.values()));
            hasProblem = ExchangeData.hasProblem(new ArrayList<>(StateObj.exchangeDataMap.values()));
            updateInfoButtons();
        }

        ImageButton helpButton = findViewById(R.id.exchange_portfolio_explorer_helpButton);
        helpButton.setOnClickListener(new CrashView.CrashOnClickListener(this) {
            @Override
            public void onClickImpl(View view) {
                HelpUtil.showHelp(ExchangePortfolioExplorerActivity.this, R.raw.help_exchange_portfolio_explorer);
            }
        });

        table = findViewById(R.id.exchange_portfolio_explorer_table);
        table.pageView = findViewById(R.id.exchange_portfolio_explorer_tablePageView);
        table.pageView.setTable(table);
        table.pageView.updateLayout();

        BaseDialogFragment chooseExchangeDialogFragment = BaseDialogFragment.newInstance(ChooseExchangeDialog.class);
        chooseExchangeDialogFragment.setOnDismissListener(new CrashDialogInterface.CrashOnDismissListener(this) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                if(((ChooseExchangeDialog)dialog).isComplete) {
                    // Save new exchange to the portfolio.
                    CryptoExchange newCryptoExchange = ((ChooseExchangeDialog)dialog).user_CRYPTOEXCHANGE;

                    if(StateObj.exchangePortfolioObj.isSaved(newCryptoExchange)) {
                        ToastUtil.showToast(ExchangePortfolioExplorerActivity.this,"exchange_in_portfolio");
                    }
                    else {
                        StateObj.exchangePortfolioObj.addData(newCryptoExchange);
                        ExchangePortfolio.updatePortfolio(ExchangePortfolioExplorerActivity.this, StateObj.exchangePortfolioObj);

                        updateFilter();

                        HashMapUtil.putValueInMap(StateObj.exchangeDataMap, newCryptoExchange, ExchangeData.getNoData(newCryptoExchange));
                        HashMapUtil.putValueInMap(StateObj.exchangeDataFilterMap, newCryptoExchange, ExchangeData.getNoData(newCryptoExchange));

                        hasDiscrepancy = ExchangeData.hasDiscrepancy(new ArrayList<>(StateObj.exchangeDataMap.values()));
                        hasProblem = ExchangeData.hasProblem(new ArrayList<>(StateObj.exchangeDataMap.values()));
                        updateInfoButtons();
                    }
                }
            }
        });
        chooseExchangeDialogFragment.restoreListeners(this, "add");

        FloatingActionButton fab_add = findViewById(R.id.exchange_portfolio_explorer_addButton);
        fab_add.setOnClickListener(new CrashView.CrashOnClickListener(this) {
            @Override
            public void onClickImpl(View view) {
                chooseExchangeDialogFragment.show(ExchangePortfolioExplorerActivity.this, "add");
            }
        });

        BaseDialogFragment removeExchangeDialogFragment = BaseDialogFragment.newInstance(RemoveExchangeDialog.class, StateObj.exchangePortfolioObj.cryptoExchangeArrayList);
        removeExchangeDialogFragment.setOnDismissListener(new CrashDialogInterface.CrashOnDismissListener(this) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                if(((RemoveExchangeDialog)dialog).isComplete) {
                    // Remove exchanges from portfolio and then remove their data from the table.
                    ArrayList<CryptoExchange> toRemove = ((RemoveExchangeDialog)dialog).user_cryptoExchangeArrayList;
                    for(CryptoExchange exchange : toRemove) {
                        StateObj.exchangePortfolioObj.removeData(exchange);
                        HashMapUtil.removeValueFromMap(StateObj.exchangeDataMap, exchange);
                        HashMapUtil.removeValueFromMap(StateObj.exchangeDataFilterMap, exchange);
                    }

                    ExchangePortfolio.updatePortfolio(ExchangePortfolioExplorerActivity.this, StateObj.exchangePortfolioObj);

                    hasDiscrepancy = ExchangeData.hasDiscrepancy(new ArrayList<>(StateObj.exchangeDataMap.values()));
                    hasProblem = ExchangeData.hasProblem(new ArrayList<>(StateObj.exchangeDataMap.values()));

                    updateInfoButtons();
                    updateFilter();
                    updateLayout();
                }
            }
        });
        removeExchangeDialogFragment.restoreListeners(this, "remove");

        FloatingActionButton fab_remove = findViewById(R.id.exchange_portfolio_explorer_removeButton);
        fab_remove.setOnClickListener(new CrashView.CrashOnClickListener(this) {
            @Override
            public void onClickImpl(View view) {
                removeExchangeDialogFragment.show(ExchangePortfolioExplorerActivity.this, "remove");
            }
        });

        FloatingActionButton fab_info = findViewById(R.id.exchange_portfolio_explorer_infoButton);
        fab_info.setOnClickListener(new CrashView.CrashOnClickListener(this) {
            @Override
            public void onClickImpl(View view) {
                BaseDialogFragment.newInstance(ExchangeInfoDialog.class, StateObj.exchangePortfolioObj.cryptoExchangeArrayList).show(ExchangePortfolioExplorerActivity.this, "info");
            }
        });

        FloatingActionButton fab_total = findViewById(R.id.exchange_portfolio_explorer_totalButton);
        fab_total.setOnClickListener(new CrashView.CrashOnClickListener(this) {
            @Override
            public void onClickImpl(View view) {
                StateObj.filteredTransactionArrayList = table.getFilteredTransactionArrayList();
                BaseDialogFragment.newInstance(TotalDialog.class).show(ExchangePortfolioExplorerActivity.this, "total");
            }
        });

        FloatingActionButton fab_authorize = findViewById(R.id.exchange_portfolio_explorer_authorizeButton);
        fab_authorize.setOnClickListener(new CrashView.CrashOnClickListener(this) {
            @Override
            public void onClickImpl(View view) {
                BaseDialogFragment.newInstance(AuthorizeExchangeDialog.class, StateObj.exchangePortfolioObj.cryptoExchangeArrayList).show(ExchangePortfolioExplorerActivity.this, "authorize_exchange");
            }
        });

        ProgressDialogFragment download_progressDialogFragment = ProgressDialogFragment.newInstance(ProgressDialog.class);
        download_progressDialogFragment.setOnShowListener(new CrashDialogInterface.CrashOnShowListener(this) {
            @Override
            public void onShowImpl(DialogInterface dialog) {
                ProgressDialogFragment.updateProgressTitle("Downloading Exchange Data...");

                ArrayList<CryptoExchange> exchangeArrayList = StateObj.exchangePortfolioObj.cryptoExchangeArrayList;

                ArrayList<ExchangeData> newExchangeDataArrayList = new ArrayList<>();
                for(int i = 0; i < exchangeArrayList.size(); i++) {
                    ProgressDialogFragment.reportProgress(i, exchangeArrayList.size(), "Exchanges Finished");

                    CryptoExchange cryptoExchange = exchangeArrayList.get(i);

                    if(ProgressDialogFragment.isCancelled()) { return; }

                    ExchangeData newExchangeData;

                    if(includeBalances.get(i) && includeTransactions.get(i)) {
                        newExchangeData = ExchangeData.getAllData(cryptoExchange);
                    }
                    else if(includeBalances.get(i)) {
                        newExchangeData = ExchangeData.getCurrentBalanceData(cryptoExchange);
                    }
                    else if(includeTransactions.get(i)) {
                        newExchangeData = ExchangeData.getTransactionsData(cryptoExchange);
                    }
                    else {
                        newExchangeData = ExchangeData.getNoData(cryptoExchange);
                    }

                    newExchangeDataArrayList.add(newExchangeData);

                    // Save found tokens, potentially from multiple TokenManagers.
                    TokenManagerList.saveAllData(ExchangePortfolioExplorerActivity.this);
                }

                ProgressDialogFragment.setValue(Serialization.serializeArrayList(newExchangeDataArrayList));
            }
        });

        download_progressDialogFragment.setOnDismissListener(new CrashDialogInterface.CrashOnDismissListener(this) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                ArrayList<ExchangeData> newExchangeDataArrayList = Serialization.deserializeArrayList(ProgressDialogFragment.getValue(), ExchangeData.class);

                for(int i = 0; i < newExchangeDataArrayList.size(); i++) {
                    ExchangeData newExchangeData = newExchangeDataArrayList.get(i);

                    boolean isComplete;
                    if(includeBalances.get(i) && includeTransactions.get(i)) {
                        isComplete = newExchangeData.isComplete();
                    }
                    else if(includeBalances.get(i)) {
                        isComplete = newExchangeData.isCurrentBalanceComplete();
                    }
                    else if(includeTransactions.get(i)) {
                        isComplete = newExchangeData.isTransactionsComplete();
                    }
                    else {
                        isComplete = true;
                    }

                    if(!isComplete) {
                        // Only alert once. Others would be redundant.
                        ToastUtil.showToast(ExchangePortfolioExplorerActivity.this,"incomplete_exchange_data");
                        break;
                    }
                }

                for(int i = 0; i < StateObj.exchangePortfolioObj.cryptoExchangeArrayList.size(); i++) {
                    CryptoExchange cryptoExchange = StateObj.exchangePortfolioObj.cryptoExchangeArrayList.get(i);
                    ExchangeData newExchangeData = newExchangeDataArrayList.get(i);
                    ExchangeData oldExchangeData = HashMapUtil.getValueFromMap(StateObj.exchangeDataMap, cryptoExchange);
                    ExchangeData mergedExchangeData = ExchangeData.merge(oldExchangeData, newExchangeData);
                    HashMapUtil.putValueInMap(StateObj.exchangeDataMap, cryptoExchange, mergedExchangeData);
                }

                // Apply filter after downloading data.
                ArrayList<String> choices = exchangeFilter.user_choices;

                StateObj.exchangeDataFilterMap.clear();
                for(CryptoExchange cryptoExchange : new ArrayList<>(StateObj.exchangeDataMap.keySet())) {
                    if(choices.contains(cryptoExchange.toString())) {
                        ExchangeData exchangeData = HashMapUtil.getValueFromMap(StateObj.exchangeDataMap, cryptoExchange);
                        HashMapUtil.putValueInMap(StateObj.exchangeDataFilterMap, cryptoExchange, exchangeData);
                    }
                }

                hasDiscrepancy = ExchangeData.hasDiscrepancy(new ArrayList<>(StateObj.exchangeDataMap.values()));
                hasProblem = ExchangeData.hasProblem(new ArrayList<>(StateObj.exchangeDataMap.values()));

                updateLayout();
                updateInfoButtons();

                ToastUtil.showToast(ExchangePortfolioExplorerActivity.this,"exchange_data_downloaded");
            }
        });
        download_progressDialogFragment.restoreListeners(this, "progress_download");

        BaseDialogFragment downloadDialogFragment = BaseDialogFragment.newInstance(DownloadExchangeDataDialog.class, StateObj.exchangePortfolioObj.cryptoExchangeArrayList);
        downloadDialogFragment.setOnDismissListener(new CrashDialogInterface.CrashOnDismissListener(this) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                if(((DownloadExchangeDataDialog)dialog).isComplete) {
                    includeBalances = ((DownloadExchangeDataDialog)dialog).user_BALANCES;
                    includeTransactions = ((DownloadExchangeDataDialog)dialog).user_TRANSACTIONS;
                    download_progressDialogFragment.show(ExchangePortfolioExplorerActivity.this, "progress_download");
                }
            }
        });
        downloadDialogFragment.restoreListeners(this, "download");

        AppCompatButton downloadDataButton = findViewById(R.id.exchange_portfolio_explorer_downloadDataButton);
        downloadDataButton.setOnClickListener(new CrashView.CrashOnClickListener(this) {
            @Override
            public void onClickImpl(View view) {
                downloadDialogFragment.show(ExchangePortfolioExplorerActivity.this, "download");
            }
        });

        BaseDialogFragment exchangeFilterDialogFragment = exchangeFilter.getGenericDialogFragment();
        exchangeFilterDialogFragment.setOnDismissListener(new CrashDialogInterface.CrashOnDismissListener(this) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                if(((DiscreteFilterDialog)dialog).isComplete) {
                    exchangeFilter = ((DiscreteFilterDialog)dialog).discreteFilter;

                    ArrayList<String> choices = exchangeFilter.user_choices;

                    StateObj.exchangeDataFilterMap.clear();
                    for(CryptoExchange cryptoExchange : new ArrayList<>(StateObj.exchangeDataMap.keySet())) {
                        if(choices.contains(cryptoExchange.toString())) {
                            ExchangeData exchangeData = HashMapUtil.getValueFromMap(StateObj.exchangeDataMap, cryptoExchange);
                            HashMapUtil.putValueInMap(StateObj.exchangeDataFilterMap, cryptoExchange, exchangeData);
                        }
                    }

                    updateLayout();
                }
            }
        });
        exchangeFilterDialogFragment.restoreListeners(this, "exchange_filter");

        AppCompatButton filterExchangeButton = findViewById(R.id.exchange_portfolio_explorer_filterExchangeButton);
        filterExchangeButton.setOnClickListener(new CrashView.CrashOnClickListener(this) {
            @Override
            public void onClickImpl(View view) {
                exchangeFilterDialogFragment.updateArguments(DiscreteFilterDialog.class, exchangeFilter);
                exchangeFilterDialogFragment.show(ExchangePortfolioExplorerActivity.this, "exchange_filter");
            }
        });
    }

    public void updateLayout() {
        table.resetTable();
        table.addRowsFromExchangeDataArray(new ArrayList<>(StateObj.exchangeDataFilterMap.values()));
    }

    public void updateInfoButtons() {
        ImageButton discrepancyButton = findViewById(R.id.exchange_portfolio_explorer_discrepancyButton);
        discrepancyButton.setVisibility(hasDiscrepancy ? View.VISIBLE : View.GONE);

        ImageButton problemInfoButton = findViewById(R.id.exchange_portfolio_explorer_problemButton);
        problemInfoButton.setVisibility(hasProblem ? View.VISIBLE : View.GONE);
    }

    public void updateFilter() {
        ArrayList<String> data = new ArrayList<>();
        for(CryptoExchange cryptoExchange : StateObj.exchangePortfolioObj.cryptoExchangeArrayList) {
            data.add(cryptoExchange.toString());
        }

        exchangeFilter.updateFilterData(data);
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
            BaseDialogFragment.newInstance(CryptoPricesDialog.class).show(ExchangePortfolioExplorerActivity.this, "price");
            return true;
        }
        else if (id == 2) {
            BaseDialogFragment.newInstance(CryptoConverterDialog.class).show(ExchangePortfolioExplorerActivity.this, "converter");
            return true;
        }
        else if (id == 3) {
            String type = "ExchangePortfolio";
            StateObj.tableInfo = table.getInfo();
            StateObj.filterInfo = Serialization.serialize(exchangeFilter);
            BaseDialogFragment.newInstance(ReportFeedbackDialog.class, type).show(ExchangePortfolioExplorerActivity.this, "feedback");
            return true;
        }

        return false;
    }

    @Override
    public void onSaveInstanceStateImpl(@NonNull Bundle bundle) {
        bundle.putSerializable("includeBalances", includeBalances);
        bundle.putSerializable("includeTransactions", includeTransactions);
        bundle.putParcelable("filter", exchangeFilter);
        bundle.putBoolean("hasDiscrepancy", hasDiscrepancy);
        bundle.putBoolean("hasProblem", hasProblem);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onRestoreInstanceStateImpl(Bundle bundle) {
        if(bundle != null) {
            includeBalances = (ArrayList<Boolean>)bundle.getSerializable("includeBalances");
            includeTransactions = (ArrayList<Boolean>)bundle.getSerializable("includeTransactions");
            exchangeFilter = bundle.getParcelable("filter");
            hasDiscrepancy = bundle.getBoolean("hasDiscrepancy");
            hasProblem = bundle.getBoolean("hasProblem");

            updateInfoButtons();
        }
    }
}