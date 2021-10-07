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
import com.musicslayer.cryptobuddy.crash.CrashDialogInterface;
import com.musicslayer.cryptobuddy.crash.CrashView;
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
import com.musicslayer.cryptobuddy.persistence.TokenManagerList;
import com.musicslayer.cryptobuddy.util.HelpUtil;
import com.musicslayer.cryptobuddy.util.InfoUtil;
import com.musicslayer.cryptobuddy.serialize.Serialization;
import com.musicslayer.cryptobuddy.util.ToastUtil;
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
        infoButton.setOnClickListener(new CrashView.CrashOnClickListener(this) {
            @Override
            public void onClickImpl(View view) {
                InfoUtil.showInfo(AddressPortfolioExplorerActivity.this, addressDataArrayList);
            }
        });

        if(!InfoUtil.hasInfo(addressDataArrayList)) {
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
                addressData[0] = AddressData.getAddressData(cryptoAddress[0]);
                TokenManagerList.saveAllData(AddressPortfolioExplorerActivity.this);
            }
        });

        add_progressDialogFragment.setOnDismissListener(new CrashDialogInterface.CrashOnDismissListener(this) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                if(!addressData[0].isComplete()) {
                    ToastUtil.showToast(AddressPortfolioExplorerActivity.this,"no_address_data");
                }

                addressPortfolioObj.addData(cryptoAddress[0]);
                AddressPortfolio.saveAllData(AddressPortfolioExplorerActivity.this);

                addressDataArrayList.add(addressData[0]);
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
                    cryptoAddress[0] = ((ChooseAddressDialog)dialog).user_CRYPTOADDRESS;

                    if(addressPortfolioObj.isSaved(cryptoAddress[0])) {
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
                BaseDialogFragment addressInfoDialogFragment = BaseDialogFragment.newInstance(AddressInfoDialog.class, addressDataArrayList);
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

        BaseDialogFragment addressFilterDialogFragment = BaseDialogFragment.newInstance(AddressFilterDialog.class, filterIndex, addressDataArrayList);
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

        FloatingActionButton fab_address_filter = findViewById(R.id.address_portfolio_explorer_addressFilterButton);
        fab_address_filter.setOnClickListener(new CrashView.CrashOnClickListener(this) {
            @Override
            public void onClickImpl(View view) {
                addressFilterDialogFragment.updateArguments(AddressFilterDialog.class, filterIndex, addressDataArrayList);
                addressFilterDialogFragment.show(AddressPortfolioExplorerActivity.this, "address_filter");
            }
        });

        ProgressDialogFragment refresh_progressDialogFragment = ProgressDialogFragment.newInstance(ProgressDialog.class);
        refresh_progressDialogFragment.setOnShowListener(new CrashDialogInterface.CrashOnShowListener(this) {
            @Override
            public void onShowImpl(DialogInterface dialog) {
                ArrayList<CryptoAddress> cryptoAddressArrayList = addressPortfolioObj.cryptoAddressArrayList;

                newAddressDataArrayList[0] = new ArrayList<>();
                for(CryptoAddress cryptoAddress : cryptoAddressArrayList) {
                    if(((ProgressDialog)dialog).isCancelled) { return; }
                    newAddressDataArrayList[0].add(AddressData.getAddressData(cryptoAddress));
                    TokenManagerList.saveAllData(AddressPortfolioExplorerActivity.this);
                }
            }
        });

        refresh_progressDialogFragment.setOnDismissListener(new CrashDialogInterface.CrashOnDismissListener(this) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                for(AddressData addressData : newAddressDataArrayList[0]) {
                    if(!addressData.isComplete()) {
                        // Only alert once. Others would be redundant.
                        ToastUtil.showToast(AddressPortfolioExplorerActivity.this,"no_address_data");
                        break;
                    }
                }

                addressDataArrayList.clear();
                addressDataArrayList.addAll(newAddressDataArrayList[0]);
                updateLayout();
                ToastUtil.showToast(AddressPortfolioExplorerActivity.this,"refresh");
            }
        });
        refresh_progressDialogFragment.restoreListeners(this, "progress_refresh");

        FloatingActionButton fab_refresh = findViewById(R.id.address_portfolio_explorer_refreshButton);
        fab_refresh.setOnClickListener(new CrashView.CrashOnClickListener(this) {
            @Override
            public void onClickImpl(View view) {
                refresh_progressDialogFragment.show(AddressPortfolioExplorerActivity.this, "progress_refresh");
            }
        });

        FloatingActionButton fab_qrcode = findViewById(R.id.address_portfolio_explorer_qrCodeButton);
        fab_qrcode.setOnClickListener(new CrashView.CrashOnClickListener(this) {
            @Override
            public void onClickImpl(View view) {
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
            table.addRowsFromAddressDataArray(this, addressDataArrayList);
        }
        else {
            T.setText("Portfolio = " + addressPortfolioObj.name + "\nAddress = " + addressPortfolioObj.cryptoAddressArrayList.get(filterIndex).toString());
            table.addRowsFromAddressData(this, addressDataArrayList.get(filterIndex));
        }
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
            BaseDialogFragment.newInstance(ReportFeedbackDialog.class).show(AddressPortfolioExplorerActivity.this, "feedback");
            return true;
        }

        return false;
    }

    @Override
    public void onSaveInstanceStateImpl(@NonNull Bundle bundle) {
        bundle.putInt("filterIndex", filterIndex);
        bundle.putString("addressDataArrayList", Serialization.serializeArrayList(addressDataArrayList));
    }

    @Override
    public void onRestoreInstanceStateImpl(Bundle bundle) {
        if(bundle != null) {
            filterIndex = bundle.getInt("filterIndex", -1);
            addressDataArrayList = Serialization.deserializeArrayList(bundle.getString("addressDataArrayList"), AddressData.class);

            updateLayout();
        }
    }
}