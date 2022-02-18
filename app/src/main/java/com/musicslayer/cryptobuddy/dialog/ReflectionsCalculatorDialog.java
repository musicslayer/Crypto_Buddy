package com.musicslayer.cryptobuddy.dialog;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatButton;

import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.api.address.AddressData;
import com.musicslayer.cryptobuddy.api.address.CryptoAddress;
import com.musicslayer.cryptobuddy.asset.Asset;
import com.musicslayer.cryptobuddy.asset.coinmanager.CoinManager;
import com.musicslayer.cryptobuddy.asset.crypto.Crypto;
import com.musicslayer.cryptobuddy.asset.crypto.token.Token;
import com.musicslayer.cryptobuddy.asset.tokenmanager.TokenManager;
import com.musicslayer.cryptobuddy.crash.CrashDialogInterface;
import com.musicslayer.cryptobuddy.crash.CrashView;
import com.musicslayer.cryptobuddy.persistence.TokenManagerList;
import com.musicslayer.cryptobuddy.rich.RichStringBuilder;
import com.musicslayer.cryptobuddy.data.Serialization;
import com.musicslayer.cryptobuddy.transaction.AssetAmount;
import com.musicslayer.cryptobuddy.transaction.AssetQuantity;
import com.musicslayer.cryptobuddy.transaction.Transaction;
import com.musicslayer.cryptobuddy.util.HashMapUtil;
import com.musicslayer.cryptobuddy.util.HelpUtil;
import com.musicslayer.cryptobuddy.util.ToastUtil;
import com.musicslayer.cryptobuddy.view.asset.SelectAndSearchView;
import com.musicslayer.cryptobuddy.view.red.NumericEditText;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;

public class ReflectionsCalculatorDialog extends BaseDialog {
    CryptoAddress cryptoAddress;

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

        BaseDialogFragment chooseAddressDialogFragment = BaseDialogFragment.newInstance(ChooseAddressDialog.class);
        chooseAddressDialogFragment.setOnDismissListener(new CrashDialogInterface.CrashOnDismissListener(activity) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                if(((ChooseAddressDialog)dialog).isComplete) {
                    // Always include tokens regardless of user choice.
                    cryptoAddress = ((ChooseAddressDialog)dialog).user_CRYPTOADDRESS;
                    cryptoAddress.includeTokens = true;

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
                ProgressDialogFragment.updateProgressTitle("Scanning For Tokens...");

                // Search the address balances and transactions for any tokens.
                // If any new tokens are found, save them here.
                // Then, when the layout is updated, they will be added to the available options.
                AddressData.getAllData(cryptoAddress);
                TokenManagerList.saveAllData(activity);
            }
        });
        progressDialogFragment.setOnDismissListener(new CrashDialogInterface.CrashOnDismissListener(activity) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                updateLayout();
            }
        });
        progressDialogFragment.restoreListeners(activity, "progress");

        AppCompatButton scanButton = findViewById(R.id.reflections_calculator_dialog_scanButton);
        scanButton.setOnClickListener(new CrashView.CrashOnClickListener(activity) {
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

        SelectAndSearchView ssv = findViewById(R.id.reflections_calculator_dialog_selectAndSearchView);
        NumericEditText E_TAX = findViewById(R.id.reflections_calculator_dialog_percentageTaxEditText);

        ProgressDialogFragment reflectionsProgressDialogFragment = ProgressDialogFragment.newInstance(ProgressDialog.class);
        reflectionsProgressDialogFragment.setOnShowListener(new CrashDialogInterface.CrashOnShowListener(activity) {
            @Override
            public void onShowImpl(DialogInterface dialog) {
                ProgressDialogFragment.updateProgressTitle("Downloading Reflections Data...");

                // We need all data for the address to perform the calculation.
                AddressData reflectionsAddressData = AddressData.getSingleAllData(cryptoAddress, (Crypto)ssv.getChosenAsset());

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
                    // Convert percentage to decimal.
                    // With reflections, only sends are taxed.
                    BigDecimal D_RECEIVETAX = BigDecimal.ONE;
                    BigDecimal D_SENDTAX = BigDecimal.ONE.add(new BigDecimal(E_TAX.getTextString()).movePointLeft(2));

                    ArrayList<AssetQuantity> reflectionsCurrentBalanceArrayList = reflectionsAddressData.currentBalanceArrayList;
                    AssetQuantity reflectionsCurrentBalanceAssetQuantity = reflectionsCurrentBalanceArrayList.get(0);

                    HashMap<Asset, AssetAmount> reflectionsTransactionsMap = Transaction.resolveAssets(reflectionsAddressData.transactionArrayList, D_RECEIVETAX, D_SENDTAX);
                    AssetAmount reflectionsTransactionsAssetAmount = HashMapUtil.getValueFromMap(reflectionsTransactionsMap, ssv.getChosenAsset());

                    AssetQuantity resultAssetQuantity = new AssetQuantity(reflectionsCurrentBalanceAssetQuantity.assetAmount.subtract(reflectionsTransactionsAssetAmount), (Crypto)ssv.getChosenAsset());

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
                else if(ssv.getChosenAsset() == null) {
                    ToastUtil.showToast(activity,"must_choose_assets");
                }
                else if(!isValid) {
                    ToastUtil.showToast(activity,"must_fill_inputs");
                }
                else {
                    reflectionsProgressDialogFragment.show(activity, "progress_reflections");
                }
            }
        });

        updateLayout();
    }

    public void updateLayout() {
        TextView T_ADDRESS = findViewById(R.id.reflections_calculator_dialog_addressTextView);
        SelectAndSearchView ssv = findViewById(R.id.reflections_calculator_dialog_selectAndSearchView);

        if(cryptoAddress == null) {
            T_ADDRESS.setVisibility(View.GONE);
            ssv.setVisibility(View.GONE);
        }
        else {
            // Show the address but don't include the coins/tokens part.
            T_ADDRESS.setVisibility(View.VISIBLE);
            T_ADDRESS.setText(cryptoAddress.toSimpleString());

            // Set crypto
            ssv.setVisibility(View.VISIBLE);
            ssv.setIncludesFiat(false);
            ssv.setIncludesCoin(true);
            ssv.setIncludesToken(true);

            ssv.setCoinOptions(cryptoAddress.getCoins());

            ArrayList<CoinManager> coinManagerArrayList = new ArrayList<>();
            coinManagerArrayList.add(CoinManager.getDefaultCoinManager());
            ssv.setCoinManagerOptions(coinManagerArrayList);

            ArrayList<Token> tokenArrayList = new ArrayList<>();
            for(TokenManager tokenManager : cryptoAddress.getTokenManagers()) {
                tokenArrayList.addAll(tokenManager.getTokens());
            }
            ssv.setTokenOptions(tokenArrayList);

            ssv.setTokenManagerOptions(cryptoAddress.getTokenManagers());

            ssv.chooseCoin("BASE");
        }
    }

    @Override
    public Bundle onSaveInstanceStateImpl(Bundle bundle) {
        bundle.putParcelable("cryptoAddress", cryptoAddress);
        return bundle;
    }

    @Override
    public void onRestoreInstanceStateImpl(Bundle bundle) {
        if(bundle != null) {
            cryptoAddress = bundle.getParcelable("cryptoAddress");
        }
    }
}
