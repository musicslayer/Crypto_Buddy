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
import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.api.address.AddressData;
import com.musicslayer.cryptobuddy.api.address.CryptoAddress;
import com.musicslayer.cryptobuddy.crash.CrashDialogInterface;
import com.musicslayer.cryptobuddy.crash.CrashView;
import com.musicslayer.cryptobuddy.dialog.AddressInfoDialog;
import com.musicslayer.cryptobuddy.dialog.AddressQRCodeDialog;
import com.musicslayer.cryptobuddy.dialog.BaseDialogFragment;
import com.musicslayer.cryptobuddy.dialog.ChooseAddressDialog;
import com.musicslayer.cryptobuddy.dialog.ConfirmBackDialog;
import com.musicslayer.cryptobuddy.dialog.CryptoConverterDialog;
import com.musicslayer.cryptobuddy.dialog.CryptoPricesDialog;
import com.musicslayer.cryptobuddy.dialog.DiscreteFilterDialog;
import com.musicslayer.cryptobuddy.dialog.DownloadAddressDataDialog;
import com.musicslayer.cryptobuddy.dialog.ProgressDialog;
import com.musicslayer.cryptobuddy.dialog.ProgressDialogFragment;
import com.musicslayer.cryptobuddy.dialog.RemoveAddressDialog;
import com.musicslayer.cryptobuddy.dialog.ReportFeedbackDialog;
import com.musicslayer.cryptobuddy.dialog.TotalDialog;
import com.musicslayer.cryptobuddy.filter.DiscreteFilter;
import com.musicslayer.cryptobuddy.persistence.AddressPortfolio;
import com.musicslayer.cryptobuddy.persistence.AddressPortfolioObj;
import com.musicslayer.cryptobuddy.persistence.Purchases;
import com.musicslayer.cryptobuddy.persistence.TokenManagerList;
import com.musicslayer.cryptobuddy.serialize.Serialization;
import com.musicslayer.cryptobuddy.state.ActivityStateObj;
import com.musicslayer.cryptobuddy.state.TableStateObj;
import com.musicslayer.cryptobuddy.util.HashMapUtil;
import com.musicslayer.cryptobuddy.util.HelpUtil;
import com.musicslayer.cryptobuddy.util.InfoUtil;
import com.musicslayer.cryptobuddy.util.ToastUtil;
import com.musicslayer.cryptobuddy.view.table.AddressTable;

import java.util.ArrayList;

public class ExchangePortfolioExplorerActivity extends BaseActivity {
    public BaseDialogFragment confirmBackDialogFragment;

    AddressTable table;
    public final static ActivityStateObj[] activityStateObj = new ActivityStateObj[1];

    AddressPortfolioObj addressPortfolioObj;

    DiscreteFilter addressFilter = new DiscreteFilter();

    public ArrayList<Boolean> includeBalances;
    public ArrayList<Boolean> includeTransactions;

    public int getAdLayoutViewID() {
        return R.id.address_portfolio_explorer_adLayout;
    }

    @Override
    public void onBackPressedImpl() {
        confirmBackDialogFragment.show(ExchangePortfolioExplorerActivity.this, "back");
    }

