package com.musicslayer.cryptobuddy.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;

import com.musicslayer.cryptobuddy.api.address.AddressData;
import com.musicslayer.cryptobuddy.api.address.CryptoAddress;
import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.app.App;
import com.musicslayer.cryptobuddy.asset.tokenmanager.TokenManager;
import com.musicslayer.cryptobuddy.dialog.ChooseAddressDialog;
import com.musicslayer.cryptobuddy.dialog.CryptoConverterDialog;
import com.musicslayer.cryptobuddy.dialog.CryptoPricesDialog;
import com.musicslayer.cryptobuddy.dialog.PrivacyPolicyDialog;
import com.musicslayer.cryptobuddy.dialog.ProgressDialog;
import com.musicslayer.cryptobuddy.dialog.ProgressDialogFragment;
import com.musicslayer.cryptobuddy.dialog.ReportFeedbackDialog;
import com.musicslayer.cryptobuddy.dialog.BaseDialogFragment;
import com.musicslayer.cryptobuddy.dialog.ReviewDialog;
import com.musicslayer.cryptobuddy.dialog.ShareAppDialog;
import com.musicslayer.cryptobuddy.monetization.InAppPurchase;
import com.musicslayer.cryptobuddy.persistence.PrivacyPolicy;
import com.musicslayer.cryptobuddy.persistence.Purchases;
import com.musicslayer.cryptobuddy.persistence.Review;
import com.musicslayer.cryptobuddy.util.Help;
import com.musicslayer.cryptobuddy.util.Serialization;
import com.musicslayer.cryptobuddy.util.Toast;

import java.util.Date;

public class MainActivity extends BaseActivity {
    final static CryptoAddress[] cryptoAddress = new CryptoAddress[1];
    final static AddressData[] addressData = new AddressData[1];

