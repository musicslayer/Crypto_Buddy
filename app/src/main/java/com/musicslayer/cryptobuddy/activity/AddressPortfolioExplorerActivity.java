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
import com.musicslayer.cryptobuddy.api.address.CryptoAddress;
import com.musicslayer.cryptobuddy.asset.tokenmanager.TokenManager;
import com.musicslayer.cryptobuddy.dialog.AddressFilterDialog;
import com.musicslayer.cryptobuddy.dialog.AddressQRCodeDialog;
import com.musicslayer.cryptobuddy.dialog.ConfirmBackDialog;
import com.musicslayer.cryptobuddy.dialog.CryptoConverterDialog;
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
import com.musicslayer.cryptobuddy.util.Help;
import com.musicslayer.cryptobuddy.util.Info;
import com.musicslayer.cryptobuddy.util.Serialization;
import com.musicslayer.cryptobuddy.util.Toast;
import com.musicslayer.cryptobuddy.view.table.AddressTable;

import java.util.ArrayList;

public class AddressPortfolioExplorerActivity extends BaseActivity {
    public BaseDialogFragment confirmBackDialogFragment;

    AddressTable table;

    final static CryptoAddress[] cryptoAddress = new CryptoAddress[1];
    final AddressData[] addressData = new AddressData[1];
    final static ArrayList<AddressData>[] newAddressDataArrayList = new ArrayList[1];

    AddressPortfolioObj addressPortfolioObj;
    ArrayList<AddressData> addressDataArrayList;

    int filterIndex = -1;

    public int getAdLayoutViewID() {
        return R.id.address_portfolio_explorer_adLayout;
    }

    @Override
    public void onBackPressed() {
        confirmBackDialogFragment.show(AddressPortfolioExplorerActivity.this, "back");
    }