    public void createLayout () {
        setContentView(R.layout.activity_address_portfolio_explorer);

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

                    startActivity(new Intent(ExchangePortfolioExplorerActivity.this, AddressPortfolioViewerActivity.class));
                    finish();
                }
            }
        });
        confirmBackDialogFragment.restoreListeners(this, "back");

        addressPortfolioObj = AddressPortfolio.getFromName(getIntent().getStringExtra("AddressPortfolioName"));

        if(isFirstCreate) {
            activityStateObj[0].addressPortfolioObj = addressPortfolioObj;
        }

        updateFilter();

        boolean includeTokens = false;
        for(CryptoAddress cryptoAddress : addressPortfolioObj.cryptoAddressArrayList) {
            if(isFirstCreate) {
                HashMapUtil.putValueInMap(activityStateObj[0].addressDataMap, cryptoAddress, AddressData.getNoData(cryptoAddress));
                HashMapUtil.putValueInMap(activityStateObj[0].addressDataFilterMap, cryptoAddress, AddressData.getNoData(cryptoAddress));
            }

            if(cryptoAddress.includeTokens) {
                includeTokens = true;
            }
        }

        TextView T_INFO = findViewById(R.id.address_portfolio_explorer_infoTextView);
        T_INFO.setText("Portfolio = " + addressPortfolioObj.name);

        TextView T_MESSAGE = findViewById(R.id.address_portfolio_explorer_messageTextView);
        if(!Purchases.isUnlockTokensPurchased() && includeTokens) {
            T_MESSAGE.setVisibility(View.VISIBLE);
        }
        else {
            T_MESSAGE.setVisibility(View.GONE);
        }

        Toolbar toolbar = findViewById(R.id.address_portfolio_explorer_toolbar);
        setSupportActionBar(toolbar);

        ImageButton infoButton = findViewById(R.id.address_portfolio_explorer_problemInfoButton);
        infoButton.setOnClickListener(new CrashView.CrashOnClickListener(this) {
            @Override
            public void onClickImpl(View view) {
                InfoUtil.showInfo_CryptoAddress(ExchangePortfolioExplorerActivity.this, addressPortfolioObj.cryptoAddressArrayList);
            }
        });

        if(!InfoUtil.hasInfo_CryptoAddress(addressPortfolioObj.cryptoAddressArrayList)) {
            infoButton.setVisibility(View.GONE);
        }

        ImageButton helpButton = findViewById(R.id.address_portfolio_explorer_helpButton);
        helpButton.setOnClickListener(new CrashView.CrashOnClickListener(this) {
            @Override
            public void onClickImpl(View view) {
                HelpUtil.showHelp(ExchangePortfolioExplorerActivity.this, R.raw.help_address_portfolio_explorer);
            }
        });

        table = findViewById(R.id.address_portfolio_explorer_table);
        table.pageView = findViewById(R.id.address_portfolio_explorer_tablePageView);
        table.pageView.setTable(table);
        table.pageView.updateLayout();
        if(isFirstCreate) {
            table.tableStateObj[0] = new TableStateObj();
            table.tableStateObj[0].table = table;
        }

        BaseDialogFragment chooseAddressDialogFragment = BaseDialogFragment.newInstance(ChooseAddressDialog.class);
        chooseAddressDialogFragment.setOnDismissListener(new CrashDialogInterface.CrashOnDismissListener(this) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                if(((ChooseAddressDialog)dialog).isComplete) {
                    // Save new address to the portfolio and then add them to the table.
                    CryptoAddress newCryptoAddress = ((ChooseAddressDialog)dialog).user_CRYPTOADDRESS;

                    if(addressPortfolioObj.isSaved(newCryptoAddress)) {
                        ToastUtil.showToast(ExchangePortfolioExplorerActivity.this,"address_in_portfolio");
                    }
                    else {
                        addressPortfolioObj.addData(newCryptoAddress);
                        AddressPortfolio.updatePortfolio(ExchangePortfolioExplorerActivity.this, addressPortfolioObj);

                        updateFilter();

                        HashMapUtil.putValueInMap(activityStateObj[0].addressDataMap, newCryptoAddress, AddressData.getNoData(newCryptoAddress));
                        HashMapUtil.putValueInMap(activityStateObj[0].addressDataFilterMap, newCryptoAddress, AddressData.getNoData(newCryptoAddress));
                    }
                }
            }
        });
        chooseAddressDialogFragment.restoreListeners(this, "add");

        FloatingActionButton fab_add = findViewById(R.id.address_portfolio_explorer_addButton);
        fab_add.setOnClickListener(new CrashView.CrashOnClickListener(this) {
            @Override
            public void onClickImpl(View view) {
                chooseAddressDialogFragment.show(ExchangePortfolioExplorerActivity.this, "add");
            }
        });

        BaseDialogFragment removeAddressDialogFragment = BaseDialogFragment.newInstance(RemoveAddressDialog.class, addressPortfolioObj.cryptoAddressArrayList);
        removeAddressDialogFragment.setOnDismissListener(new CrashDialogInterface.CrashOnDismissListener(this) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                if(((RemoveAddressDialog)dialog).isComplete) {
                    // Remove addresses from portfolio and then remove them from the table.
                    ArrayList<CryptoAddress> toRemove = ((RemoveAddressDialog)dialog).user_cryptoAddressArrayList;
                    for(CryptoAddress cryptoAddress : toRemove) {
                        addressPortfolioObj.removeData(cryptoAddress);
                        HashMapUtil.removeValueFromMap(activityStateObj[0].addressDataMap, cryptoAddress);
                        HashMapUtil.removeValueFromMap(activityStateObj[0].addressDataFilterMap, cryptoAddress);
                    }

                    AddressPortfolio.updatePortfolio(ExchangePortfolioExplorerActivity.this, addressPortfolioObj);

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
                removeAddressDialogFragment.show(ExchangePortfolioExplorerActivity.this, "remove");
            }
        });

        FloatingActionButton fab_info = findViewById(R.id.address_portfolio_explorer_infoButton);
        fab_info.setOnClickListener(new CrashView.CrashOnClickListener(this) {
            @Override
            public void onClickImpl(View view) {
                //BaseDialogFragment.newInstance(AddressInfoDialog.class, addressPortfolioObj.cryptoAddressArrayList, activityStateObj[0].addressDataMap).show(AddressPortfolioExplorerActivity.this, "info");
                BaseDialogFragment.newInstance(AddressInfoDialog.class, addressPortfolioObj.cryptoAddressArrayList).show(ExchangePortfolioExplorerActivity.this, "info");
            }
        });

        FloatingActionButton fab_total = findViewById(R.id.address_portfolio_explorer_totalButton);
        fab_total.setOnClickListener(new CrashView.CrashOnClickListener(this) {
            @Override
            public void onClickImpl(View view) {
                //BaseDialogFragment.newInstance(TotalDialog.class, table.getFilteredMaskedTransactionArrayList()).show(AddressPortfolioExplorerActivity.this, "total");
                BaseDialogFragment.newInstance(TotalDialog.class).show(ExchangePortfolioExplorerActivity.this, "total");
            }
        });

        FloatingActionButton fab_qrcode = findViewById(R.id.address_portfolio_explorer_qrCodeButton);
        fab_qrcode.setOnClickListener(new CrashView.CrashOnClickListener(this) {
            @Override
            public void onClickImpl(View view) {
                BaseDialogFragment.newInstance(AddressQRCodeDialog.class, addressPortfolioObj.cryptoAddressArrayList).show(ExchangePortfolioExplorerActivity.this, "qrcode");
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
                    TokenManagerList.saveAllData(ExchangePortfolioExplorerActivity.this);
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
                        ToastUtil.showToast(ExchangePortfolioExplorerActivity.this,"incomplete_address_data");
                        break;
                    }
                }

                for(int i = 0; i < addressPortfolioObj.cryptoAddressArrayList.size(); i++) {
                    CryptoAddress cryptoAddress = addressPortfolioObj.cryptoAddressArrayList.get(i);
                    AddressData newAddressData = newAddressDataArrayList.get(i);
                    AddressData oldAddressData = HashMapUtil.getValueFromMap(activityStateObj[0].addressDataMap, cryptoAddress);
                    AddressData mergedAddressData = AddressData.merge(oldAddressData, newAddressData);
                    HashMapUtil.putValueInMap(activityStateObj[0].addressDataMap, cryptoAddress, mergedAddressData);
                }

                // Apply filter after downloading data.
                ArrayList<String> choices = addressFilter.user_choices;

                activityStateObj[0].addressDataFilterMap.clear();
                for(CryptoAddress cryptoAddress : new ArrayList<>(activityStateObj[0].addressDataMap.keySet())) {
                    if(choices.contains(cryptoAddress.toString())) {
                        AddressData addressData = HashMapUtil.getValueFromMap(activityStateObj[0].addressDataMap, cryptoAddress);
                        HashMapUtil.putValueInMap(activityStateObj[0].addressDataFilterMap, cryptoAddress, addressData);
                    }
                }

                updateLayout();
                ToastUtil.showToast(ExchangePortfolioExplorerActivity.this,"address_data_downloaded");
            }
        });
        download_progressDialogFragment.restoreListeners(this, "progress_download");

        //BaseDialogFragment downloadDialogFragment = BaseDialogFragment.newInstance(DownloadDataDialog.class, addressPortfolioObj.cryptoAddressArrayList, activityStateObj[0].addressDataMap);
        BaseDialogFragment downloadDialogFragment = BaseDialogFragment.newInstance(DownloadAddressDataDialog.class, addressPortfolioObj.cryptoAddressArrayList);
        downloadDialogFragment.setOnDismissListener(new CrashDialogInterface.CrashOnDismissListener(this) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                if(((DownloadAddressDataDialog)dialog).isComplete) {
                    includeBalances = ((DownloadAddressDataDialog)dialog).user_BALANCES;
                    includeTransactions = ((DownloadAddressDataDialog)dialog).user_TRANSACTIONS;
                    download_progressDialogFragment.show(ExchangePortfolioExplorerActivity.this, "progress_download");
                }
            }
        });
        downloadDialogFragment.restoreListeners(this, "download");

        AppCompatButton downloadDataButton = findViewById(R.id.address_portfolio_explorer_downloadDataButton);
        downloadDataButton.setOnClickListener(new CrashView.CrashOnClickListener(this) {
            @Override
            public void onClickImpl(View view) {
                downloadDialogFragment.show(ExchangePortfolioExplorerActivity.this, "download");
            }
        });

        BaseDialogFragment addressFilterDialogFragment = addressFilter.getGenericDialogFragment();
        addressFilterDialogFragment.setOnDismissListener(new CrashDialogInterface.CrashOnDismissListener(this) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                if(((DiscreteFilterDialog)dialog).isComplete) {
                    addressFilter = ((DiscreteFilterDialog)dialog).discreteFilter;

                    ArrayList<String> choices = addressFilter.user_choices;

                    activityStateObj[0].addressDataFilterMap.clear();
                    for(CryptoAddress cryptoAddress : new ArrayList<>(activityStateObj[0].addressDataMap.keySet())) {
                        if(choices.contains(cryptoAddress.toString())) {
                            AddressData addressData = HashMapUtil.getValueFromMap(activityStateObj[0].addressDataMap, cryptoAddress);
                            HashMapUtil.putValueInMap(activityStateObj[0].addressDataFilterMap, cryptoAddress, addressData);
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
                addressFilterDialogFragment.show(ExchangePortfolioExplorerActivity.this, "address_filter");
            }
        });
    }

    public void updateLayout() {
        table.resetTable();
        table.addRowsFromAddressDataArray(new ArrayList<>(activityStateObj[0].addressDataFilterMap.values()));
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
            BaseDialogFragment.newInstance(CryptoPricesDialog.class).show(ExchangePortfolioExplorerActivity.this, "price");
            return true;
        }
        else if (id == 2) {
            BaseDialogFragment.newInstance(CryptoConverterDialog.class).show(ExchangePortfolioExplorerActivity.this, "converter");
            return true;
        }
        else if (id == 3) {
            String type = "AddressPortfolio";
            BaseDialogFragment.newInstance(ReportFeedbackDialog.class, type).show(ExchangePortfolioExplorerActivity.this, "feedback");
            return true;
        }

        return false;
    }

    @Override
    public void onSaveInstanceStateImpl(@NonNull Bundle bundle) {
        bundle.putSerializable("includeBalances", includeBalances);
        bundle.putSerializable("includeTransactions", includeTransactions);
        bundle.putParcelable("filter", addressFilter);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onRestoreInstanceStateImpl(Bundle bundle) {
        if(bundle != null) {
            includeBalances = (ArrayList<Boolean>)bundle.getSerializable("includeBalances");
            includeTransactions = (ArrayList<Boolean>)bundle.getSerializable("includeTransactions");
            addressFilter = bundle.getParcelable("filter");
        }
    }
}