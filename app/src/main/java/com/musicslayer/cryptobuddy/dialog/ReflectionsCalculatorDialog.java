package com.musicslayer.cryptobuddy.dialog;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatButton;

import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.api.address.AddressData;
import com.musicslayer.cryptobuddy.api.address.CryptoAddress;
import com.musicslayer.cryptobuddy.asset.Asset;
import com.musicslayer.cryptobuddy.asset.crypto.Crypto;
import com.musicslayer.cryptobuddy.crash.CrashAdapterView;
import com.musicslayer.cryptobuddy.crash.CrashDialogInterface;
import com.musicslayer.cryptobuddy.crash.CrashView;
import com.musicslayer.cryptobuddy.persistence.TokenManagerList;
import com.musicslayer.cryptobuddy.rich.RichStringBuilder;
import com.musicslayer.cryptobuddy.serialize.Serialization;
import com.musicslayer.cryptobuddy.transaction.AssetAmount;
import com.musicslayer.cryptobuddy.transaction.AssetQuantity;
import com.musicslayer.cryptobuddy.transaction.Transaction;
import com.musicslayer.cryptobuddy.util.HashMapUtil;
import com.musicslayer.cryptobuddy.util.HelpUtil;
import com.musicslayer.cryptobuddy.util.ToastUtil;
import com.musicslayer.cryptobuddy.view.BorderedSpinnerView;
import com.musicslayer.cryptobuddy.view.red.NumericEditText;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;

// TODO Do we need to download the first address data, or can we just let the pick a token...

public class ReflectionsCalculatorDialog extends BaseDialog {
    CryptoAddress cryptoAddress;
    ArrayList<Crypto> cryptoArrayList;
    Crypto crypto;

    public ReflectionsCalculatorDialog(Activity activity) {
        super(activity);
    }

    public int getBaseViewID() {
        return R.id.reflections_calculator_dialog;
    }

