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
import com.musicslayer.cryptobuddy.api.address.AddressData;
import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.api.address.CryptoAddress;
import com.musicslayer.cryptobuddy.crash.CrashDialogInterface;
import com.musicslayer.cryptobuddy.crash.CrashView;
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
import com.musicslayer.cryptobuddy.persistence.TokenManagerList;
import com.musicslayer.cryptobuddy.util.HelpUtil;
import com.musicslayer.cryptobuddy.util.InfoUtil;
import com.musicslayer.cryptobuddy.serialize.Serialization;
import com.musicslayer.cryptobuddy.util.ToastUtil;
import com.musicslayer.cryptobuddy.view.table.AddressTable;

import java.util.ArrayList;

public class AddressExplorerActivity extends BaseActivity {
    public BaseDialogFragment confirmBackDialogFragment;

    AddressTable table;
    ArrayList<AddressData> addressDataArrayList = new ArrayList<>();
    ArrayList<CryptoAddress> cryptoAddressArrayList = new ArrayList<>();

    // Whether the "Download Data" button was pressed.
    public boolean isPressed = false;

    public int getAdLayoutViewID() {
        return R.id.address_explorer_adLayout;
    }

    @Override
    public void onBackPressedImpl() {
        confirmBackDialogFragment.show(AddressExplorerActivity.this, "back");
    }

