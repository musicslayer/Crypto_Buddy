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
import com.musicslayer.cryptobuddy.api.address.CryptoAddress;
import com.musicslayer.cryptobuddy.crash.CrashDialogInterface;
import com.musicslayer.cryptobuddy.crash.CrashView;
import com.musicslayer.cryptobuddy.dialog.AddressFilterDialog;
import com.musicslayer.cryptobuddy.dialog.AddressQRCodeDialog;
import com.musicslayer.cryptobuddy.dialog.ConfirmBackDialog;
import com.musicslayer.cryptobuddy.dialog.CryptoConverterDialog;
import com.musicslayer.cryptobuddy.dialog.DownloadDataDialog;
import com.musicslayer.cryptobuddy.dialog.ProgressDialog;
import com.musicslayer.cryptobuddy.dialog.ProgressDialogFragment;
import com.musicslayer.cryptobuddy.dialog.ReportFeedbackDialog;
import com.musicslayer.cryptobuddy.persistence.AddressPortfolio;
import com.musicslayer.cryptobuddy.persistence.AddressPortfolioObj;
import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.dialog.AddressInfoDialog;
import com.musicslayer.cryptobuddy.dialog.ChooseAddressDialog;
import com.musicslayer.cryptobuddy.dialog.CryptoPricesDialog;
import com.musicslayer.cryptobuddy.dialog.BaseDialogFragment;
import com.musicslayer.cryptobuddy.dialog.TotalDialog;
import com.musicslayer.cryptobuddy.persistence.Purchases;
import com.musicslayer.cryptobuddy.persistence.TokenManagerList;
import com.musicslayer.cryptobuddy.util.HelpUtil;
import com.musicslayer.cryptobuddy.util.InfoUtil;
import com.musicslayer.cryptobuddy.serialize.Serialization;
import com.musicslayer.cryptobuddy.util.ToastUtil;
import com.musicslayer.cryptobuddy.view.table.AddressTable;

import java.util.ArrayList;
import java.util.HashMap;

public class AddressPortfolioExplorerActivity extends BaseActivity {
    public BaseDialogFragment confirmBackDialogFragment;

    AddressTable table;

    final static CryptoAddress[] newCryptoAddress = new CryptoAddress[1];

    AddressPortfolioObj addressPortfolioObj;
    HashMap<CryptoAddress, AddressData> addressDataMap = new HashMap<>();

    int filterIndex = -1;

    public boolean includeBalances = true;
    public boolean includeTransactions = true;

    public int getAdLayoutViewID() {
        return R.id.address_portfolio_explorer_adLayout;
    }

    @Override
    public void onBackPressedImpl() {
        confirmBackDialogFragment.show(AddressPortfolioExplorerActivity.this, "back");
    }

