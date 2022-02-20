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

import com.musicslayer.cryptobuddy.crash.CrashDialogInterface;
import com.musicslayer.cryptobuddy.crash.CrashView;
import com.musicslayer.cryptobuddy.data.persistent.user.AddressPortfolio;
import com.musicslayer.cryptobuddy.data.persistent.user.AddressPortfolioObj;
import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.dialog.ConfirmDeletePortfolioDialog;
import com.musicslayer.cryptobuddy.dialog.CreatePortfolioDialog;
import com.musicslayer.cryptobuddy.dialog.BaseDialogFragment;
import com.musicslayer.cryptobuddy.data.persistent.user.PersistentUserDataStore;
import com.musicslayer.cryptobuddy.util.HelpUtil;
import com.musicslayer.cryptobuddy.util.ToastUtil;

public class AddressPortfolioViewerActivity extends BaseActivity {
    String currentDeletePortfolioName;

    @Override
    public int getAdLayoutViewID() {
        return R.id.address_portfolio_viewer_adLayout;
    }

    @Override
    public void onBackPressedImpl() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    @Override
    public void createLayout(Bundle savedInstanceState) {
        setContentView(R.layout.activity_address_portfolio_viewer);

        Toolbar toolbar = findViewById(R.id.address_portfolio_viewer_toolbar);
        setSupportActionBar(toolbar);

        ImageButton helpButton = findViewById(R.id.address_portfolio_viewer_helpButton);
        helpButton.setOnClickListener(new CrashView.CrashOnClickListener(this) {
            @Override
            public void onClickImpl(View view) {
                HelpUtil.showHelp(AddressPortfolioViewerActivity.this, R.raw.help_address_portfolio_viewer);
            }
        });

        BaseDialogFragment createPortfolioDialogFragment = BaseDialogFragment.newInstance(CreatePortfolioDialog.class);
        createPortfolioDialogFragment.setOnDismissListener(new CrashDialogInterface.CrashOnDismissListener(this) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                if(((CreatePortfolioDialog)dialog).isComplete) {
                    String name = ((CreatePortfolioDialog)dialog).user_NAME;

                    if(AddressPortfolio.isSaved(name)) {
                        ToastUtil.showToast("portfolio_name_used");
                    }
                    else {
                        PersistentUserDataStore.getInstance(AddressPortfolio.class).addPortfolio(new AddressPortfolioObj(name));
                        updateLayout();
                    }
                }
            }
        });
        createPortfolioDialogFragment.restoreListeners(this, "create");

        Button bCreate = findViewById(R.id.address_portfolio_viewer_addButton);
        bCreate.setOnClickListener(new CrashView.CrashOnClickListener(this) {
            @Override
            public void onClickImpl(View view) {
                createPortfolioDialogFragment.show(AddressPortfolioViewerActivity.this, "create");
            }
        });

        updateLayout();
    }

    public void updateLayout() {
        TableLayout table = findViewById(R.id.address_portfolio_viewer_tableLayout);
        table.removeAllViews();

        BaseDialogFragment confirmDeletePortfolioDialogFragment = BaseDialogFragment.newInstance(ConfirmDeletePortfolioDialog.class);
        confirmDeletePortfolioDialogFragment.setOnDismissListener(new CrashDialogInterface.CrashOnDismissListener(this) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                if(((ConfirmDeletePortfolioDialog)dialog).isComplete) {
                    PersistentUserDataStore.getInstance(AddressPortfolio.class).removePortfolio(currentDeletePortfolioName);
                    updateLayout();
                }
            }
        });
        confirmDeletePortfolioDialogFragment.restoreListeners(this, "delete");

        for(String addressPortfolioObjName : AddressPortfolio.settings_address_portfolio_names) {
            AppCompatButton B = new AppCompatButton(AddressPortfolioViewerActivity.this);
            B.setText(addressPortfolioObjName);
            B.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_folder_24, 0, 0, 0);
            B.setOnClickListener(new CrashView.CrashOnClickListener(this) {
                @Override
                public void onClickImpl(View view) {
                    Intent intent = new Intent(AddressPortfolioViewerActivity.this, AddressPortfolioExplorerActivity.class);
                    intent.putExtra("AddressPortfolioName",  addressPortfolioObjName);

                    startActivity(intent);
                    finish();
                }
            });

            AppCompatButton B_DELETE = new AppCompatButton(AddressPortfolioViewerActivity.this);
            B_DELETE.setText("Delete");
            B_DELETE.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_delete_24, 0, 0, 0);
            B_DELETE.setOnClickListener(new CrashView.CrashOnClickListener(this) {
                @Override
                public void onClickImpl(View view) {
                    currentDeletePortfolioName = addressPortfolioObjName;
                    confirmDeletePortfolioDialogFragment.show(AddressPortfolioViewerActivity.this, "delete");
                }
            });

            TableRow.LayoutParams TRP = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
            TRP.setMargins(80,0,0,0);

            TableRow TR = new TableRow(AddressPortfolioViewerActivity.this);
            TR.addView(B);
            TR.addView(B_DELETE, TRP);
            table.addView(TR);
        }
    }

    @Override
    public void onSaveInstanceStateImpl(@NonNull Bundle bundle) {
        bundle.putString("PortfolioName", currentDeletePortfolioName);
    }

    @Override
    public void onRestoreInstanceStateImpl(Bundle bundle) {
        if(bundle != null) {
            currentDeletePortfolioName = bundle.getString("PortfolioName");
        }
    }
}
