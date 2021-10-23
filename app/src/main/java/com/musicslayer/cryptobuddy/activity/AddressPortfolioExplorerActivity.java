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
import com.musicslayer.cryptobuddy.dialog.AddressQRCodeDialog;
import com.musicslayer.cryptobuddy.dialog.ConfirmBackDialog;
import com.musicslayer.cryptobuddy.dialog.CryptoConverterDialog;
import com.musicslayer.cryptobuddy.dialog.DiscreteFilterDialog;
import com.musicslayer.cryptobuddy.dialog.DownloadDataDialog;
import com.musicslayer.cryptobuddy.dialog.ProgressDialog;
import com.musicslayer.cryptobuddy.dialog.ProgressDialogFragment;
import com.musicslayer.cryptobuddy.dialog.RemoveAddressDialog;
import com.musicslayer.cryptobuddy.dialog.ReportFeedbackDialog;
import com.musicslayer.cryptobuddy.filter.DiscreteFilter;
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
import com.musicslayer.cryptobuddy.util.HashMapUtil;
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

    AddressPortfolioObj addressPortfolioObj;
    HashMap<CryptoAddress, AddressData> addressDataMap = new HashMap<>();
    HashMap<CryptoAddress, AddressData> addressDataFilterMap = new HashMap<>();

    DiscreteFilter addressFilter = new DiscreteFilter();

    public ArrayList<Boolean> includeBalances;
    public ArrayList<Boolean> includeTransactions;

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

        updateFilter();

        boolean includeTokens = false;
        for(CryptoAddress cryptoAddress : addressPortfolioObj.cryptoAddressArrayList) {
            HashMapUtil.putValueInMap(addressDataMap, cryptoAddress, AddressData.getNoData(cryptoAddress));
            HashMapUtil.putValueInMap(addressDataFilterMap, cryptoAddress, AddressData.getNoData(cryptoAddress));

            if(cryptoAddress.includeTokens) {
                includeTokens = true;
            }
        }

        TextView T_INFO = findViewById(R.id.address_portfolio_explorer_infoTextView);
        T_INFO.setText("Portfolio = " + addressPortfolioObj.name);

        TextView T_MESSAGE = findViewById(R.id.address_portfolio_explorer_messageTextView);
        if(!Purchases.isUnlockTokensPurchased && includeTokens) {
            T_MESSAGE.setVisibility(View.VISIBLE);
        }
        else {
            T_MESSAGE.setVisibility(View.GONE);
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

        BaseDialogFragment chooseAddressDialogFragment = BaseDialogFragment.newInstance(ChooseAddressDialog.class);
        chooseAddressDialogFragment.setOnDismissListener(new CrashDialogInterface.CrashOnDismissListener(this) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                if(((ChooseAddressDialog)dialog).isComplete) {
                    // Save new address to the portfolio and then add them to the table.
                    CryptoAddress newCryptoAddress = ((ChooseAddressDialog)dialog).user_CRYPTOADDRESS;

                    if(addressPortfolioObj.isSaved(newCryptoAddress)) {
                        ToastUtil.showToast(AddressPortfolioExplorerActivity.this,"address_in_portfolio");
                    }
                    else {
                        addressPortfolioObj.addData(newCryptoAddress);
                        AddressPortfolio.updatePortfolio(AddressPortfolioExplorerActivity.this, addressPortfolioObj);

                        updateFilter();

                        HashMapUtil.putValueInMap(addressDataMap, newCryptoAddress, AddressData.getNoData(newCryptoAddress));
                        HashMapUtil.putValueInMap(addressDataFilterMap, newCryptoAddress, AddressData.getNoData(newCryptoAddress));
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

        BaseDialogFragment removeAddressDialogFragment = BaseDialogFragment.newInstance(RemoveAddressDialog.class, addressPortfolioObj);
        removeAddressDialogFragment.setOnDismissListener(new CrashDialogInterface.CrashOnDismissListener(this) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                if(((RemoveAddressDialog)dialog).isComplete) {
                    // Remove addresses from portfolio and then remove them from the table.
                    ArrayList<CryptoAddress> toRemove = ((RemoveAddressDialog)dialog).user_cryptoAddressArrayList;
                    for(CryptoAddress cryptoAddress : toRemove) {
                        addressPortfolioObj.removeData(cryptoAddress);
                        HashMapUtil.removeValueFromMap(addressDataMap, cryptoAddress);
                        HashMapUtil.removeValueFromMap(addressDataFilterMap, cryptoAddress);
                    }

                    AddressPortfolio.updatePortfolio(AddressPortfolioExplorerActivity.this, addressPortfolioObj);

                    updateFilter();

                    updateLayout();
                }
            }
        });
        removeAddressDialogFragment.restoreListeners(this, "remove");

        FloatingActionButton fab_remove = findViewById(R.id.address_portfolio_explorer_removeButton);
        fab_remove.setOnClickListener(new CrashView.CrashOnClickListener(this) {
            @Override
            public void onClickImpl(View view) {
                removeAddressDialogFragment.show(AddressPortfolioExplorerActivity.this, "remove");
            }
        });

        FloatingActionButton fab_info = findViewById(R.id.address_portfolio_explorer_infoButton);
        fab_info.setOnClickListener(new CrashView.CrashOnClickListener(this) {
            @Override
            public void onClickImpl(View view) {
                BaseDialogFragment addressInfoDialogFragment = BaseDialogFragment.newInstance(AddressInfoDialog.class, addressPortfolioObj.cryptoAddressArrayList, addressDataMap);
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
                for(int i = 0; i < cryptoAddressArrayList.size(); i++) {
                    CryptoAddress cryptoAddress = cryptoAddressArrayList.get(i);

                    if(ProgressDialogFragment.isCancelled()) { return; }

                    AddressData newAddressData;

                    if(includeBalances.get(i) && includeTransactions.get(i)) {
                        newAddressData = AddressData.getAllData(cryptoAddress);
                    }
                    else if(includeBalances.get(i)) {
                        newAddressData = AddressData.getCurrentBalanceData(cryptoAddress);
                    }
                    else if(includeTransactions.get(i)) {
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

                for(int i = 0; i < newAddressDataArrayList.size(); i++) {
                    AddressData newAddressData = newAddressDataArrayList.get(i);

                    boolean isComplete;
                    if(includeBalances.get(i) && includeTransactions.get(i)) {
                        isComplete = newAddressData.isComplete();
                    }
                    else if(includeBalances.get(i)) {
                        isComplete = newAddressData.isCurrentBalanceComplete();
                    }
                    else if(includeTransactions.get(i)) {
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

                for(int i = 0; i < addressPortfolioObj.cryptoAddressArrayList.size(); i++) {
                    CryptoAddress cryptoAddress = addressPortfolioObj.cryptoAddressArrayList.get(i);
                    AddressData newAddressData = newAddressDataArrayList.get(i);
                    AddressData oldAddressData = HashMapUtil.getValueFromMap(addressDataMap, cryptoAddress);
                    AddressData mergedAddressData = AddressData.merge(oldAddressData, newAddressData);
                    HashMapUtil.putValueInMap(addressDataMap, cryptoAddress, mergedAddressData);
                }

                // Apply filter after downloading data.
                ArrayList<String> choices = addressFilter.user_choices;

                addressDataFilterMap.clear();
                for(CryptoAddress cryptoAddress : new ArrayList<>(addressDataMap.keySet())) {
                    if(choices.contains(cryptoAddress.toString())) {
                        AddressData addressData = HashMapUtil.getValueFromMap(addressDataMap, cryptoAddress);
                        HashMapUtil.putValueInMap(addressDataFilterMap, cryptoAddress, addressData);
                    }
                }

                updateLayout();
                ToastUtil.showToast(AddressPortfolioExplorerActivity.this,"address_data_downloaded");
            }
        });
        download_progressDialogFragment.restoreListeners(this, "progress_download");

        BaseDialogFragment downloadDialogFragment = BaseDialogFragment.newInstance(DownloadDataDialog.class, addressPortfolioObj.cryptoAddressArrayList, addressDataMap);
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

        BaseDialogFragment addressFilterDialogFragment = addressFilter.getGenericDialogFragment();
        addressFilterDialogFragment.setOnDismissListener(new CrashDialogInterface.CrashOnDismissListener(this) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                if(((DiscreteFilterDialog)dialog).isComplete) {
                    addressFilter = ((DiscreteFilterDialog)dialog).discreteFilter;

                    ArrayList<String> choices = addressFilter.user_choices;

                    addressDataFilterMap.clear();
                    for(CryptoAddress cryptoAddress : new ArrayList<>(addressDataMap.keySet())) {
                        if(choices.contains(cryptoAddress.toString())) {
                            AddressData addressData = HashMapUtil.getValueFromMap(addressDataMap, cryptoAddress);
                            HashMapUtil.putValueInMap(addressDataFilterMap, cryptoAddress, addressData);
                        }
                    }

                    updateLayout();
                }
            }
        });
        addressFilterDialogFragment.restoreListeners(this, "address_filter");

        AppCompatButton filterAddressButton = findViewById(R.id.address_portfolio_explorer_filterAddressButton);
        filterAddressButton.setOnClickListener(new CrashView.CrashOnClickListener(this) {
            @Override
            public void onClickImpl(View view) {
                addressFilterDialogFragment.updateArguments(DiscreteFilterDialog.class, addressFilter);
                addressFilterDialogFragment.show(AddressPortfolioExplorerActivity.this, "address_filter");
            }
        });
    }

    public void updateLayout() {
        table.resetTable();
        table.addRowsFromAddressDataArray(new ArrayList<>(addressDataFilterMap.values()));
    }

    public void updateFilter() {
        ArrayList<String> data = new ArrayList<>();
        for(CryptoAddress cryptoAddress : addressPortfolioObj.cryptoAddressArrayList) {
            data.add(cryptoAddress.toString());
        }

        addressFilter.updateFilterData(data);
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
        bundle.putSerializable("addressDataMap", addressDataMap);
        bundle.putSerializable("addressDataFilterMap", addressDataFilterMap);
        bundle.putSerializable("includeBalances", includeBalances);
        bundle.putSerializable("includeTransactions", includeTransactions);
        bundle.putString("filter", Serialization.serialize(addressFilter));
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onRestoreInstanceStateImpl(Bundle bundle) {
        if(bundle != null) {
            addressDataMap = (HashMap<CryptoAddress, AddressData>)bundle.getSerializable("addressDataMap");
            addressDataFilterMap = (HashMap<CryptoAddress, AddressData>)bundle.getSerializable("addressDataFilterMap");
            includeBalances = (ArrayList<Boolean>)bundle.getSerializable("includeBalances");
            includeTransactions = (ArrayList<Boolean>)bundle.getSerializable("includeTransactions");
            addressFilter = Serialization.deserialize(bundle.getString("filter"), DiscreteFilter.class);
        }
    }
}