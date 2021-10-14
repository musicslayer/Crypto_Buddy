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

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class AddressExplorerActivity extends BaseActivity {
    WeakReference<BaseDialogFragment> confirmBackDialogFragment_w;
    WeakReference<ProgressDialogFragment> progressDialogFragment_w;

    AddressTable table;
    ArrayList<AddressData> addressDataArrayList = new ArrayList<>();

    public int getAdLayoutViewID() {
        return R.id.address_explorer_adLayout;
    }

    @Override
    public void onBackPressedImpl() {
        confirmBackDialogFragment_w.get().show(AddressExplorerActivity.this, "back");
    }

    public void createLayout () {
        setContentView(R.layout.activity_address_explorer);

        confirmBackDialogFragment_w = new WeakReference<>(BaseDialogFragment.newInstance(ConfirmBackDialog.class));
        confirmBackDialogFragment_w.get().setOnDismissListener(new CrashDialogInterface.CrashOnDismissListener(this) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                if(((ConfirmBackDialog)dialog).isComplete) {
                    startActivity(new Intent(AddressExplorerActivity.this, MainActivity.class));
                    finish();
                }
            }
        });
        confirmBackDialogFragment_w.get().restoreListeners(this, "back");

        AddressData addressData = Serialization.deserialize(getIntent().getStringExtra("AddressData"), AddressData.class);
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
        infoButton.setOnClickListener(new CrashView.CrashOnClickListener(this) {
            @Override
            public void onClickImpl(View view) {
                InfoUtil.showInfo(AddressExplorerActivity.this, addressDataArrayList);
            }
        });

        if(!InfoUtil.hasInfo(addressDataArrayList)) {
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

        progressDialogFragment_w = new WeakReference<>(ProgressDialogFragment.newInstance(ProgressDialog.class));
        progressDialogFragment_w.get().setOnShowListener(new CrashDialogInterface.CrashOnShowListener(this) {
            @Override
            public void onShowImpl(DialogInterface dialog) {
                AddressData newAddressData = AddressData.getAllData(addressDataArrayList.get(0).cryptoAddress);

                // Save found tokens, potentially from multiple TokenManagers.
                TokenManagerList.saveAllData(AddressExplorerActivity.this);

                ProgressDialogFragment.setValue(Serialization.serialize(newAddressData));
            }
        });
        progressDialogFragment_w.get().setOnDismissListener(new CrashDialogInterface.CrashOnDismissListener(this) {
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
        progressDialogFragment_w.get().restoreListeners(this, "progress");

        FloatingActionButton fab_refresh = findViewById(R.id.address_explorer_refreshButton);
        fab_refresh.setOnClickListener(new CrashView.CrashOnClickListener(this) {
            @Override
            public void onClickImpl(View view) {
                progressDialogFragment_w.get().show(AddressExplorerActivity.this, "progress");
            }
        });

        FloatingActionButton fab_qrcode = findViewById(R.id.address_explorer_qrCodeButton);
        fab_qrcode.setOnClickListener(new CrashView.CrashOnClickListener(this) {
            @Override
            public void onClickImpl(View view) {
                BaseDialogFragment.newInstance(AddressQRCodeDialog.class, addressDataArrayList).show(AddressExplorerActivity.this, "qrcode");
            }
        });

        updateLayout();
    }

    public void updateLayout() {
        table.resetTable();

        TextView T = findViewById(R.id.address_explorer_infoTextView);
        T.setText("Address = " + addressDataArrayList.get(0).cryptoAddress.toString());

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
    }

    @Override
    public void onRestoreInstanceStateImpl(Bundle bundle) {
        if(bundle != null) {
            addressDataArrayList = Serialization.deserializeArrayList(bundle.getString("addressDataArrayList"), AddressData.class);
            updateLayout();
        }
    }
}