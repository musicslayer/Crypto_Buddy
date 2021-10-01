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
import com.musicslayer.cryptobuddy.asset.tokenmanager.TokenManager;
import com.musicslayer.cryptobuddy.dialog.ProgressDialog;
import com.musicslayer.cryptobuddy.dialog.ProgressDialogFragment;
import com.musicslayer.cryptobuddy.persistence.AddressPortfolio;
import com.musicslayer.cryptobuddy.persistence.AddressPortfolioObj;
import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.dialog.ConfirmDeletePortfolioDialog;
import com.musicslayer.cryptobuddy.dialog.CreatePortfolioDialog;
import com.musicslayer.cryptobuddy.dialog.BaseDialogFragment;
import com.musicslayer.cryptobuddy.util.Help;
import com.musicslayer.cryptobuddy.util.Toast;

import java.util.ArrayList;

public class AddressPortfolioViewerActivity extends BaseActivity {
    String currentDeletePortfolioName;

    final ArrayList<CryptoAddress>[] cryptoAddressArrayList = new ArrayList[1];
    final ArrayList<AddressData>[] addressDataArrayList = new ArrayList[1];
    final String[] AddressPortfolioObjName = new String[1];

    public int getAdLayoutViewID() {
        return R.id.address_portfolio_viewer_adLayout;
    }

    @Override
    public void onBackPressed() {
        finish();
        startActivity(new Intent(this, MainActivity.class));
    }

    public void createLayout () {
        setContentView(R.layout.activity_address_portfolio_viewer);

        Toolbar toolbar = findViewById(R.id.address_portfolio_viewer_toolbar);
        setSupportActionBar(toolbar);

        ImageButton helpButton = findViewById(R.id.address_portfolio_viewer_helpButton);
        helpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Help.showHelp(AddressPortfolioViewerActivity.this, R.raw.help_address_portfolio_viewer);
            }
        });

        BaseDialogFragment createPortfolioDialogFragment = BaseDialogFragment.newInstance(CreatePortfolioDialog.class);
        createPortfolioDialogFragment.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if(((CreatePortfolioDialog)dialog).isComplete) {
                    String name = ((CreatePortfolioDialog)dialog).user_NAME;

                    if(AddressPortfolio.isSaved(name)) {
                        Toast.showToast("portfolio_name_used");
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
        bCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createPortfolioDialogFragment.show(AddressPortfolioViewerActivity.this, "create");
            }
        });

        updateLayout();
    }

    public void updateLayout() {
        TableLayout table = findViewById(R.id.address_portfolio_viewer_tableLayout);
        table.removeAllViews();

        BaseDialogFragment confirmDeletePortfolioDialogFragment = BaseDialogFragment.newInstance(ConfirmDeletePortfolioDialog.class);
        confirmDeletePortfolioDialogFragment.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if(((ConfirmDeletePortfolioDialog)dialog).isComplete) {
                    AddressPortfolio.removePortfolio(AddressPortfolioViewerActivity.this, AddressPortfolio.getFromName(currentDeletePortfolioName));
                    updateLayout();
                }
            }
        });
        confirmDeletePortfolioDialogFragment.restoreListeners(this, "delete");

        ProgressDialogFragment progressDialogFragment = ProgressDialogFragment.newInstance(ProgressDialog.class);
        progressDialogFragment.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                for(CryptoAddress cryptoAddress : cryptoAddressArrayList[0]) {
                    if(((ProgressDialog)dialog).isCancelled) { return; }
                    addressDataArrayList[0].add(AddressData.getAddressData(cryptoAddress));
                    TokenManager.saveAll(AddressPortfolioViewerActivity.this, "found");
                }
            }
        });

        progressDialogFragment.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                AddressData.alertUser(addressDataArrayList[0]);

                finish();

                // TODO we should only pass the cryptoaddress here, NOT all the addressdata, which could be super large based on balance/transaction count. Then we can increase the setting limit.
                Intent intent = new Intent(AddressPortfolioViewerActivity.this, AddressPortfolioExplorerActivity.class);
                intent.putExtra("AddressPortfolioName",  AddressPortfolioObjName[0]);
                //intent.putExtra("AddressData_Array",  addressDataArrayList[0]);
                intent.putExtra("AddressData_Array", AddressData.serializeArray(addressDataArrayList[0]));
                AddressPortfolioViewerActivity.this.startActivity(intent);
            }
        });
        progressDialogFragment.restoreListeners(this, "progress");

        for(AddressPortfolioObj addressPortfolioObj : AddressPortfolio.settings_address_portfolio) {
            TableRow TR = new TableRow(AddressPortfolioViewerActivity.this);
            AppCompatButton B = new AppCompatButton(AddressPortfolioViewerActivity.this);
            B.setText(addressPortfolioObj.name);
            B.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_folder_24, 0, 0, 0);
            B.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    cryptoAddressArrayList[0] = addressPortfolioObj.cryptoAddressArrayList;
                    addressDataArrayList[0] = new ArrayList<>();
                    AddressPortfolioObjName[0] = addressPortfolioObj.name;

                    progressDialogFragment.show(AddressPortfolioViewerActivity.this, "progress");
                }
            });

            AppCompatButton B_DELETE = new AppCompatButton(AddressPortfolioViewerActivity.this);
            B_DELETE.setText("Delete");
            B_DELETE.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_delete_24, 0, 0, 0);
            B_DELETE.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    currentDeletePortfolioName = addressPortfolioObj.name;
                    confirmDeletePortfolioDialogFragment.show(AddressPortfolioViewerActivity.this, "delete");
                }
            });

            TableRow.LayoutParams TRP = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
            TRP.setMargins(80,0,0,0);

            TR.addView(B);
            TR.addView(B_DELETE, TRP);
            table.addView(TR);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle bundle) {
        super.onSaveInstanceState(bundle);
        bundle.putString("PortfolioName", currentDeletePortfolioName);

        bundle.putString("cryptoAddressArrayList", CryptoAddress.serializeArray(cryptoAddressArrayList[0]));
        bundle.putString("addressDataArrayList", AddressData.serializeArray(addressDataArrayList[0]));
        bundle.putString("AddressPortfolioObjName", AddressPortfolioObjName[0]);
    }

    @Override
    public void onRestoreInstanceState(Bundle bundle) {
        if(bundle != null) {
            currentDeletePortfolioName = bundle.getString("PortfolioName");

            cryptoAddressArrayList[0] = CryptoAddress.deserializeArray(bundle.getString("cryptoAddressArrayList"));
            addressDataArrayList[0] = AddressData.deserializeArray(bundle.getString("addressDataArrayList"));
            AddressPortfolioObjName[0] = bundle.getString("AddressPortfolioObjName");
        }

        super.onRestoreInstanceState(bundle);
    }
}
