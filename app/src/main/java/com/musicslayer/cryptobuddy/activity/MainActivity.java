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
import com.musicslayer.cryptobuddy.crash.CrashDialogInterface;
import com.musicslayer.cryptobuddy.crash.CrashView;
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
import com.musicslayer.cryptobuddy.persistence.TokenManagerList;
import com.musicslayer.cryptobuddy.util.HelpUtil;
import com.musicslayer.cryptobuddy.serialize.Serialization;
import com.musicslayer.cryptobuddy.util.ToastUtil;

import java.lang.ref.WeakReference;

public class MainActivity extends BaseActivity {
    WeakReference<BaseDialogFragment> chooseAddressDialogFragment_w;
    WeakReference<BaseDialogFragment> privacyPolicyDialogFragment_w;
    WeakReference<BaseDialogFragment> reviewDialogFragment_w;
    WeakReference<ProgressDialogFragment> progressDialogFragment_w;

    final static CryptoAddress[] cryptoAddress = new CryptoAddress[1];

    public int getAdLayoutViewID() {
        return R.id.main_adLayout;
    }

    @Override
    public void onBackPressedImpl() {
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
        helpButton.setOnClickListener(new CrashView.CrashOnClickListener(this) {
            @Override
            public void onClickImpl(View view) {
                HelpUtil.showHelp(MainActivity.this, R.raw.help_main);
            }
        });

        Button B_TRANSACTION_EXPLORER = findViewById(R.id.main_transactionExplorerButton);
        B_TRANSACTION_EXPLORER.setOnClickListener(new CrashView.CrashOnClickListener(this) {
            @Override
            public void onClickImpl(View view) {
                Intent intent = new Intent(MainActivity.this, TransactionExplorerActivity.class);
                MainActivity.this.startActivity(intent);

                MainActivity.this.finish();
            }
        });

        Button B_TRANSACTION_PORTFOLIO = findViewById(R.id.main_transactionPortfolioViewerButton);
        B_TRANSACTION_PORTFOLIO.setOnClickListener(new CrashView.CrashOnClickListener(this) {
            @Override
            public void onClickImpl(View view) {
                startActivity(new Intent(MainActivity.this, TransactionPortfolioViewerActivity.class));
                finish();
            }
        });

        progressDialogFragment_w = new WeakReference<>(ProgressDialogFragment.newInstance(ProgressDialog.class));
        progressDialogFragment_w.get().setOnShowListener(new CrashDialogInterface.CrashOnShowListener(this) {
            @Override
            public void onShowImpl(DialogInterface dialog) {
                AddressData addressData = AddressData.getAllData(cryptoAddress[0]);

                // Save found tokens, potentially from multiple TokenManagers.
                TokenManagerList.saveAllData(MainActivity.this);

                ProgressDialogFragment.setValue(Serialization.serialize(addressData));
            }
        });

        progressDialogFragment_w.get().setOnDismissListener(new CrashDialogInterface.CrashOnDismissListener(this) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                AddressData addressData = Serialization.deserialize(ProgressDialogFragment.getValue(), AddressData.class);

                if(!addressData.isComplete()) {
                    ToastUtil.showToast(MainActivity.this,"no_address_data");
                }

                Intent intent = new Intent(MainActivity.this, AddressExplorerActivity.class);

                // TODO we should only pass the cryptoaddress here, NOT all the addressdata, which could be super large based on balance/transaction count. Then we can increase the setting limit.
                // Note that serialization does shrink this somewhat.
                intent.putExtra("AddressData", Serialization.serialize(addressData));
                MainActivity.this.startActivity(intent);

                MainActivity.this.finish();
            }
        });
        progressDialogFragment_w.get().restoreListeners(this, "progress");

        chooseAddressDialogFragment_w = new WeakReference<>(BaseDialogFragment.newInstance(ChooseAddressDialog.class));
        chooseAddressDialogFragment_w.get().setOnDismissListener(new CrashDialogInterface.CrashOnDismissListener(this) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                if(((ChooseAddressDialog)dialog).isComplete) {
                    cryptoAddress[0] = ((ChooseAddressDialog)dialog).user_CRYPTOADDRESS;

                    progressDialogFragment_w.get().show(MainActivity.this, "progress");
                }
            }
        });
        chooseAddressDialogFragment_w.get().restoreListeners(this, "address");

        Button B_ADDRESS_EXPLORER = findViewById(R.id.main_addressExplorerButton);
        B_ADDRESS_EXPLORER.setOnClickListener(new CrashView.CrashOnClickListener(this) {
            @Override
            public void onClickImpl(View view) {
                chooseAddressDialogFragment_w.get().show(MainActivity.this, "address");
            }
        });

        Button B_ADDRESS_PORTFOLIO = findViewById(R.id.main_addressPortfolioViewerButton);
        B_ADDRESS_PORTFOLIO.setOnClickListener(new CrashView.CrashOnClickListener(this) {
            @Override
            public void onClickImpl(View view) {
                startActivity(new Intent(MainActivity.this, AddressPortfolioViewerActivity.class));
                finish();
            }
        });

        Button bCryptoPrices = findViewById(R.id.main_cryptoPricesButton);
        bCryptoPrices.setOnClickListener(new CrashView.CrashOnClickListener(this) {
            @Override
            public void onClickImpl(View view) {
                BaseDialogFragment.newInstance(CryptoPricesDialog.class).show(MainActivity.this, "price");
            }
        });

        Button bCryptoPriceConverter = findViewById(R.id.main_cryptoConverterButton);
        bCryptoPriceConverter.setOnClickListener(new CrashView.CrashOnClickListener(this) {
            @Override
            public void onClickImpl(View view) {
                BaseDialogFragment.newInstance(CryptoConverterDialog.class).show(MainActivity.this, "converter");
            }
        });

        Button B_INAPP = findViewById(R.id.main_inAppPurchasesButton);
        B_INAPP.setOnClickListener(new CrashView.CrashOnClickListener(this) {
            @Override
            public void onClickImpl(View view) {
                startActivity(new Intent(MainActivity.this, InAppPurchasesActivity.class));
                finish();
            }
        });

        Button B_TokenManager = findViewById(R.id.main_tokenManagerButton);
        B_TokenManager.setOnClickListener(new CrashView.CrashOnClickListener(this) {
            @Override
            public void onClickImpl(View view) {
                if(Purchases.isUnlockTokensPurchased) {
                    startActivity(new Intent(MainActivity.this, TokenManagerActivity.class));
                    finish();
                }
                else {
                    ToastUtil.showToast(MainActivity.this,"unlock_tokens_required");
                }
            }
        });

        Button B_LOCK = findViewById(R.id.main_lockButton);
        if(App.DEBUG) {
            B_LOCK.setOnClickListener(new CrashView.CrashOnClickListener(this) {
                @Override
                public void onClickImpl(View view) {
                    InAppPurchase.lock(MainActivity.this);
                    ToastUtil.showToast(MainActivity.this, "lock_purchases");
                }
            });
        }
        else {
            B_LOCK.setVisibility(View.GONE);
        }

        Button B_UNLOCK = findViewById(R.id.main_unlockButton);
        if(App.DEBUG) {
            B_UNLOCK.setOnClickListener(new CrashView.CrashOnClickListener(this) {
                @Override
                public void onClickImpl(View view) {
                    InAppPurchase.unlock(MainActivity.this);
                    ToastUtil.showToast(MainActivity.this, "unlock_purchases");
                }
            });
        }
        else {
            B_UNLOCK.setVisibility(View.GONE);
        }

        Button B_REFUND = findViewById(R.id.main_refundButton);
        if(App.DEBUG) {
            B_REFUND.setOnClickListener(new CrashView.CrashOnClickListener(this) {
                @Override
                public void onClickImpl(View view) {
                    InAppPurchase.refund(MainActivity.this);
                }
            });
        }
        else {
            B_REFUND.setVisibility(View.GONE);
        }

        Button B_CRASH = findViewById(R.id.main_crashButton);
        if(App.DEBUG) {
            B_CRASH.setOnClickListener(new CrashView.CrashOnClickListener(this) {
                @Override
                public void onClickImpl(View view) {
                    throw new RuntimeException();
                }
            });
        }
        else {
            B_CRASH.setVisibility(View.GONE);
        }
    }

    public void checkPrivacyPolicy() {
        if(!PrivacyPolicy.settings_privacy_policy) {
            privacyPolicyDialogFragment_w = new WeakReference<>(BaseDialogFragment.newInstance(PrivacyPolicyDialog.class));
            privacyPolicyDialogFragment_w.get().setOnDismissListener(new CrashDialogInterface.CrashOnDismissListener(this) {
                @Override
                public void onDismissImpl(DialogInterface dialog) {
                    if(((PrivacyPolicyDialog)dialog).isComplete) {
                        finish();
                    }
                }
            });
            privacyPolicyDialogFragment_w.get().restoreListeners(this, "privacy_policy");

            if(BaseDialogFragment.isNotShowing(this, "privacy_policy")) {
                privacyPolicyDialogFragment_w.get().show(this, "privacy_policy");
            }
        }
    }

    public void checkReview() {
        // Check after 5 Days, and the user must have already agreed to the Privacy Policy
        if(PrivacyPolicy.settings_privacy_policy && (System.currentTimeMillis() - Review.settings_review_time > 432000000L)) {
            reviewDialogFragment_w = new WeakReference<>(BaseDialogFragment.newInstance(ReviewDialog.class));
            reviewDialogFragment_w.get().setOnDismissListener(new CrashDialogInterface.CrashOnDismissListener(this) {
                @Override
                public void onDismissImpl(DialogInterface dialog) {
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
            reviewDialogFragment_w.get().restoreListeners(this, "review");

            if(BaseDialogFragment.isNotShowing(this, "review")) {
                reviewDialogFragment_w.get().show(this, "review");
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenuImpl(Menu menu) {
        menu.add(0, 1, 100, "About");
        menu.add(0, 2, 200, "Settings");
        menu.add(0, 3, 300, "Report Feedback");
        menu.add(0, 4, 400, "Share App");
        return true;
    }

    @Override
    public boolean onOptionsItemSelectedImpl(MenuItem item) {
        int id = item.getItemId();

        if (id == 1) {
            startActivity(new Intent(MainActivity.this, AboutActivity.class));
            finish();
            return true;
        }
        else if (id == 2) {
            startActivity(new Intent(MainActivity.this, SettingsActivity.class));
            finish();
            return true;
        }
        else if (id == 3) {
            BaseDialogFragment.newInstance(ReportFeedbackDialog.class, "None", "").show(MainActivity.this, "feedback");
            return true;
        }
        else if (id == 4) {
            BaseDialogFragment.newInstance(ShareAppDialog.class).show(MainActivity.this, "share");
            return true;
        }

        return false;
    }
}