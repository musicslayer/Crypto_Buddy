package com.musicslayer.cryptobuddy.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;

import androidx.appcompat.widget.Toolbar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.crash.CrashDialogInterface;
import com.musicslayer.cryptobuddy.crash.CrashView;
import com.musicslayer.cryptobuddy.dialog.AddTransactionDialog;
import com.musicslayer.cryptobuddy.dialog.ConfirmBackDialog;
import com.musicslayer.cryptobuddy.dialog.ConfirmResetTableDialog;
import com.musicslayer.cryptobuddy.dialog.CryptoConverterDialog;
import com.musicslayer.cryptobuddy.dialog.CryptoPricesDialog;
import com.musicslayer.cryptobuddy.dialog.BaseDialogFragment;
import com.musicslayer.cryptobuddy.dialog.ReportFeedbackDialog;
import com.musicslayer.cryptobuddy.dialog.TotalDialog;
import com.musicslayer.cryptobuddy.state.StateObj;
import com.musicslayer.cryptobuddy.util.HelpUtil;
import com.musicslayer.cryptobuddy.view.table.TransactionTable;

public class TransactionExplorerActivity extends BaseActivity {
    public BaseDialogFragment confirmBackDialogFragment;

    TransactionTable table;

    @Override
    public int getAdLayoutViewID() {
        return R.id.transaction_explorer_adLayout;
    }

    @Override
    public void onBackPressedImpl() {
        confirmBackDialogFragment.show(TransactionExplorerActivity.this, "back");
    }

    @Override
    public void createLayout(Bundle savedInstanceState) {
        setContentView(R.layout.activity_transaction_explorer);

        confirmBackDialogFragment = BaseDialogFragment.newInstance(ConfirmBackDialog.class);
        confirmBackDialogFragment.setOnDismissListener(new CrashDialogInterface.CrashOnDismissListener(this) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                if(((ConfirmBackDialog)dialog).isComplete) {
                    startActivity(new Intent(TransactionExplorerActivity.this, MainActivity.class));
                    finish();
                }
            }
        });
        confirmBackDialogFragment.restoreListeners(this, "back");

        Toolbar toolbar = findViewById(R.id.transaction_explorer_toolbar);
        setSupportActionBar(toolbar);

        ImageButton helpButton = findViewById(R.id.transaction_explorer_helpButton);
        helpButton.setOnClickListener(new CrashView.CrashOnClickListener(this) {
            @Override
            public void onClickImpl(View view) {
                HelpUtil.showHelp(TransactionExplorerActivity.this, R.raw.help_transaction_explorer);
            }
        });

        table = findViewById(R.id.transaction_explorer_table);
        table.pageView = findViewById(R.id.transaction_explorer_tablePageView);
        table.pageView.setTable(table);
        table.pageView.updateLayout();

        BaseDialogFragment addTransactionDialogFragment = BaseDialogFragment.newInstance(AddTransactionDialog.class);
        addTransactionDialogFragment.setOnDismissListener(new CrashDialogInterface.CrashOnDismissListener(this) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                if(((AddTransactionDialog)dialog).isComplete) {
                    table.addRow(((AddTransactionDialog) dialog).user_TRANSACTION);
                }
            }
        });
        addTransactionDialogFragment.restoreListeners(this, "transaction");

        FloatingActionButton fab_add = findViewById(R.id.transaction_explorer_addButton);
        fab_add.setOnClickListener(new CrashView.CrashOnClickListener(this) {
            @Override
            public void onClickImpl(View view) {
                addTransactionDialogFragment.show(TransactionExplorerActivity.this, "transaction");
            }
        });

        FloatingActionButton fab_total = findViewById(R.id.transaction_explorer_totalButton);
        fab_total.setOnClickListener(new CrashView.CrashOnClickListener(this) {
            @Override
            public void onClickImpl(View view) {
                StateObj.filteredTransactionArrayList = table.getFilteredTransactionArrayList();
                BaseDialogFragment.newInstance(TotalDialog.class).show(TransactionExplorerActivity.this, "total");
            }
        });

        BaseDialogFragment confirmResetTableDialogFragment = BaseDialogFragment.newInstance(ConfirmResetTableDialog.class);
        confirmResetTableDialogFragment.setOnDismissListener(new CrashDialogInterface.CrashOnDismissListener(this) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                if(((ConfirmResetTableDialog)dialog).isComplete) {
                    table.resetTable();
                }
            }
        });
        confirmResetTableDialogFragment.restoreListeners(this, "reset");

        FloatingActionButton fab_reset = findViewById(R.id.transaction_explorer_resetButton);
        fab_reset.setOnClickListener(new CrashView.CrashOnClickListener(this) {
            @Override
            public void onClickImpl(View view) {
                confirmResetTableDialogFragment.show(TransactionExplorerActivity.this, "reset");
            }
        });
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
            BaseDialogFragment.newInstance(CryptoPricesDialog.class).show(TransactionExplorerActivity.this, "price");
            return true;
        }
        else if (id == 2) {
            BaseDialogFragment.newInstance(CryptoConverterDialog.class).show(TransactionExplorerActivity.this, "converter");
            return true;
        }
        else if (id == 3) {
            String type = "Transaction";
            StateObj.tableInfo = table.getInfo();
            BaseDialogFragment.newInstance(ReportFeedbackDialog.class, type).show(TransactionExplorerActivity.this, "feedback");
            return true;
        }

        return false;
    }
}