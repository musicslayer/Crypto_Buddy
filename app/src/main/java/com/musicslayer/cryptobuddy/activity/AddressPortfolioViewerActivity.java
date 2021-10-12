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

import com.musicslayer.cryptobuddy.api.address.AddressData;
import com.musicslayer.cryptobuddy.api.address.CryptoAddress;
import com.musicslayer.cryptobuddy.crash.CrashDialogInterface;
import com.musicslayer.cryptobuddy.crash.CrashView;
import com.musicslayer.cryptobuddy.dialog.ProgressDialog;
import com.musicslayer.cryptobuddy.dialog.ProgressDialogFragment;
import com.musicslayer.cryptobuddy.persistence.AddressPortfolio;
import com.musicslayer.cryptobuddy.persistence.AddressPortfolioObj;
import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.dialog.ConfirmDeletePortfolioDialog;
import com.musicslayer.cryptobuddy.dialog.CreatePortfolioDialog;
import com.musicslayer.cryptobuddy.dialog.BaseDialogFragment;
import com.musicslayer.cryptobuddy.persistence.TokenManagerList;
import com.musicslayer.cryptobuddy.util.HelpUtil;
import com.musicslayer.cryptobuddy.serialize.Serialization;
import com.musicslayer.cryptobuddy.util.ToastUtil;

import java.util.ArrayList;

// TODO Have another screen which lists addresses in the portfolio.
// Users can select one or more that they wish to analyze?

public class AddressPortfolioViewerActivity extends BaseActivity {
    String currentDeletePortfolioName;

    final ArrayList<CryptoAddress>[] cryptoAddressArrayList = new ArrayList[1];
    final String[] AddressPortfolioObjName = new String[1];

    public int getAdLayoutViewID() {
        return R.id.address_portfolio_viewer_adLayout;
    }

    @Override
    public void onBackPressedImpl() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    public void createLayout () {
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
                        ToastUtil.showToast(AddressPortfolioViewerActivity.this,"portfolio_name_used");
                    }
                    else {
                        AddressPortfolio.addPortfolio(AddressPortfolioViewerActivity.this, new AddressPortfolioObj(name));
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
                    AddressPortfolio.removePortfolio(AddressPortfolioViewerActivity.this, AddressPortfolio.getFromName(currentDeletePortfolioName));
                    updateLayout();
                }
            }
        });
        confirmDeletePortfolioDialogFragment.restoreListeners(this, "delete");

        ProgressDialogFragment progressDialogFragment = ProgressDialogFragment.newInstance(ProgressDialog.class);
        progressDialogFragment.setOnShowListener(new CrashDialogInterface.CrashOnShowListener(this) {
            @Override
            public void onShowImpl(DialogInterface dialog) {
                ArrayList<AddressData> addressDataArrayList = new ArrayList<>();

                for(CryptoAddress cryptoAddress : cryptoAddressArrayList[0]) {
                    if(ProgressDialogFragment.isCancelled()) { return; }
                    addressDataArrayList.add(AddressData.getAddressData(cryptoAddress));

                    // Save found tokens, potentially from multiple TokenManagers.
                    TokenManagerList.saveAllData(AddressPortfolioViewerActivity.this);
                }

                ProgressDialogFragment.setValue(Serialization.serializeArrayList(addressDataArrayList));
            }
        });

        progressDialogFragment.setOnDismissListener(new CrashDialogInterface.CrashOnDismissListener(this) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                ArrayList<AddressData> addressDataArrayList = Serialization.deserializeArrayList(ProgressDialogFragment.getValue(), AddressData.class);

                for(AddressData addressData : addressDataArrayList) {
                    if(!addressData.isComplete()) {
                        // Only alert once. Others would be redundant.
                        ToastUtil.showToast(AddressPortfolioViewerActivity.this,"no_address_data");
                        break;
                    }
                }

                // TODO we should only pass the cryptoaddress here, NOT all the addressdata, which could be super large based on balance/transaction count. Then we can increase the setting limit.
                // Note that serialization does shrink this somewhat.
                Intent intent = new Intent(AddressPortfolioViewerActivity.this, AddressPortfolioExplorerActivity.class);
                intent.putExtra("AddressPortfolioName",  AddressPortfolioObjName[0]);
                intent.putExtra("AddressData_Array", Serialization.serializeArrayList(addressDataArrayList));
                AddressPortfolioViewerActivity.this.startActivity(intent);

                finish();
            }
        });
        progressDialogFragment.restoreListeners(this, "progress");

        for(AddressPortfolioObj addressPortfolioObj : AddressPortfolio.settings_address_portfolio) {
            AppCompatButton B = new AppCompatButton(AddressPortfolioViewerActivity.this);
            B.setText(addressPortfolioObj.name);
            B.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_folder_24, 0, 0, 0);
            B.setOnClickListener(new CrashView.CrashOnClickListener(this) {
                @Override
                public void onClickImpl(View view) {
                    cryptoAddressArrayList[0] = addressPortfolioObj.cryptoAddressArrayList;
                    AddressPortfolioObjName[0] = addressPortfolioObj.name;

                    progressDialogFragment.show(AddressPortfolioViewerActivity.this, "progress");
                }
            });

            AppCompatButton B_DELETE = new AppCompatButton(AddressPortfolioViewerActivity.this);
            B_DELETE.setText("Delete");
            B_DELETE.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_delete_24, 0, 0, 0);
            B_DELETE.setOnClickListener(new CrashView.CrashOnClickListener(this) {
                @Override
                public void onClickImpl(View view) {
                    currentDeletePortfolioName = addressPortfolioObj.name;
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
        bundle.putString("cryptoAddressArrayList", Serialization.serializeArrayList(cryptoAddressArrayList[0]));
        bundle.putString("AddressPortfolioObjName", AddressPortfolioObjName[0]);
    }

    @Override
    public void onRestoreInstanceStateImpl(Bundle bundle) {
        if(bundle != null) {
            currentDeletePortfolioName = bundle.getString("PortfolioName");
            cryptoAddressArrayList[0] = Serialization.deserializeArrayList(bundle.getString("cryptoAddressArrayList"), CryptoAddress.class);
            AddressPortfolioObjName[0] = bundle.getString("AddressPortfolioObjName");
        }
    }
}