    public void createLayout () {
        setContentView(R.layout.activity_address_explorer);

        confirmBackDialogFragment = BaseDialogFragment.newInstance(ConfirmBackDialog.class);
        confirmBackDialogFragment.setOnDismissListener(new CrashDialogInterface.CrashOnDismissListener(this) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                if(((ConfirmBackDialog)dialog).isComplete) {
                    startActivity(new Intent(AddressExplorerActivity.this, MainActivity.class));
                    finish();
                }
            }
        });
        confirmBackDialogFragment.restoreListeners(this, "back");

        CryptoAddress cryptoAddress = Serialization.deserialize(getIntent().getStringExtra("CryptoAddress"), CryptoAddress.class);
        cryptoAddressArrayList.add(cryptoAddress);

        boolean includeTokens = cryptoAddress.includeTokens;

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
        infoButton.setOnClickListener(new CrashView.CrashOnClickListener(this) {
            @Override
            public void onClickImpl(View view) {
                InfoUtil.showInfo(AddressExplorerActivity.this, cryptoAddressArrayList);
            }
        });

        if(!InfoUtil.hasInfo(cryptoAddressArrayList)) {
            infoButton.setVisibility(View.GONE);
        }

        ImageButton helpButton = findViewById(R.id.address_explorer_helpButton);
        helpButton.setOnClickListener(new CrashView.CrashOnClickListener(this) {
            @Override
            public void onClickImpl(View view) {
                HelpUtil.showHelp(AddressExplorerActivity.this, R.raw.help_address_explorer);
            }
        });

        table = findViewById(R.id.address_explorer_table);
        table.pageView = findViewById(R.id.address_explorer_tablePageView);
        table.pageView.setTable(table);
        table.pageView.updateLayout();

        FloatingActionButton fab_info = findViewById(R.id.address_explorer_infoButton);
        fab_info.setOnClickListener(new CrashView.CrashOnClickListener(this) {
            @Override
            public void onClickImpl(View view) {
                BaseDialogFragment.newInstance(AddressInfoDialog.class, addressDataArrayList).show(AddressExplorerActivity.this, "info");
            }
        });

        FloatingActionButton fab_total = findViewById(R.id.address_explorer_totalButton);
        fab_total.setOnClickListener(new CrashView.CrashOnClickListener(this) {
            @Override
            public void onClickImpl(View view) {
                BaseDialogFragment.newInstance(TotalDialog.class, table.getFilteredMaskedTransactionArrayList()).show(AddressExplorerActivity.this, "total");
            }
        });

        ProgressDialogFragment progressDialogFragment = ProgressDialogFragment.newInstance(ProgressDialog.class);
        progressDialogFragment.setOnShowListener(new CrashDialogInterface.CrashOnShowListener(this) {
            @Override
            public void onShowImpl(DialogInterface dialog) {
                AddressData newAddressData = AddressData.getAllData(cryptoAddressArrayList.get(0));

                // Save found tokens, potentially from multiple TokenManagers.
                TokenManagerList.saveAllData(AddressExplorerActivity.this);

                ProgressDialogFragment.setValue(Serialization.serialize(newAddressData));
            }
        });
        progressDialogFragment.setOnDismissListener(new CrashDialogInterface.CrashOnDismissListener(this) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                AddressData newAddressData = Serialization.deserialize(ProgressDialogFragment.getValue(), AddressData.class);

                if(!newAddressData.isComplete()) {
                    ToastUtil.showToast(AddressExplorerActivity.this,"no_address_data");
                }

                addressDataArrayList.clear();
                addressDataArrayList.add(newAddressData);
                updateLayout();
                ToastUtil.showToast(AddressExplorerActivity.this,"refresh");
            }
        });
        progressDialogFragment.restoreListeners(this, "progress");

        FloatingActionButton fab_refresh = findViewById(R.id.address_explorer_refreshButton);
        fab_refresh.setOnClickListener(new CrashView.CrashOnClickListener(this) {
            @Override
            public void onClickImpl(View view) {
                progressDialogFragment.show(AddressExplorerActivity.this, "progress");
            }
        });

        FloatingActionButton fab_qrcode = findViewById(R.id.address_explorer_qrCodeButton);
        fab_qrcode.setOnClickListener(new CrashView.CrashOnClickListener(this) {
            @Override
            public void onClickImpl(View view) {
                BaseDialogFragment.newInstance(AddressQRCodeDialog.class, cryptoAddressArrayList).show(AddressExplorerActivity.this, "qrcode");
            }
        });

        // Download data is same as refresh, but we will change the visibility of buttons after.
        AppCompatButton downloadDataButton = findViewById(R.id.address_explorer_downloadDataButton);
        downloadDataButton.setOnClickListener(new CrashView.CrashOnClickListener(this) {
            @Override
            public void onClickImpl(View view) {
                progressDialogFragment.show(AddressExplorerActivity.this, "progress");

                // Even if this fails, or the user backs out, we still consider it pressed.
                isPressed = true;

                downloadDataButton.setVisibility(View.GONE);

                fab_info.setVisibility(View.VISIBLE);
                fab_total.setVisibility(View.VISIBLE);
                fab_refresh.setVisibility(View.VISIBLE);
                fab_qrcode.setVisibility(View.VISIBLE);
            }
        });

        updateLayout();
    }

    public void updateLayout() {
        table.resetTable();

        AppCompatButton downloadDataButton = findViewById(R.id.address_explorer_downloadDataButton);
        FloatingActionButton fab_info = findViewById(R.id.address_explorer_infoButton);
        FloatingActionButton fab_total = findViewById(R.id.address_explorer_totalButton);
        FloatingActionButton fab_refresh = findViewById(R.id.address_explorer_refreshButton);
        FloatingActionButton fab_qrcode = findViewById(R.id.address_explorer_qrCodeButton);

        if(isPressed) {
            downloadDataButton.setVisibility(View.GONE);
            fab_info.setVisibility(View.VISIBLE);
            fab_total.setVisibility(View.VISIBLE);
            fab_refresh.setVisibility(View.VISIBLE);
            fab_qrcode.setVisibility(View.VISIBLE);
        }
        else {
            downloadDataButton.setVisibility(View.VISIBLE);
            fab_info.setVisibility(View.GONE);
            fab_total.setVisibility(View.GONE);
            fab_refresh.setVisibility(View.GONE);
            fab_qrcode.setVisibility(View.GONE);
        }

        TextView T = findViewById(R.id.address_explorer_infoTextView);
        T.setText("Address = " + cryptoAddressArrayList.get(0).toString());

        table.addRowsFromAddressDataArray(this, addressDataArrayList);
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
            BaseDialogFragment.newInstance(CryptoPricesDialog.class).show(AddressExplorerActivity.this, "price");
            return true;
        }
        else if (id == 2) {
            BaseDialogFragment.newInstance(CryptoConverterDialog.class).show(AddressExplorerActivity.this, "converter");
            return true;
        }
        else if (id == 3) {
            AddressTable table = findViewById(R.id.address_explorer_table);
            String type = "Address";
            String info = table.getInfo();
            BaseDialogFragment.newInstance(ReportFeedbackDialog.class, type, info).show(AddressExplorerActivity.this, "feedback");
            return true;
        }

        return false;
    }

    @Override
    public void onSaveInstanceStateImpl(@NonNull Bundle bundle) {
        bundle.putString("addressDataArrayList", Serialization.serializeArrayList(addressDataArrayList));
        bundle.putString("isPressed", Serialization.boolean_serialize(isPressed));
    }

    @Override
    public void onRestoreInstanceStateImpl(Bundle bundle) {
        if(bundle != null) {
            addressDataArrayList = Serialization.deserializeArrayList(bundle.getString("addressDataArrayList"), AddressData.class);
            isPressed = Serialization.boolean_deserialize(bundle.getString("isPressed"));

            updateLayout();
        }
    }
}