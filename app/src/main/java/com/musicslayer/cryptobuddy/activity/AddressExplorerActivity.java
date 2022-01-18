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
import com.musicslayer.cryptobuddy.dialog.DownloadAddressDataDialog;
import com.musicslayer.cryptobuddy.dialog.ProgressDialog;
import com.musicslayer.cryptobuddy.dialog.ProgressDialogFragment;
import com.musicslayer.cryptobuddy.dialog.ReportFeedbackDialog;
import com.musicslayer.cryptobuddy.dialog.TotalDialog;
import com.musicslayer.cryptobuddy.persistence.Purchases;
import com.musicslayer.cryptobuddy.persistence.TokenManagerList;
import com.musicslayer.cryptobuddy.state.ActivityStateObj;
import com.musicslayer.cryptobuddy.state.TableStateObj;
import com.musicslayer.cryptobuddy.util.HashMapUtil;
import com.musicslayer.cryptobuddy.util.HelpUtil;
import com.musicslayer.cryptobuddy.util.InfoUtil;
import com.musicslayer.cryptobuddy.serialize.Serialization;
import com.musicslayer.cryptobuddy.util.ToastUtil;
import com.musicslayer.cryptobuddy.view.table.AddressTable;

import java.util.ArrayList;

public class AddressExplorerActivity extends BaseActivity {
    public BaseDialogFragment confirmBackDialogFragment;

    AddressTable table;
    public final static ActivityStateObj[] activityStateObj = new ActivityStateObj[1];

    ArrayList<CryptoAddress> cryptoAddressArrayList = new ArrayList<>();

    public ArrayList<Boolean> includeBalances;
    public ArrayList<Boolean> includeTransactions;

    public int getAdLayoutViewID() {
        return R.id.address_explorer_adLayout;
    }

    @Override
    public void onBackPressedImpl() {
        confirmBackDialogFragment.show(AddressExplorerActivity.this, "back");
    }