    public void createLayout () {
        setContentView(R.layout.activity_address_portfolio_explorer);

        confirmBackDialogFragment = BaseDialogFragment.newInstance(ConfirmBackDialog.class);
        confirmBackDialogFragment.setOnDismissListener(new CrashDialogInterface.CrashOnDismissListener(this) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                if(((ConfirmBackDialog)dialog).isComplete) {
                    startActivity(new Intent(AddressPortfolioExplorerActivity.this, AddressPortfolioViewerActivity.class));
                    finish();
                }
            }
        });
        confirmBackDialogFragment.restoreListeners(this, "back");

        addressPortfolioObj = AddressPortfolio.getFromName(getIntent().getStringExtra("AddressPortfolioName"));

        boolean includeTokens = false;
        for(CryptoAddress cryptoAddress : addressPortfolioObj.cryptoAddressArrayList) {
            if(cryptoAddress.includeTokens) {
                includeTokens = true;
                break;
            }
        }

        TextView T = findViewById(R.id.address_portfolio_explorer_messageTextView);
        if(!Purchases.isUnlockTokensPurchased && includeTokens) {
            T.setVisibility(View.VISIBLE);
        }
        else {
            T.setVisibility(View.GONE);
        }

        Toolbar toolbar = findViewById(R.id.address_portfolio_explorer_toolbar);
        setSupportActionBar(toolbar);

        ImageButton infoButton = findViewById(R.id.address_portfolio_explorer_cryptoInfoButton);
        infoButton.setOnClickListener(new CrashView.CrashOnClickListener(this) {
            @Override
            public void onClickImpl(View view) {
                InfoUtil.showInfo(AddressPortfolioExplorerActivity.this, addressPortfolioObj.cryptoAddressArrayList);
            }
        });

        if(!InfoUtil.hasInfo(addressPortfolioObj.cryptoAddressArrayList)) {
            infoButton.setVisibility(View.GONE);
        }

        ImageButton helpButton = findViewById(R.id.address_portfolio_explorer_helpButton);
        helpButton.setOnClickListener(new CrashView.CrashOnClickListener(this) {
            @Override
            public void onClickImpl(View view) {
                HelpUtil.showHelp(AddressPortfolioExplorerActivity.this, R.raw.help_address_portfolio_explorer);
            }
        });

        table = findViewById(R.id.address_portfolio_explorer_table);
        table.pageView = findViewById(R.id.address_portfolio_explorer_tablePageView);
        table.pageView.setTable(table);
        table.pageView.updateLayout();

        ProgressDialogFragment add_progressDialogFragment = ProgressDialogFragment.newInstance(ProgressDialog.class);
        add_progressDialogFragment.setOnShowListener(new CrashDialogInterface.CrashOnShowListener(this) {
            @Override
            public void onShowImpl(DialogInterface dialog) {
                AddressData addressData = AddressData.getAllData(newCryptoAddress[0]);

                // Save found tokens, potentially from multiple TokenManagers.
                TokenManagerList.saveAllData(AddressPortfolioExplorerActivity.this);

                ProgressDialogFragment.setValue(Serialization.serialize(addressData));
            }
        });

        add_progressDialogFragment.setOnDismissListener(new CrashDialogInterface.CrashOnDismissListener(this) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                AddressData addressData = Serialization.deserialize(ProgressDialogFragment.getValue(), AddressData.class);

                if(!addressData.isComplete()) {
                    ToastUtil.showToast(AddressPortfolioExplorerActivity.this,"incomplete_address_data");
                }

                addressPortfolioObj.addData(newCryptoAddress[0]);
                AddressPortfolio.updatePortfolio(AddressPortfolioExplorerActivity.this, addressPortfolioObj);

                addressDataMap.put(newCryptoAddress[0], addressData);
                updateLayout();

                if(filterIndex != -1) {
                    ToastUtil.showToast(AddressPortfolioExplorerActivity.this,"new_address_filtered");
                }
            }
        });
        add_progressDialogFragment.restoreListeners(this, "progress_add");

        BaseDialogFragment chooseAddressDialogFragment = BaseDialogFragment.newInstance(ChooseAddressDialog.class);
        chooseAddressDialogFragment.setOnDismissListener(new CrashDialogInterface.CrashOnDismissListener(this) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                if(((ChooseAddressDialog)dialog).isComplete) {
                    // Save new address and then fill it into the table.
                    newCryptoAddress[0] = ((ChooseAddressDialog)dialog).user_CRYPTOADDRESS;

                    if(addressPortfolioObj.isSaved(newCryptoAddress[0])) {
                        ToastUtil.showToast(AddressPortfolioExplorerActivity.this,"address_in_portfolio");
                    }
                    else {
                        add_progressDialogFragment.show(AddressPortfolioExplorerActivity.this, "progress_add");
                    }
                }
            }
        });
        chooseAddressDialogFragment.restoreListeners(this, "add");

        FloatingActionButton fab_add = findViewById(R.id.address_portfolio_explorer_addButton);
        fab_add.setOnClickListener(new CrashView.CrashOnClickListener(this) {
            @Override
            public void onClickImpl(View view) {
                chooseAddressDialogFragment.show(AddressPortfolioExplorerActivity.this, "add");
            }
        });

        FloatingActionButton fab_info = findViewById(R.id.address_portfolio_explorer_infoButton);
        fab_info.setOnClickListener(new CrashView.CrashOnClickListener(this) {
            @Override
            public void onClickImpl(View view) {
                BaseDialogFragment addressInfoDialogFragment = BaseDialogFragment.newInstance(AddressInfoDialog.class, new ArrayList<>(addressDataMap.values()));
                addressInfoDialogFragment.show(AddressPortfolioExplorerActivity.this, "info");
            }
        });

        FloatingActionButton fab_total = findViewById(R.id.address_portfolio_explorer_totalButton);
        fab_total.setOnClickListener(new CrashView.CrashOnClickListener(this) {
            @Override
            public void onClickImpl(View view) {
                BaseDialogFragment.newInstance(TotalDialog.class, table.getFilteredMaskedTransactionArrayList()).show(AddressPortfolioExplorerActivity.this, "total");
            }
        });

        FloatingActionButton fab_qrcode = findViewById(R.id.address_portfolio_explorer_qrCodeButton);
        fab_qrcode.setOnClickListener(new CrashView.CrashOnClickListener(this) {
            @Override
            public void onClickImpl(View view) {
                BaseDialogFragment.newInstance(AddressQRCodeDialog.class, addressPortfolioObj.cryptoAddressArrayList).show(AddressPortfolioExplorerActivity.this, "qrcode");
            }
        });

        ProgressDialogFragment download_progressDialogFragment = ProgressDialogFragment.newInstance(ProgressDialog.class);
        download_progressDialogFragment.setOnShowListener(new CrashDialogInterface.CrashOnShowListener(this) {
            @Override
            public void onShowImpl(DialogInterface dialog) {
                ArrayList<CryptoAddress> cryptoAddressArrayList = addressPortfolioObj.cryptoAddressArrayList;

                ArrayList<AddressData> newAddressDataArrayList = new ArrayList<>();
                for(CryptoAddress cryptoAddress : cryptoAddressArrayList) {
                    if(ProgressDialogFragment.isCancelled()) { return; }

                    AddressData newAddressData;

                    if(includeBalances && includeTransactions) {
                        newAddressData = AddressData.getAllData(cryptoAddress);
                    }
                    else if(includeBalances) {
                        newAddressData = AddressData.getCurrentBalanceData(cryptoAddress);
                    }
                    else if(includeTransactions) {
                        newAddressData = AddressData.getTransactionsData(cryptoAddress);
                    }
                    else {
                        newAddressData = AddressData.getNoData(cryptoAddress);
                    }

                    newAddressDataArrayList.add(newAddressData);

                    // Save found tokens, potentially from multiple TokenManagers.
                    TokenManagerList.saveAllData(AddressPortfolioExplorerActivity.this);
                }

                ProgressDialogFragment.setValue(Serialization.serializeArrayList(newAddressDataArrayList));
            }
        });

        download_progressDialogFragment.setOnDismissListener(new CrashDialogInterface.CrashOnDismissListener(this) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                ArrayList<AddressData> newAddressDataArrayList = Serialization.deserializeArrayList(ProgressDialogFragment.getValue(), AddressData.class);

                for(AddressData newAddressData : newAddressDataArrayList) {
                    boolean isComplete;
                    if(includeBalances && includeTransactions) {
                        isComplete = newAddressData.isComplete();
                    }
                    else if(includeBalances) {
                        isComplete = newAddressData.isCurrentBalanceComplete();
                    }
                    else if(includeTransactions) {
                        isComplete = newAddressData.isTransactionsComplete();
                    }
                    else {
                        isComplete = true;
                    }

                    if(!isComplete) {
                        // Only alert once. Others would be redundant.
                        ToastUtil.showToast(AddressPortfolioExplorerActivity.this,"incomplete_address_data");
                        break;
                    }
                }

                addressDataMap.clear();
                for(int i = 0; i < addressPortfolioObj.cryptoAddressArrayList.size(); i++) {
                    CryptoAddress cryptoAddress = addressPortfolioObj.cryptoAddressArrayList.get(i);
                    AddressData addressData = newAddressDataArrayList.get(i);
                    addressDataMap.put(cryptoAddress, addressData);
                }

                updateLayout();
                ToastUtil.showToast(AddressPortfolioExplorerActivity.this,"address_data_downloaded");
            }
        });
        download_progressDialogFragment.restoreListeners(this, "progress_download");

        BaseDialogFragment downloadDialogFragment = BaseDialogFragment.newInstance(DownloadDataDialog.class);
        downloadDialogFragment.setOnDismissListener(new CrashDialogInterface.CrashOnDismissListener(this) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                if(((DownloadDataDialog)dialog).isComplete) {
                    includeBalances = ((DownloadDataDialog)dialog).user_BALANCES;
                    includeTransactions = ((DownloadDataDialog)dialog).user_TRANSACTIONS;
                    download_progressDialogFragment.show(AddressPortfolioExplorerActivity.this, "progress_download");
                }
            }
        });
        downloadDialogFragment.restoreListeners(this, "download");

        AppCompatButton downloadDataButton = findViewById(R.id.address_portfolio_explorer_downloadDataButton);
        downloadDataButton.setOnClickListener(new CrashView.CrashOnClickListener(this) {
            @Override
            public void onClickImpl(View view) {
                downloadDialogFragment.show(AddressPortfolioExplorerActivity.this, "download");
            }
        });

        BaseDialogFragment addressFilterDialogFragment = BaseDialogFragment.newInstance(AddressFilterDialog.class, filterIndex, addressPortfolioObj.cryptoAddressArrayList);
        addressFilterDialogFragment.setOnDismissListener(new CrashDialogInterface.CrashOnDismissListener(this) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                if(((AddressFilterDialog)dialog).isComplete) {
                    filterIndex = ((AddressFilterDialog)dialog).user_INDEX;
                    updateLayout();
                }
            }
        });
        addressFilterDialogFragment.restoreListeners(this, "address_filter");

        AppCompatButton filterAddressButton = findViewById(R.id.address_portfolio_explorer_filterAddressButton);
        filterAddressButton.setOnClickListener(new CrashView.CrashOnClickListener(this) {
            @Override
            public void onClickImpl(View view) {
                addressFilterDialogFragment.updateArguments(AddressFilterDialog.class, filterIndex, addressPortfolioObj.cryptoAddressArrayList);
                addressFilterDialogFragment.show(AddressPortfolioExplorerActivity.this, "address_filter");
            }
        });

        updateLayout();
    }

    public void updateLayout() {
        table.resetTable();

        TextView T = findViewById(R.id.address_portfolio_explorer_infoTextView);

        if(filterIndex == -1) {
            T.setText("Portfolio = " + addressPortfolioObj.name);
            table.addRowsFromAddressDataArray(this, new ArrayList<>(addressDataMap.values()));
        }
        else {
            T.setText("Portfolio = " + addressPortfolioObj.name + "\nAddress = " + addressPortfolioObj.cryptoAddressArrayList.get(filterIndex).toString());
            table.addRowsFromAddressData(this, getValueFromMap(addressPortfolioObj.cryptoAddressArrayList.get(filterIndex)));
        }
    }

    public AddressData getValueFromMap(CryptoAddress cryptoAddress) {
        // We need this because HashMap isn't using the equals method as we expect.
        ArrayList<CryptoAddress> keys = new ArrayList<>(addressDataMap.keySet());
        for(CryptoAddress key : keys) {
            if(key.equals(cryptoAddress)) {
                return addressDataMap.get(key);
            }
        }
        return null;
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
            BaseDialogFragment.newInstance(CryptoPricesDialog.class).show(AddressPortfolioExplorerActivity.this, "price");
            return true;
        }
        else if (id == 2) {
            BaseDialogFragment.newInstance(CryptoConverterDialog.class).show(AddressPortfolioExplorerActivity.this, "converter");
            return true;
        }
        else if (id == 3) {
            AddressTable table = findViewById(R.id.address_portfolio_explorer_table);
            String type = "AddressPortfolio";
            String info = "Address Portfolio:\n\n" + Serialization.serialize(addressPortfolioObj) + "\n\n" + table.getInfo();
            BaseDialogFragment.newInstance(ReportFeedbackDialog.class, type, info).show(AddressPortfolioExplorerActivity.this, "feedback");
            return true;
        }

        return false;
    }

    @Override
    public void onSaveInstanceStateImpl(@NonNull Bundle bundle) {
        bundle.putInt("filterIndex", filterIndex);
        bundle.putString("addressDataMap", Serialization.serializeHashMap(addressDataMap));
        bundle.putString("includeBalances", Serialization.boolean_serialize(includeBalances));
        bundle.putString("includeTransactions", Serialization.boolean_serialize(includeTransactions));
    }

    @Override
    public void onRestoreInstanceStateImpl(Bundle bundle) {
        if(bundle != null) {
            filterIndex = bundle.getInt("filterIndex", -1);
            addressDataMap = Serialization.deserializeHashMap(bundle.getString("addressDataMap"), CryptoAddress.class, AddressData.class);
            includeBalances = Serialization.boolean_deserialize(bundle.getString("includeBalances"));
            includeTransactions = Serialization.boolean_deserialize(bundle.getString("includeTransactions"));

            updateLayout();
        }
    }
}