package com.musicslayer.cryptobuddy.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;

import com.musicslayer.cryptobuddy.api.address.CryptoAddress;
import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.app.App;
import com.musicslayer.cryptobuddy.asset.exchange.Exchange;
import com.musicslayer.cryptobuddy.crash.CrashDialogInterface;
import com.musicslayer.cryptobuddy.crash.CrashRunnable;
import com.musicslayer.cryptobuddy.crash.CrashView;
import com.musicslayer.cryptobuddy.dialog.ChooseAddressDialog;
import com.musicslayer.cryptobuddy.dialog.ChooseExchangeDialog;
import com.musicslayer.cryptobuddy.dialog.CryptoConverterDialog;
import com.musicslayer.cryptobuddy.dialog.CryptoPricesDialog;
import com.musicslayer.cryptobuddy.dialog.DisclaimerDialog;
import com.musicslayer.cryptobuddy.dialog.PrivacyPolicyDialog;
import com.musicslayer.cryptobuddy.dialog.ReportFeedbackDialog;
import com.musicslayer.cryptobuddy.dialog.BaseDialogFragment;
import com.musicslayer.cryptobuddy.dialog.ReviewDialog;
import com.musicslayer.cryptobuddy.dialog.ShareAppDialog;
import com.musicslayer.cryptobuddy.monetization.InAppPurchase;
import com.musicslayer.cryptobuddy.persistence.Policy;
import com.musicslayer.cryptobuddy.persistence.Purchases;
import com.musicslayer.cryptobuddy.persistence.Review;
import com.musicslayer.cryptobuddy.util.HelpUtil;
import com.musicslayer.cryptobuddy.util.ToastUtil;

import java.util.Date;

public class MainActivity extends BaseActivity {
    @Override
    public int getAdLayoutViewID() {
        return R.id.main_adLayout;
    }

    @Override
    public void onBackPressedImpl() {
        finish();
    }

