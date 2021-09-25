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
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.musicslayer.cryptobuddy.api.address.AddressData;
import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.asset.tokenmanager.TokenManager;
import com.musicslayer.cryptobuddy.dialog.AddressInfoDialog;
import com.musicslayer.cryptobuddy.dialog.AddressQRCodeDialog;
import com.musicslayer.cryptobuddy.dialog.ConfirmBackDialog;
import com.musicslayer.cryptobuddy.dialog.CryptoConverterDialog;
import com.musicslayer.cryptobuddy.dialog.CryptoPricesDialog;
import com.musicslayer.cryptobuddy.dialog.BaseDialogFragment;
import com.musicslayer.cryptobuddy.dialog.ProgressDialog;
import com.musicslayer.cryptobuddy.dialog.ProgressDialogFragment;
import com.musicslayer.cryptobuddy.dialog.ReportFeedbackDialog;
import com.musicslayer.cryptobuddy.dialog.TotalDialog;
import com.musicslayer.cryptobuddy.persistence.Purchases;
import com.musicslayer.cryptobuddy.util.Help;
import com.musicslayer.cryptobuddy.util.Info;
import com.musicslayer.cryptobuddy.util.Toast;
import com.musicslayer.cryptobuddy.view.table.AddressTable;

import java.util.ArrayList;

public class AddressExplorerActivity extends BaseActivity {
    public BaseDialogFragment confirmBackDialogFragment;

    AddressTable table;
    ArrayList<AddressData> addressDataArrayList = new ArrayList<>();

    final static AddressData[] newAddressData = new AddressData[1];

    public int getAdLayoutViewID() {
        return R.id.address_explorer_adLayout;
    }

    @Override
    public void onBackPressed() {
        confirmBackDialogFragment.show(AddressExplorerActivity.this, "back");
    }

    public void createLayout () {
        setContentView(R.layout.activity_address_explorer);

        confirmBackDialogFragment = BaseDialogFragment.newInstance(ConfirmBackDialog.class);
        confirmBackDialogFragment.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if(((ConfirmBackDialog)dialog).isComplete) {
                    finish();
                    startActivity(new Intent(AddressExplorerActivity.this, MainActivity.class));
                }
            }
        });
        confirmBackDialogFragment.restoreListeners(this, "back");

        //AddressData addressData = (AddressData)getIntent().getSerializableExtra("AddressData");
        AddressData addressData = AddressData.deserialize(getIntent().getStringExtra("AddressData"));
        addressDataArrayList.add(addressData);

        boolean includeTokens = addressData.cryptoAddress.includeTokens;

        TextView T = findViewById(R.id.address_explorer_messageTextView);
        if(!Purchases.isUnlockTokensPurchased && includeTokens) {
            T.setVisibility(View.VISIBLE);
        }
        else {
            T.setVisibility(View.GONE);
        }

        Toolbar toolbar = findViewById(R.id.address_explorer_toolbar);
        setSupportActionBar(toolbar);

        ImageButton infoButton = findViewById(R.id.address_explorer_cryptoInfoButton);
        infoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Info.showInfo(AddressExplorerActivity.this, addressDataArrayList);
            }
        });

        if(!Info.hasInfo(addressDataArrayList)) {
            infoButton.setVisibility(View.GONE);
        }

        ImageButton helpButton = findViewById(R.id.address_explorer_helpButton);
        helpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Help.showHelp(AddressExplorerActivity.this, R.raw.help_address_explorer);
            }
        });

        table = findViewById(R.id.address_explorer_table);
        table.pageView = findViewById(R.id.address_explorer_tablePageView);
        table.pageView.setTable(table);
        table.pageView.updateLayout();

        FloatingActionButton fab_info = findViewById(R.id.address_explorer_infoButton);
        fab_info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BaseDialogFragment.newInstance(AddressInfoDialog.class, addressDataArrayList).show(AddressExplorerActivity.this, "info");
            }
        });

        FloatingActionButton fab_total = findViewById(R.id.address_explorer_totalButton);
        fab_total.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BaseDialogFragment.newInstance(TotalDialog.class, table.getFilteredMaskedTransactionArrayList()).show(AddressExplorerActivity.this, "total");
            }
        });

        ProgressDialogFragment progressDialogFragment = ProgressDialogFragment.newInstance(ProgressDialog.class);
        progressDialogFragment.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                newAddressData[0] = AddressData.getAddressData(addressDataArrayList.get(0).cryptoAddress);
                TokenManager.saveAll(AddressExplorerActivity.this, "found");
            }
        });
        progressDialogFragment.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                newAddressData[0].alertUser();

                addressDataArrayList.clear();
                addressDataArrayList.add(newAddressData[0]);
                updateLayout();
                Toast.showToast("refresh");
            }
        });
        progressDialogFragment.restoreListeners(this, "progress");

        FloatingActionButton fab_refresh = findViewById(R.id.address_explorer_refreshButton);
        fab_refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressDialogFragment.show(AddressExplorerActivity.this, "progress");
            }
        });

        FloatingActionButton fab_qrcode = findViewById(R.id.address_explorer_qrCodeButton);
        fab_qrcode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BaseDialogFragment.newInstance(AddressQRCodeDialog.class, addressDataArrayList).show(AddressExplorerActivity.this, "qrcode");
            }
        });

        updateLayout();
    }

    public void updateLayout() {
        table.resetTable();

        TextView T = findViewById(R.id.address_explorer_infoTextView);
        T.setText("Address = " + addressDataArrayList.get(0).cryptoAddress.toString());

        table.addRowsFromAddressDataArray(addressDataArrayList);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 1, 100, "Prices");
        menu.add(0, 2, 200, "Converter");
        menu.add(0, 3, 300, "Report Feedback");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == 1) {
            BaseDialogFragment.newInstance(CryptoPricesDialog.class).show(AddressExplorerActivity.this, "price");
            return true;
        }
        else if (id == 2) {
            BaseDialogFragment.newInstance(CryptoConverterDialog.class).show(AddressExplorerActivity.this, "converter");
            return true;
        }
        else if (id == 3) {
            BaseDialogFragment.newInstance(ReportFeedbackDialog.class).show(AddressExplorerActivity.this, "feedback");
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle bundle) {
        super.onSaveInstanceState(bundle);
        bundle.putSerializable("addressDataArrayList", addressDataArrayList);
    }

    @Override
    public void onRestoreInstanceState(Bundle bundle) {
        if(bundle != null) {
            addressDataArrayList = (ArrayList<AddressData>)bundle.getSerializable("addressDataArrayList");
            updateLayout();
        }

        super.onRestoreInstanceState(bundle);
    }
}