    public void createLayout () {
        setContentView(R.layout.activity_address_explorer);

        if(isFirstCreate) {
            activityStateObj[0] = new ActivityStateObj();
        }

        confirmBackDialogFragment = BaseDialogFragment.newInstance(ConfirmBackDialog.class);
        confirmBackDialogFragment.setOnDismissListener(new CrashDialogInterface.CrashOnDismissListener(this) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                if(((ConfirmBackDialog)dialog).isComplete) {
                    // Clean State Objects.
                    activityStateObj[0] = null;
                    table.tableStateObj[0] = null;

                    startActivity(new Intent(AddressExplorerActivity.this, MainActivity.class));
                    finish();
                }
            }
        });
        confirmBackDialogFragment.restoreListeners(this, "back");

        CryptoAddress cryptoAddress = getIntent().getParcelableExtra("CryptoAddress");
        cryptoAddressArrayList.add(cryptoAddress);
        if(isFirstCreate) {
            HashMapUtil.putValueInMap(activityStateObj[0].addressDataMap, cryptoAddress, AddressData.getNoData(cryptoAddress));
        }

        boolean includeTokens = cryptoAddress.includeTokens;

        TextView T_INFO = findViewById(R.id.address_explorer_infoTextView);
        T_INFO.setText("Address = " + cryptoAddressArrayList.get(0).toString());

        TextView T_MESSAGE = findViewById(R.id.address_explorer_messageTextView);
        if(!Purchases.isUnlockTokensPurchased() && includeTokens) {
            T_MESSAGE.setVisibility(View.VISIBLE);
        }
        else {
            T_MESSAGE.setVisibility(View.GONE);
        }

        Toolbar toolbar = findViewById(R.id.address_explorer_toolbar);
        setSupportActionBar(toolbar);

        ImageButton problemInfoButton = findViewById(R.id.address_explorer_problemInfoButton);
        problemInfoButton.setOnClickListener(new CrashView.CrashOnClickListener(this) {
            @Override
            public void onClickImpl(View view) {
                InfoUtil.showInfo_CryptoAddress(AddressExplorerActivity.this, cryptoAddressArrayList);
            }
        });

        if(!InfoUtil.hasInfo_CryptoAddress(cryptoAddressArrayList)) {
            problemInfoButton.setVisibility(View.GONE);
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
        if(isFirstCreate) {
            table.tableStateObj[0] = new TableStateObj();
            table.tableStateObj[0].table = table;
        }

        FloatingActionButton fab_info = findViewById(R.id.address_explorer_infoButton);
        fab_info.setOnClickListener(new CrashView.CrashOnClickListener(this) {
            @Override
            public void onClickImpl(View view) {
                BaseDialogFragment.newInstance(AddressInfoDialog.class, cryptoAddressArrayList).show(AddressExplorerActivity.this, "info");
            }
        });

        FloatingActionButton fab_total = findViewById(R.id.address_explorer_totalButton);
        fab_total.setOnClickListener(new CrashView.CrashOnClickListener(this) {
            @Override
            public void onClickImpl(View view) {
                //BaseDialogFragment.newInstance(TotalDialog.class, table.getFilteredMaskedTransactionArrayList()).show(AddressExplorerActivity.this, "total");
                BaseDialogFragment.newInstance(TotalDialog.class).show(AddressExplorerActivity.this, "total");
            }
        });

        FloatingActionButton fab_qrcode = findViewById(R.id.address_explorer_qrCodeButton);
        fab_qrcode.setOnClickListener(new CrashView.CrashOnClickListener(this) {
            @Override
            public void onClickImpl(View view) {
                BaseDialogFragment.newInstance(AddressQRCodeDialog.class, cryptoAddressArrayList).show(AddressExplorerActivity.this, "qrcode");
            }
        });

        ProgressDialogFragment progressDialogFragment = ProgressDialogFragment.newInstance(ProgressDialog.class);
        progressDialogFragment.setOnShowListener(new CrashDialogInterface.CrashOnShowListener(this) {
            @Override
            public void onShowImpl(DialogInterface dialog) {
                CryptoAddress cryptoAddress = cryptoAddressArrayList.get(0);

                AddressData newAddressData;

                if(includeBalances.get(0) && includeTransactions.get(0)) {
                    newAddressData = AddressData.getAllData(cryptoAddress);
                }
                else if(includeBalances.get(0)) {
                    newAddressData = AddressData.getCurrentBalanceData(cryptoAddress);
                }
                else if(includeTransactions.get(0)) {
                    newAddressData = AddressData.getTransactionsData(cryptoAddress);
                }
                else {
                    newAddressData = AddressData.getNoData(cryptoAddress);
                }

                // Save found tokens, potentially from multiple TokenManagers.
                TokenManagerList.saveAllData(AddressExplorerActivity.this);

                ProgressDialogFragment.setValue(Serialization.serialize(newAddressData));
            }
        });
        progressDialogFragment.setOnDismissListener(new CrashDialogInterface.CrashOnDismissListener(this) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                AddressData newAddressData = Serialization.deserialize(ProgressDialogFragment.getValue(), AddressData.class);

                boolean isComplete;
                if(includeBalances.get(0) && includeTransactions.get(0)) {
                    isComplete = newAddressData.isComplete();
                }
                else if(includeBalances.get(0)) {
                    isComplete = newAddressData.isCurrentBalanceComplete();
                }
                else if(includeTransactions.get(0)) {
                    isComplete = newAddressData.isTransactionsComplete();
                }
                else {
                    isComplete = true;
                }

                if(!isComplete) {
                    ToastUtil.showToast(AddressExplorerActivity.this,"incomplete_address_data");
                }

                CryptoAddress cryptoAddress = cryptoAddressArrayList.get(0);
                AddressData oldAddressData = HashMapUtil.getValueFromMap(activityStateObj[0].addressDataMap, cryptoAddress);
                AddressData mergedAddressData = AddressData.merge(oldAddressData, newAddressData);
                HashMapUtil.putValueInMap(activityStateObj[0].addressDataMap, cryptoAddress, mergedAddressData);

                updateLayout();
                ToastUtil.showToast(AddressExplorerActivity.this,"address_data_downloaded");
            }
        });
        progressDialogFragment.restoreListeners(this, "progress");

        BaseDialogFragment downloadDialogFragment = BaseDialogFragment.newInstance(DownloadAddressDataDialog.class, cryptoAddressArrayList);
        downloadDialogFragment.setOnDismissListener(new CrashDialogInterface.CrashOnDismissListener(this) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                if(((DownloadAddressDataDialog)dialog).isComplete) {
                    includeBalances = ((DownloadAddressDataDialog)dialog).user_BALANCES;
                    includeTransactions = ((DownloadAddressDataDialog)dialog).user_TRANSACTIONS;
                    progressDialogFragment.show(AddressExplorerActivity.this, "progress");
                }
            }
        });
        downloadDialogFragment.restoreListeners(this, "download");

        AppCompatButton downloadDataButton = findViewById(R.id.address_explorer_downloadDataButton);
        downloadDataButton.setOnClickListener(new CrashView.CrashOnClickListener(this) {
            @Override
            public void onClickImpl(View view) {
                downloadDialogFragment.show(AddressExplorerActivity.this, "download");
            }
        });
    }

    public void updateLayout() {
        table.resetTable();
        table.addRowsFromAddressDataArray(new ArrayList<>(activityStateObj[0].addressDataMap.values()));
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
            String type = "Address";
            BaseDialogFragment.newInstance(ReportFeedbackDialog.class, type).show(AddressExplorerActivity.this, "feedback");
            return true;
        }

        return false;
    }

    @Override
    public void onSaveInstanceStateImpl(@NonNull Bundle bundle) {
        bundle.putSerializable("includeBalances", includeBalances);
        bundle.putSerializable("includeTransactions", includeTransactions);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onRestoreInstanceStateImpl(Bundle bundle) {
        if(bundle != null) {
            includeBalances = (ArrayList<Boolean>)bundle.getSerializable("includeBalances");
            includeTransactions = (ArrayList<Boolean>)bundle.getSerializable("includeTransactions");
        }
    }
}