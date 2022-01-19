package com.musicslayer.cryptobuddy.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.crash.CrashDialogInterface;
import com.musicslayer.cryptobuddy.crash.CrashView;
import com.musicslayer.cryptobuddy.dialog.ConfirmBackDialog;
import com.musicslayer.cryptobuddy.dialog.CryptoConverterDialog;
import com.musicslayer.cryptobuddy.dialog.ReportFeedbackDialog;
import com.musicslayer.cryptobuddy.persistence.TransactionPortfolio;
import com.musicslayer.cryptobuddy.persistence.TransactionPortfolioObj;
import com.musicslayer.cryptobuddy.dialog.AddTransactionDialog;
import com.musicslayer.cryptobuddy.dialog.CryptoPricesDialog;
import com.musicslayer.cryptobuddy.dialog.BaseDialogFragment;
import com.musicslayer.cryptobuddy.dialog.TotalDialog;
import com.musicslayer.cryptobuddy.state.StateObj;
import com.musicslayer.cryptobuddy.transaction.Transaction;
import com.musicslayer.cryptobuddy.util.HelpUtil;
import com.musicslayer.cryptobuddy.view.table.Table;
import com.musicslayer.cryptobuddy.view.table.TransactionTable;

public class TransactionPortfolioExplorerActivity extends BaseActivity {
    public BaseDialogFragment confirmBackDialogFragment;

    TransactionTable table;

    TransactionPortfolioObj transactionPortfolioObj;

    public int getAdLayoutViewID() {
        return R.id.transaction_portfolio_explorer_adLayout;
    }

    @Override
    public void onBackPressedImpl() {
        confirmBackDialogFragment.show(TransactionPortfolioExplorerActivity.this, "back");
    }

    public void createLayout(Bundle savedInstanceState) {
        setContentView(R.layout.activity_transaction_portfolio_explorer);

        confirmBackDialogFragment = BaseDialogFragment.newInstance(ConfirmBackDialog.class);
        confirmBackDialogFragment.setOnDismissListener(new CrashDialogInterface.CrashOnDismissListener(this) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                if(((ConfirmBackDialog)dialog).isComplete) {
                    startActivity(new Intent(TransactionPortfolioExplorerActivity.this, TransactionPortfolioViewerActivity.class));
                    finish();
                }
            }
        });
        confirmBackDialogFragment.restoreListeners(this, "back");

        transactionPortfolioObj = TransactionPortfolio.getFromName(getIntent().getStringExtra("TransactionPortfolioName"));

        if(savedInstanceState == null) {
            StateObj.transactionPortfolioObj = transactionPortfolioObj;
        }

        Toolbar toolbar = findViewById(R.id.transaction_portfolio_explorer_toolbar);
        setSupportActionBar(toolbar);

        TextView T = findViewById(R.id.transaction_portfolio_explorer_infoTextView);
        T.setText("Portfolio = " + transactionPortfolioObj.name);

        ImageButton helpButton = findViewById(R.id.transaction_portfolio_explorer_helpButton);
        helpButton.setOnClickListener(new CrashView.CrashOnClickListener(this) {
            @Override
            public void onClickImpl(View view) {
                HelpUtil.showHelp(TransactionPortfolioExplorerActivity.this, R.raw.help_transaction_portfolio_explorer);
            }
        });

        table = findViewById(R.id.transaction_portfolio_explorer_table);
        table.pageView = findViewById(R.id.transaction_portfolio_explorer_tablePageView);
        table.pageView.setTable(table);
        table.pageView.updateLayout();
        table.setOnDeleteTransactionListener(new Table.OnDeleteTransactionListener() {
            @Override
            public void onDeleteTransaction(Table table, Transaction transaction) {
                // Remove the transaction from the portfolio.
                transactionPortfolioObj.removeData(transaction);
                TransactionPortfolio.updatePortfolio(TransactionPortfolioExplorerActivity.this, transactionPortfolioObj);
            }
        });

        BaseDialogFragment addTransactionDialogFragment = BaseDialogFragment.newInstance(AddTransactionDialog.class);
        addTransactionDialogFragment.setOnDismissListener(new CrashDialogInterface.CrashOnDismissListener(this) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                if(((AddTransactionDialog)dialog).isComplete) {
                    transactionPortfolioObj.addData(((AddTransactionDialog) dialog).user_TRANSACTION);
                    TransactionPortfolio.updatePortfolio(TransactionPortfolioExplorerActivity.this, transactionPortfolioObj);

                    table.addRow(((AddTransactionDialog) dialog).user_TRANSACTION);
                }
            }
        });
        addTransactionDialogFragment.restoreListeners(this, "transaction");

        FloatingActionButton fab_add = findViewById(R.id.transaction_portfolio_explorer_addButton);
        fab_add.setOnClickListener(new CrashView.CrashOnClickListener(this) {
            @Override
            public void onClickImpl(View view) {
                addTransactionDialogFragment.show(TransactionPortfolioExplorerActivity.this, "transaction");
            }
        });

        FloatingActionButton fab_total = findViewById(R.id.transaction_portfolio_explorer_totalButton);
        fab_total.setOnClickListener(new CrashView.CrashOnClickListener(this) {
            @Override
            public void onClickImpl(View view) {
                StateObj.filteredMaskedTransactionArrayList = table.getFilteredMaskedTransactionArrayList();
                BaseDialogFragment.newInstance(TotalDialog.class).show(TransactionPortfolioExplorerActivity.this, "total");
            }
        });

        if(savedInstanceState == null) {
            table.addRows(transactionPortfolioObj.transactionArrayList);
        }
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
            BaseDialogFragment.newInstance(CryptoPricesDialog.class).show(TransactionPortfolioExplorerActivity.this, "price");
            return true;
        }
        else if (id == 2) {
            BaseDialogFragment.newInstance(CryptoConverterDialog.class).show(TransactionPortfolioExplorerActivity.this, "converter");
            return true;
        }
        else if (id == 3) {
            String type = "TransactionPortfolio";
            StateObj.tableInfo = table.getInfo();
            BaseDialogFragment.newInstance(ReportFeedbackDialog.class, type).show(TransactionPortfolioExplorerActivity.this, "feedback");
            return true;
        }

        return false;
    }
}