    public int getAdLayoutViewID() {
        return R.id.main_adLayout;
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    public void createLayout () {
        // Before doing anything, show the privacy policy dialog if the user hasn't already agreed to it.
        checkPrivacyPolicy();

        // Check to see if we should request the user leaves us a review.
        checkReview();

        setContentView(R.layout.activity_main);

        TextView T = findViewById(R.id.main_messageTextView);
        if(App.isGooglePlayAvailable) {
            T.setVisibility(View.GONE);
        }

        Toolbar toolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);

        ImageButton helpButton = findViewById(R.id.main_helpButton);
        helpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Help.showHelp(MainActivity.this, R.raw.help_main);
            }
        });

        Button B_TRANSACTION_EXPLORER = findViewById(R.id.main_transactionExplorerButton);
        B_TRANSACTION_EXPLORER.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.this.finish();

                Intent intent = new Intent(MainActivity.this, TransactionExplorerActivity.class);
                MainActivity.this.startActivity(intent);
            }
        });

        Button B_TRANSACTION_PORTFOLIO = findViewById(R.id.main_transactionPortfolioViewerButton);
        B_TRANSACTION_PORTFOLIO.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                startActivity(new Intent(MainActivity.this, TransactionPortfolioViewerActivity.class));
            }
        });

        ProgressDialogFragment progressDialogFragment = ProgressDialogFragment.newInstance(ProgressDialog.class);
        progressDialogFragment.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                addressData[0] = AddressData.getAddressData(cryptoAddress[0]);
                TokenManager.saveAll(MainActivity.this, "found");
            }
        });

        progressDialogFragment.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                addressData[0].alertUser();

                MainActivity.this.finish();

                Intent intent = new Intent(MainActivity.this, AddressExplorerActivity.class);

                // TODO we should only pass the cryptoaddress here, NOT all the addressdata, which could be super large based on balance/transaction count. Then we can increase the setting limit.
                //intent.putExtra("AddressData", addressData[0]);
                intent.putExtra("AddressData", Serialization.serialize(addressData[0]));
                MainActivity.this.startActivity(intent);
            }
        });
        progressDialogFragment.restoreListeners(this, "progress");

        BaseDialogFragment chooseAddressDialogFragment = BaseDialogFragment.newInstance(ChooseAddressDialog.class);
        chooseAddressDialogFragment.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if(((ChooseAddressDialog)dialog).isComplete) {
                    cryptoAddress[0] = ((ChooseAddressDialog)dialog).user_CRYPTOADDRESS;

                    progressDialogFragment.show(MainActivity.this, "progress");
                }
            }
        });
        chooseAddressDialogFragment.restoreListeners(this, "address");

        Button B_ADDRESS_EXPLORER = findViewById(R.id.main_addressExplorerButton);
        B_ADDRESS_EXPLORER.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseAddressDialogFragment.show(MainActivity.this, "address");
            }
        });

        Button B_ADDRESS_PORTFOLIO = findViewById(R.id.main_addressPortfolioViewerButton);
        B_ADDRESS_PORTFOLIO.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                startActivity(new Intent(MainActivity.this, AddressPortfolioViewerActivity.class));
            }
        });

        Button bCryptoPrices = findViewById(R.id.main_cryptoPricesButton);
        bCryptoPrices.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BaseDialogFragment.newInstance(CryptoPricesDialog.class).show(MainActivity.this, "price");
            }
        });

        Button bCryptoPriceConverter = findViewById(R.id.main_cryptoConverterButton);
        bCryptoPriceConverter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BaseDialogFragment.newInstance(CryptoConverterDialog.class).show(MainActivity.this, "converter");
            }
        });

        Button B_INAPP = findViewById(R.id.main_inAppPurchasesButton);
        B_INAPP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                startActivity(new Intent(MainActivity.this, InAppPurchasesActivity.class));
            }
        });

        Button B_TokenManager = findViewById(R.id.main_tokenManagerButton);
        B_TokenManager.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(Purchases.isUnlockTokensPurchased) {
                    finish();
                    startActivity(new Intent(MainActivity.this, TokenManagerActivity.class));
                }
                else {
                    Toast.showToast("unlock_tokens_required");
                }
            }
        });

        // Internal use only!
        Button B_REFUND = findViewById(R.id.main_refundButton);
        //B_REFUND.setVisibility(View.GONE);

        B_REFUND.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                InAppPurchase.refund(getApplicationContext());
            }
        });
    }

    public void checkPrivacyPolicy() {
        if(!PrivacyPolicy.settings_privacy_policy) {
            BaseDialogFragment privacyPolicyDialogFragment = BaseDialogFragment.newInstance(PrivacyPolicyDialog.class);
            privacyPolicyDialogFragment.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    if(((PrivacyPolicyDialog)dialog).isComplete) {
                        finish();
                    }
                }
            });
            privacyPolicyDialogFragment.restoreListeners(this, "privacy_policy");

            if(!privacyPolicyDialogFragment.isShowing(this, "privacy_policy")) {
                privacyPolicyDialogFragment.show(this, "privacy_policy");
            }
        }
    }

    public void checkReview() {
        // Check after 5 Days, and the user must have already agreed to the Privacy Policy
        if(PrivacyPolicy.settings_privacy_policy && (new Date().getTime() - Review.settings_review_time > 432000000L)) {
            BaseDialogFragment reviewDialogFragment = BaseDialogFragment.newInstance(ReviewDialog.class);
            reviewDialogFragment.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    if(((ReviewDialog)dialog).isComplete) {
                        if(((ReviewDialog)dialog).user_LATER) {
                            Review.setReviewTime(MainActivity.this);
                        }
                        else {
                            Review.disableReviewTime(MainActivity.this);
                        }
                    }
                }
            });
            reviewDialogFragment.restoreListeners(this, "review");

            if(!reviewDialogFragment.isShowing(this, "review")) {
                reviewDialogFragment.show(this, "review");
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 1, 100, "About");
        menu.add(0, 2, 200, "Settings");
        menu.add(0, 3, 300, "Report Feedback");
        menu.add(0, 4, 400, "Share App");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == 1) {
            finish();
            startActivity(new Intent(MainActivity.this, AboutActivity.class));
            return true;
        }
        else if (id == 2) {
            finish();
            startActivity(new Intent(MainActivity.this, SettingsActivity.class));
            return true;
        }
        else if (id == 3) {
            BaseDialogFragment.newInstance(ReportFeedbackDialog.class).show(MainActivity.this, "feedback");
            return true;
        }
        else if (id == 4) {
            BaseDialogFragment.newInstance(ShareAppDialog.class).show(MainActivity.this, "share");
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}