    @Override
    public void createLayout(Bundle savedInstanceState) {
        // Before doing anything, show policy dialogs that the user hasn't already agreed to.
        checkPrivacyPolicy();
        checkDisclaimer();

        // Check to see if we should request the user leaves us a review.
        checkReview();

        setContentView(R.layout.activity_main);

        multilineTextWorkaround();

        InAppPurchase.setInAppPurchaseListener(new InAppPurchase.InAppPurchaseListener() {
            @Override
            public void onInAppPurchase() {
                // Needed if the purchase update happened on another thread.
                runOnUiThread(new CrashRunnable(MainActivity.this) {
                    @Override
                    public void runImpl() {
                        updateLayout();
                    }
                });
            }
        });

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
                startActivity(new Intent(MainActivity.this, TransactionExplorerActivity.class));
                finish();
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

        BaseDialogFragment chooseAddressDialogFragment = BaseDialogFragment.newInstance(ChooseAddressDialog.class);
        chooseAddressDialogFragment.setOnDismissListener(new CrashDialogInterface.CrashOnDismissListener(this) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                if(((ChooseAddressDialog)dialog).isComplete) {
                    CryptoAddress cryptoAddress = ((ChooseAddressDialog)dialog).user_CRYPTOADDRESS;

                    Intent intent = new Intent(MainActivity.this, AddressExplorerActivity.class);
                    intent.putExtra("CryptoAddress", cryptoAddress);

                    MainActivity.this.startActivity(intent);
                    MainActivity.this.finish();
                }
            }
        });
        chooseAddressDialogFragment.restoreListeners(this, "address");

        Button B_ADDRESS_EXPLORER = findViewById(R.id.main_addressExplorerButton);
        B_ADDRESS_EXPLORER.setOnClickListener(new CrashView.CrashOnClickListener(this) {
            @Override
            public void onClickImpl(View view) {
                chooseAddressDialogFragment.show(MainActivity.this, "address");
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
                if(Purchases.isUnlockTokensPurchased()) {
                    startActivity(new Intent(MainActivity.this, TokenManagerActivity.class));
                    finish();
                }
                else {
                    ToastUtil.showToast(MainActivity.this,"unlock_tokens_required");
                }
            }
        });

        BaseDialogFragment chooseExchangeDialogFragment = BaseDialogFragment.newInstance(ChooseExchangeDialog.class);
        chooseExchangeDialogFragment.setOnDismissListener(new CrashDialogInterface.CrashOnDismissListener(this) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                if(((ChooseExchangeDialog)dialog).isComplete) {
                    Exchange exchange = ((ChooseExchangeDialog)dialog).user_EXCHANGE;

                    Intent intent = new Intent(MainActivity.this, ExchangeExplorerActivity.class);
                    intent.putExtra("Exchange", exchange);

                    MainActivity.this.startActivity(intent);
                    MainActivity.this.finish();
                }
            }
        });
        chooseExchangeDialogFragment.restoreListeners(this, "exchange");

        Button B_EXCHANGE_EXPLORER = findViewById(R.id.main_exchangeExplorerButton);
        B_EXCHANGE_EXPLORER.setOnClickListener(new CrashView.CrashOnClickListener(this) {
            @Override
            public void onClickImpl(View view) {
                if(Purchases.isUnlockExchangeIntegrationPurchased()) {
                    chooseExchangeDialogFragment.show(MainActivity.this, "exchange");
                }
                else {
                    ToastUtil.showToast(MainActivity.this,"unlock_exchange_integration_required");
                }
            }
        });

        Button B_EXCHANGE_PORTFOLIO = findViewById(R.id.main_exchangePortfolioViewerButton);
        B_EXCHANGE_PORTFOLIO.setOnClickListener(new CrashView.CrashOnClickListener(this) {
            @Override
            public void onClickImpl(View view) {
                if(Purchases.isUnlockExchangeIntegrationPurchased()) {
                    startActivity(new Intent(MainActivity.this, ExchangePortfolioViewerActivity.class));
                    finish();
                }
                else {
                    ToastUtil.showToast(MainActivity.this,"unlock_exchange_integration_required");
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

        updateLayout();
    }

    public void multilineTextWorkaround() {
        // When the app starts, buttons with multiline text that is horizontally centered shift their text quickly.
        // To work around this, make sure the second line of text is longer.
        Button B1 = findViewById(R.id.main_transactionExplorerButton);
        B1.setText("Transaction\n     Explorer     ");

        Button B2 = findViewById(R.id.main_transactionPortfolioViewerButton);
        B2.setText("Transaction\n     Portfolio     ");
    }

    public void updateLayout() {
        Button B_TokenManager = findViewById(R.id.main_tokenManagerButton);
        Button B_EXCHANGE_EXPLORER = findViewById(R.id.main_exchangeExplorerButton);
        Button B_EXCHANGE_PORTFOLIO = findViewById(R.id.main_exchangePortfolioViewerButton);

        if(Purchases.isUnlockPremiumFeaturesPurchased()) {
            B_TokenManager.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_api_24, 0, 0, 0);
            B_EXCHANGE_EXPLORER.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_forward_24, 0, 0, 0);
            B_EXCHANGE_PORTFOLIO.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_folder_24, 0, 0, 0);
        }
        else {
            B_TokenManager.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_lock_24, 0, 0, 0);
            B_EXCHANGE_EXPLORER.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_lock_24, 0, 0, 0);
            B_EXCHANGE_PORTFOLIO.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_lock_24, 0, 0, 0);
        }
    }

    public void checkPrivacyPolicy() {
        if(!Policy.settings_privacy_policy) {
            BaseDialogFragment privacyPolicyDialogFragment = BaseDialogFragment.newInstance(PrivacyPolicyDialog.class);
            privacyPolicyDialogFragment.setOnDismissListener(new CrashDialogInterface.CrashOnDismissListener(this) {
                @Override
                public void onDismissImpl(DialogInterface dialog) {
                    if(((PrivacyPolicyDialog)dialog).isComplete) {
                        finish();
                    }
                }
            });
            privacyPolicyDialogFragment.restoreListeners(this, "privacy_policy");

            // Since the dialog opens automatically on startup, use this to make sure dialog only shows once.
            if(BaseDialogFragment.isNotShowing(this, "privacy_policy")) {
                privacyPolicyDialogFragment.show(this, "privacy_policy");
            }
        }
    }

    public void checkDisclaimer() {
        if(!Policy.settings_disclaimer) {
            BaseDialogFragment disclaimerDialogFragment = BaseDialogFragment.newInstance(DisclaimerDialog.class);
            disclaimerDialogFragment.setOnDismissListener(new CrashDialogInterface.CrashOnDismissListener(this) {
                @Override
                public void onDismissImpl(DialogInterface dialog) {
                    if(((DisclaimerDialog)dialog).isComplete) {
                        finish();
                    }
                }
            });
            disclaimerDialogFragment.restoreListeners(this, "disclaimer");

            // Since the dialog opens automatically on startup, use this to make sure dialog only shows once.
            if(BaseDialogFragment.isNotShowing(this, "disclaimer")) {
                disclaimerDialogFragment.show(this, "disclaimer");
            }
        }
    }

    public void checkReview() {
        // Check after 5 Days, and the user must have already agreed to all the policies
        if(Policy.isAllPolicyAgreedTo() && (new Date().getTime() - Review.settings_review_time > 432000000L)) {
            BaseDialogFragment reviewDialogFragment = BaseDialogFragment.newInstance(ReviewDialog.class);
            reviewDialogFragment.setOnDismissListener(new CrashDialogInterface.CrashOnDismissListener(this) {
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
            reviewDialogFragment.restoreListeners(this, "review");

            if(BaseDialogFragment.isNotShowing(this, "review")) {
                reviewDialogFragment.show(this, "review");
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenuImpl(Menu menu) {
        menu.add(0, 1, 100, "About");
        menu.add(0, 2, 200, "Settings");
        menu.add(0, 3, 300, "Share App");
        menu.add(0, 4, 400, "Report Feedback");
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
            BaseDialogFragment.newInstance(ShareAppDialog.class).show(MainActivity.this, "share");
            return true;
        }
        else if (id == 4) {
            BaseDialogFragment.newInstance(ReportFeedbackDialog.class, "None").show(MainActivity.this, "feedback");
            return true;
        }

        return false;
    }
}