    public void createLayout () {
        setContentView(R.layout.activity_address_portfolio_explorer);

        confirmBackDialogFragment = BaseDialogFragment.newInstance(ConfirmBackDialog.class);
        confirmBackDialogFragment.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if(((ConfirmBackDialog)dialog).isComplete) {
                    finish();
                    startActivity(new Intent(AddressPortfolioExplorerActivity.this, AddressPortfolioViewerActivity.class));
                }
            }
        });
        confirmBackDialogFragment.restoreListeners(this, "back");

        addressPortfolioObj = AddressPortfolio.getFromName(getIntent().getStringExtra("AddressPortfolioName"));
        addressDataArrayList = Serialization.deserializeArrayList(getIntent().getStringExtra("AddressData_Array"), AddressData.class);

        boolean includeTokens = false;
        for(AddressData addressData : addressDataArrayList) {
            if(addressData.cryptoAddress.includeTokens) {
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
        infoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Info.showInfo(AddressPortfolioExplorerActivity.this, addressDataArrayList);
            }
        });

        if(!Info.hasInfo(addressDataArrayList)) {
            infoButton.setVisibility(View.GONE);
        }

        ImageButton helpButton = findViewById(R.id.address_portfolio_explorer_helpButton);
        helpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Help.showHelp(AddressPortfolioExplorerActivity.this, R.raw.help_address_portfolio_explorer);
            }
        });

        table = findViewById(R.id.address_portfolio_explorer_table);
        table.pageView = findViewById(R.id.address_portfolio_explorer_tablePageView);
        table.pageView.setTable(table);
        table.pageView.updateLayout();

        ProgressDialogFragment add_progressDialogFragment = ProgressDialogFragment.newInstance(ProgressDialog.class);
        add_progressDialogFragment.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                addressData[0] = AddressData.getAddressData(cryptoAddress[0]);
                TokenManager.saveAll(AddressPortfolioExplorerActivity.this, "found");
            }
        });

        add_progressDialogFragment.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                addressData[0].alertUser();

                addressPortfolioObj.addData(cryptoAddress[0]);
                AddressPortfolio.saveAllData(AddressPortfolioExplorerActivity.this);

                addressDataArrayList.add(addressData[0]);
                updateLayout();

                if(filterIndex != -1) {
                    Toast.showToast("new_address_filtered");
                }
            }
        });
        add_progressDialogFragment.restoreListeners(this, "progress_add");

        BaseDialogFragment chooseAddressDialogFragment = BaseDialogFragment.newInstance(ChooseAddressDialog.class);
        chooseAddressDialogFragment.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if(((ChooseAddressDialog)dialog).isComplete) {
                    // Save new address and then fill it into the table.
                    cryptoAddress[0] = ((ChooseAddressDialog)dialog).user_CRYPTOADDRESS;

                    if(addressPortfolioObj.isSaved(cryptoAddress[0])) {
                        Toast.showToast("address_in_portfolio");
                    }
                    else {
                        add_progressDialogFragment.show(AddressPortfolioExplorerActivity.this, "progress_add");
                    }
                }
            }
        });
        chooseAddressDialogFragment.restoreListeners(this, "add");

        FloatingActionButton fab_add = findViewById(R.id.address_portfolio_explorer_addButton);
        fab_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseAddressDialogFragment.show(AddressPortfolioExplorerActivity.this, "add");
            }
        });

        FloatingActionButton fab_info = findViewById(R.id.address_portfolio_explorer_infoButton);
        fab_info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BaseDialogFragment addressInfoDialogFragment = BaseDialogFragment.newInstance(AddressInfoDialog.class, addressDataArrayList);
                addressInfoDialogFragment.show(AddressPortfolioExplorerActivity.this, "info");
            }
        });

        FloatingActionButton fab_total = findViewById(R.id.address_portfolio_explorer_totalButton);
        fab_total.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BaseDialogFragment.newInstance(TotalDialog.class, table.getFilteredMaskedTransactionArrayList()).show(AddressPortfolioExplorerActivity.this, "total");
            }
        });

        BaseDialogFragment addressFilterDialogFragment = BaseDialogFragment.newInstance(AddressFilterDialog.class, filterIndex, addressDataArrayList);
        addressFilterDialogFragment.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if(((AddressFilterDialog)dialog).isComplete) {
                    filterIndex = ((AddressFilterDialog)dialog).user_INDEX;
                    updateLayout();
                }
            }
        });
        addressFilterDialogFragment.restoreListeners(this, "address_filter");

        FloatingActionButton fab_address_filter = findViewById(R.id.address_portfolio_explorer_addressFilterButton);
        fab_address_filter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addressFilterDialogFragment.updateArguments(AddressFilterDialog.class, filterIndex, addressDataArrayList);
                addressFilterDialogFragment.show(AddressPortfolioExplorerActivity.this, "address_filter");
            }
        });

        ProgressDialogFragment refresh_progressDialogFragment = ProgressDialogFragment.newInstance(ProgressDialog.class);
        refresh_progressDialogFragment.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                ArrayList<CryptoAddress> cryptoAddressArrayList = addressPortfolioObj.cryptoAddressArrayList;

                newAddressDataArrayList[0] = new ArrayList<>();
                for(CryptoAddress cryptoAddress : cryptoAddressArrayList) {
                    if(((ProgressDialog)dialog).isCancelled) { return; }
                    newAddressDataArrayList[0].add(AddressData.getAddressData(cryptoAddress));
                    TokenManager.saveAll(AddressPortfolioExplorerActivity.this, "found");
                }
            }
        });

        refresh_progressDialogFragment.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                AddressData.alertUser(newAddressDataArrayList[0]);

                addressDataArrayList.clear();
                addressDataArrayList.addAll(newAddressDataArrayList[0]);
                updateLayout();
                Toast.showToast("refresh");
            }
        });
        refresh_progressDialogFragment.restoreListeners(this, "progress_refresh");

        FloatingActionButton fab_refresh = findViewById(R.id.address_portfolio_explorer_refreshButton);
        fab_refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                refresh_progressDialogFragment.show(AddressPortfolioExplorerActivity.this, "progress_refresh");
            }
        });

        FloatingActionButton fab_qrcode = findViewById(R.id.address_portfolio_explorer_qrCodeButton);
        fab_qrcode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BaseDialogFragment.newInstance(AddressQRCodeDialog.class, addressDataArrayList).show(AddressPortfolioExplorerActivity.this, "qrcode");
            }
        });

        updateLayout();
    }

    public void updateLayout() {
        table.resetTable();
        TextView T = findViewById(R.id.address_portfolio_explorer_infoTextView);

        if(filterIndex == -1) {
            T.setText("Portfolio = " + addressPortfolioObj.name);
            table.addRowsFromAddressDataArray(addressDataArrayList);
        }
        else {
            T.setText("Portfolio = " + addressPortfolioObj.name + "\nAddress = " + addressPortfolioObj.cryptoAddressArrayList.get(filterIndex).toString());
            table.addRowsFromAddressData(addressDataArrayList.get(filterIndex));
        }
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
            BaseDialogFragment.newInstance(CryptoPricesDialog.class).show(AddressPortfolioExplorerActivity.this, "price");
            return true;
        }
        else if (id == 2) {
            BaseDialogFragment.newInstance(CryptoConverterDialog.class).show(AddressPortfolioExplorerActivity.this, "converter");
            return true;
        }
        else if (id == 3) {
            BaseDialogFragment.newInstance(ReportFeedbackDialog.class).show(AddressPortfolioExplorerActivity.this, "feedback");
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle bundle) {
        super.onSaveInstanceState(bundle);
        bundle.putInt("filterIndex", filterIndex);
        bundle.putString("addressDataArrayList", Serialization.serializeArrayList(addressDataArrayList));
    }

    @Override
    public void onRestoreInstanceState(Bundle bundle) {
        if(bundle != null) {
            filterIndex = bundle.getInt("filterIndex", -1);
            addressDataArrayList = Serialization.deserializeArrayList(bundle.getString("addressDataArrayList"), AddressData.class);

            updateLayout();
        }

        super.onRestoreInstanceState(bundle);
    }
}