    public void createLayout(Bundle savedInstanceState) {
        setContentView(R.layout.dialog_reflections_calculator);

        ImageButton helpButton = findViewById(R.id.reflections_calculator_dialog_helpButton);
        helpButton.setOnClickListener(new CrashView.CrashOnClickListener(this.activity) {
            @Override
            public void onClickImpl(View view) {
                HelpUtil.showHelp(activity, R.raw.help_reflections_calculator);
            }
        });

        // TODO Don't show "Token" toggle button.
        BaseDialogFragment chooseAddressDialogFragment = BaseDialogFragment.newInstance(ChooseAddressDialog.class);
        chooseAddressDialogFragment.setOnDismissListener(new CrashDialogInterface.CrashOnDismissListener(activity) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                if(((ChooseAddressDialog)dialog).isComplete) {
                    cryptoAddress = ((ChooseAddressDialog)dialog).user_CRYPTOADDRESS;
                    cryptoArrayList = null;
                    updateLayout();
                }
            }
        });
        chooseAddressDialogFragment.restoreListeners(activity, "address");

        Button B_ADDRESS_EXPLORER = findViewById(R.id.reflections_calculator_dialog_addressButton);
        B_ADDRESS_EXPLORER.setOnClickListener(new CrashView.CrashOnClickListener(activity) {
            @Override
            public void onClickImpl(View view) {
                chooseAddressDialogFragment.show(activity, "address");
            }
        });

        ProgressDialogFragment progressDialogFragment = ProgressDialogFragment.newInstance(ProgressDialog.class);
        progressDialogFragment.setOnShowListener(new CrashDialogInterface.CrashOnShowListener(activity) {
            @Override
            public void onShowImpl(DialogInterface dialog) {
                ProgressDialogFragment.updateProgressTitle("Downloading Address Data...");

                // We need all data for the address to perform the calculation.
                AddressData newAddressData = AddressData.getAllData(cryptoAddress);

                // Save found tokens, potentially from multiple TokenManagers.
                TokenManagerList.saveAllData(activity);

                ProgressDialogFragment.setValue(Serialization.serialize(newAddressData));
            }
        });
        progressDialogFragment.setOnDismissListener(new CrashDialogInterface.CrashOnDismissListener(activity) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                AddressData newAddressData = Serialization.deserialize(ProgressDialogFragment.getValue(), AddressData.class);

                if(newAddressData.isComplete()) {
                    // Don't merge anything. Just store all the cryptos that were seen.
                    cryptoArrayList = getCryptoArrayList(newAddressData);
                    ToastUtil.showToast(activity,"address_data_downloaded");
                }
                else {
                    // Do not process incomplete data.
                    cryptoArrayList = null;
                    ToastUtil.showToast(activity,"incomplete_address_data");
                }

                updateLayout();
            }
        });
        progressDialogFragment.restoreListeners(activity, "progress");

        AppCompatButton downloadDataButton = findViewById(R.id.reflections_calculator_dialog_downloadButton);
        downloadDataButton.setOnClickListener(new CrashView.CrashOnClickListener(activity) {
            @Override
            public void onClickImpl(View view) {
                if(cryptoAddress == null) {
                    ToastUtil.showToast(activity, "must_choose_address");
                }
                else {
                    progressDialogFragment.show(activity, "progress");
                }
            }
        });

        BorderedSpinnerView bsv_crypto = findViewById(R.id.reflections_calculator_dialog_cryptoSpinner);
        bsv_crypto.setOptions(new ArrayList<>());
        bsv_crypto.setOnItemSelectedListener(new CrashAdapterView.CrashOnItemSelectedListener(this.activity) {
            public void onNothingSelectedImpl(AdapterView<?> parent){}
            public void onItemSelectedImpl(AdapterView<?> parent, View view, int pos, long id) {
                crypto = cryptoArrayList.get(pos);
            }
        });

        NumericEditText E_TAX = findViewById(R.id.reflections_calculator_dialog_percentageTaxEditText);

        ProgressDialogFragment reflectionsProgressDialogFragment = ProgressDialogFragment.newInstance(ProgressDialog.class);
        reflectionsProgressDialogFragment.setOnShowListener(new CrashDialogInterface.CrashOnShowListener(activity) {
            @Override
            public void onShowImpl(DialogInterface dialog) {
                ProgressDialogFragment.updateProgressTitle("Downloading Reflections Data...");

                // We need all data for the address to perform the calculation.
                AddressData reflectionsAddressData = AddressData.getSingleAllData(cryptoAddress, crypto);

                // Save found tokens, potentially from multiple TokenManagers.
                TokenManagerList.saveAllData(activity);

                ProgressDialogFragment.setValue(Serialization.serialize(reflectionsAddressData));
            }
        });
        reflectionsProgressDialogFragment.setOnDismissListener(new CrashDialogInterface.CrashOnDismissListener(activity) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                AddressData reflectionsAddressData = Serialization.deserialize(ProgressDialogFragment.getValue(), AddressData.class);

                if(reflectionsAddressData.isComplete()) {
                    // Convert percentage to decimal
                    BigDecimal D_TAX = new BigDecimal(E_TAX.getTextString()).movePointLeft(2);

                    ArrayList<AssetQuantity> reflectionsCurrentBalanceArrayList = reflectionsAddressData.currentBalanceArrayList;
                    AssetQuantity reflectionsCurrentBalanceAssetQuantity = reflectionsCurrentBalanceArrayList.get(0);

                    HashMap<Asset, AssetAmount> reflectionsTransactionsMap = Transaction.resolveAssets2(reflectionsAddressData.transactionArrayList, D_TAX);
                    AssetAmount reflectionsTransactionsAssetAmount = HashMapUtil.getValueFromMap(reflectionsTransactionsMap, crypto);

                    AssetQuantity resultAssetQuantity = new AssetQuantity(reflectionsCurrentBalanceAssetQuantity.assetAmount.subtract(reflectionsTransactionsAssetAmount), crypto);

                    RichStringBuilder s = new RichStringBuilder(true);
                    s.appendRich("Reflections = ");
                    s.appendAssetQuantity(resultAssetQuantity);

                    TextView T_RESULT = findViewById(R.id.reflections_calculator_dialog_resultsTextView);
                    T_RESULT.setText(Html.fromHtml(s.toString()));
                }
                else {
                    // Do not process incomplete data.
                    ToastUtil.showToast(activity,"incomplete_reflections_data");
                }
            }
        });
        reflectionsProgressDialogFragment.restoreListeners(activity, "progress_reflections");

        AppCompatButton calculateButton = findViewById(R.id.reflections_calculator_dialog_calculateButton);
        calculateButton.setOnClickListener(new CrashView.CrashOnClickListener(activity) {
            @Override
            public void onClickImpl(View view) {
                // Test this even if we don't fulfill the other conditions.
                boolean isValid = E_TAX.test();

                if(cryptoAddress == null) {
                    ToastUtil.showToast(activity, "must_choose_address");
                }
                else if(cryptoArrayList == null) {
                    ToastUtil.showToast(activity, "must_download_data");
                }
                else if(isValid) {
                    reflectionsProgressDialogFragment.show(activity, "progress_reflections");
                }
            }
        });

        updateLayout();
    }

    public void updateLayout() {
        TextView T_ADDRESS = findViewById(R.id.reflections_calculator_dialog_addressTextView);
        TextView T_DOWNLOAD = findViewById(R.id.reflections_calculator_dialog_downloadTextView);
        BorderedSpinnerView bsv_crypto = findViewById(R.id.reflections_calculator_dialog_cryptoSpinner);

        if(cryptoAddress == null) {
            T_ADDRESS.setText("");
        }
        else {
            T_ADDRESS.setText(cryptoAddress.toString());
        }

        if(cryptoArrayList == null) {
            T_DOWNLOAD.setText("");
            bsv_crypto.setVisibility(View.GONE);
        }
        else {
            T_DOWNLOAD.setText("Data Downloaded");
            bsv_crypto.setVisibility(View.VISIBLE);

            // Update options.
            ArrayList<String> options = new ArrayList<>();
            for(Crypto crypto : cryptoArrayList) {
                options.add(crypto.getSettingName());
            }

            bsv_crypto.setOptions(options);
        }
    }

    public ArrayList<Crypto> getCryptoArrayList(AddressData addressData) {
        ArrayList<Crypto> cryptoArrayList = new ArrayList<>();

        // Ensure there is at least one entry.
        cryptoArrayList.add(cryptoAddress.getCrypto());

        // Everything here should be cryptos, but check anyway.
        // Also only add something once.

        // Check Balance Data
        for(AssetQuantity assetQuantity : addressData.currentBalanceArrayList) {
            Asset asset = assetQuantity.asset;
            if(asset instanceof Crypto && !cryptoArrayList.contains((Crypto)asset)) {
                cryptoArrayList.add((Crypto)asset);
            }
        }

        // Check Transaction Data
        for(Transaction transaction : addressData.transactionArrayList) {
            AssetQuantity actionedAssetQuantity = transaction.actionedAssetQuantity;
            Asset actionedAsset = actionedAssetQuantity.asset;
            if(actionedAsset instanceof Crypto && !cryptoArrayList.contains((Crypto)actionedAsset)) {
                cryptoArrayList.add((Crypto)actionedAsset);
            }

            AssetQuantity otherAssetQuantity = transaction.actionedAssetQuantity;
            if(otherAssetQuantity != null) {
                Asset otherAsset = otherAssetQuantity.asset;
                if(otherAsset instanceof Crypto && !cryptoArrayList.contains((Crypto)otherAsset)) {
                    cryptoArrayList.add((Crypto)otherAsset);
                }
            }
        }

        return cryptoArrayList;
    }

    @Override
    public Bundle onSaveInstanceStateImpl(Bundle bundle) {
        bundle.putParcelable("cryptoAddress", cryptoAddress);
        bundle.putParcelableArrayList("cryptoArrayList", cryptoArrayList);
        bundle.putParcelable("crypto", crypto);

        return bundle;
    }

    @Override
    public void onRestoreInstanceStateImpl(Bundle bundle) {
        if(bundle != null) {
            cryptoAddress = bundle.getParcelable("cryptoAddress");
            cryptoArrayList = bundle.getParcelableArrayList("cryptoArrayList");
            crypto = bundle.getParcelable("crypto");
        }
    }
}
