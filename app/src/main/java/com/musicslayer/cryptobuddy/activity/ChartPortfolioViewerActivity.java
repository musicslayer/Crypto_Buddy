package com.musicslayer.cryptobuddy.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TableRow;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.Toolbar;

import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.crash.CrashDialogInterface;
import com.musicslayer.cryptobuddy.crash.CrashView;
import com.musicslayer.cryptobuddy.data.persistent.user.ChartPortfolio;
import com.musicslayer.cryptobuddy.data.persistent.user.ChartPortfolioObj;
import com.musicslayer.cryptobuddy.data.persistent.user.PersistentUserDataStore;
import com.musicslayer.cryptobuddy.dialog.BaseDialogFragment;
import com.musicslayer.cryptobuddy.dialog.ConfirmDeletePortfolioDialog;
import com.musicslayer.cryptobuddy.dialog.CreatePortfolioDialog;
import com.musicslayer.cryptobuddy.dialog.RenamePortfolioDialog;
import com.musicslayer.cryptobuddy.util.HelpUtil;
import com.musicslayer.cryptobuddy.util.ToastUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class ChartPortfolioViewerActivity extends BaseActivity {
    String currentDeletePortfolioName;
    String currentRenamePortfolioName;

    @Override
    public int getAdLayoutViewID() {
        return R.id.chart_portfolio_viewer_adLayout;
    }

    @Override
    public int getProgressViewID() {
        return R.id.chart_portfolio_viewer_progressBar;
    }

    @Override
    public void onBackPressedImpl() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    @Override
    public void createLayout(Bundle savedInstanceState) {
        setContentView(R.layout.activity_chart_portfolio_viewer);

        Toolbar toolbar = findViewById(R.id.chart_portfolio_viewer_toolbar);
        setSupportActionBar(toolbar);

        ImageButton helpButton = findViewById(R.id.chart_portfolio_viewer_helpButton);
        helpButton.setOnClickListener(new CrashView.CrashOnClickListener(this) {
            @Override
            public void onClickImpl(View view) {
                HelpUtil.showHelp(ChartPortfolioViewerActivity.this, R.raw.help_chart_portfolio_viewer);
            }
        });

        BaseDialogFragment createPortfolioDialogFragment = BaseDialogFragment.newInstance(CreatePortfolioDialog.class);
        createPortfolioDialogFragment.setOnDismissListener(new CrashDialogInterface.CrashOnDismissListener(this) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                if(((CreatePortfolioDialog)dialog).isComplete) {
                    String name = ((CreatePortfolioDialog)dialog).user_NAME;

                    if(ChartPortfolio.isSaved(name)) {
                        ToastUtil.showToast("portfolio_name_used");
                    }
                    else {
                        PersistentUserDataStore.getInstance(ChartPortfolio.class).addPortfolio(new ChartPortfolioObj(name));
                        updateLayout();
                    }
                }
            }
        });
        createPortfolioDialogFragment.restoreListeners(this, "create");

        Button bCreate = findViewById(R.id.chart_portfolio_viewer_addButton);
        bCreate.setOnClickListener(new CrashView.CrashOnClickListener(this) {
            @Override
            public void onClickImpl(View view) {
                createPortfolioDialogFragment.show(ChartPortfolioViewerActivity.this, "create");
            }
        });

        updateLayout();
    }

    public void updateLayout() {
        TableLayout table = findViewById(R.id.chart_portfolio_viewer_tableLayout);
        table.removeAllViews();

        BaseDialogFragment confirmDeletePortfolioDialogFragment = BaseDialogFragment.newInstance(ConfirmDeletePortfolioDialog.class);
        confirmDeletePortfolioDialogFragment.setOnDismissListener(new CrashDialogInterface.CrashOnDismissListener(this) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                if(((ConfirmDeletePortfolioDialog)dialog).isComplete) {
                    PersistentUserDataStore.getInstance(ChartPortfolio.class).removePortfolio(currentDeletePortfolioName);
                    updateLayout();
                }
            }
        });
        confirmDeletePortfolioDialogFragment.restoreListeners(this, "delete");

        BaseDialogFragment renamePortfolioDialogFragment = BaseDialogFragment.newInstance(RenamePortfolioDialog.class, "");
        renamePortfolioDialogFragment.setOnDismissListener(new CrashDialogInterface.CrashOnDismissListener(this) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                if(((RenamePortfolioDialog)dialog).isComplete) {
                    String newName = ((RenamePortfolioDialog)dialog).user_NEWNAME;

                    if(newName.equals(currentRenamePortfolioName)) {
                        ToastUtil.showToast("portfolio_name_cannot_be_same");
                    }
                    else if(ChartPortfolio.isSaved(newName)) {
                        ToastUtil.showToast("portfolio_name_used");
                    }
                    else {
                        PersistentUserDataStore.getInstance(ChartPortfolio.class).renamePortfolio(currentRenamePortfolioName, newName);
                        updateLayout();
                    }
                }
            }
        });
        renamePortfolioDialogFragment.restoreListeners(this, "rename");

        ArrayList<String> chartPortfolioNames = new ArrayList<>(ChartPortfolio.settings_chart_portfolio_names);
        Collections.sort(chartPortfolioNames, Comparator.comparing(String::toLowerCase));

        for(String chartPortfolioName : chartPortfolioNames) {
            AppCompatButton B = new AppCompatButton(ChartPortfolioViewerActivity.this);
            B.setText(chartPortfolioName);
            B.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_folder_24, 0, 0, 0);
            B.setOnClickListener(new CrashView.CrashOnClickListener(this) {
                @Override
                public void onClickImpl(View view) {
                    Intent intent = new Intent(ChartPortfolioViewerActivity.this, ChartPortfolioExplorerActivity.class);
                    intent.putExtra("ChartPortfolioName",  chartPortfolioName);

                    startActivity(intent);
                    finish();
                }
            });

            AppCompatButton B_DELETE = new AppCompatButton(ChartPortfolioViewerActivity.this);
            B_DELETE.setText("Delete");
            B_DELETE.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_delete_24, 0, 0, 0);
            B_DELETE.setOnClickListener(new CrashView.CrashOnClickListener(this) {
                @Override
                public void onClickImpl(View view) {
                    currentDeletePortfolioName = chartPortfolioName;
                    confirmDeletePortfolioDialogFragment.show(ChartPortfolioViewerActivity.this, "delete");
                }
            });

            AppCompatButton B_RENAME = new AppCompatButton(ChartPortfolioViewerActivity.this);
            B_RENAME.setText("Rename");
            B_RENAME.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_edit_24, 0, 0, 0);
            B_RENAME.setOnClickListener(new CrashView.CrashOnClickListener(this) {
                @Override
                public void onClickImpl(View view) {
                    currentRenamePortfolioName = chartPortfolioName;
                    renamePortfolioDialogFragment.updateArguments(RenamePortfolioDialog.class, chartPortfolioName);
                    renamePortfolioDialogFragment.show(ChartPortfolioViewerActivity.this, "rename");
                }
            });

            TableRow.LayoutParams TRP = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
            TRP.setMargins(80,0,0,0);

            TableRow TR = new TableRow(ChartPortfolioViewerActivity.this);
            TR.addView(B);
            TR.addView(B_DELETE, TRP);
            TR.addView(B_RENAME);
            table.addView(TR);
        }
    }

    @Override
    public void onSaveInstanceStateImpl(@NonNull Bundle bundle) {
        super.onSaveInstanceStateImpl(bundle);
        bundle.putString("currentDeletePortfolioName", currentDeletePortfolioName);
        bundle.putString("currentRenamePortfolioName", currentRenamePortfolioName);
    }

    @Override
    public void onRestoreInstanceStateImpl(Bundle bundle) {
        super.onRestoreInstanceStateImpl(bundle);
        if(bundle != null) {
            currentDeletePortfolioName = bundle.getString("currentDeletePortfolioName");
            currentRenamePortfolioName = bundle.getString("currentRenamePortfolioName");
        }
    }
}
