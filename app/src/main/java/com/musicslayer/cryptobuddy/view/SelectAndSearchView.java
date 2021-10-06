package com.musicslayer.cryptobuddy.view;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupMenu;

import androidx.appcompat.widget.AppCompatButton;

import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.asset.Asset;
import com.musicslayer.cryptobuddy.asset.crypto.coin.Coin;
import com.musicslayer.cryptobuddy.asset.crypto.token.Token;
import com.musicslayer.cryptobuddy.asset.fiat.Fiat;
import com.musicslayer.cryptobuddy.asset.tokenmanager.TokenManager;
import com.musicslayer.cryptobuddy.crash.CrashLinearLayout;
import com.musicslayer.cryptobuddy.crash.CrashOnClickListener;
import com.musicslayer.cryptobuddy.crash.CrashOnDismissListener;
import com.musicslayer.cryptobuddy.crash.CrashOnMenuItemClickListener;
import com.musicslayer.cryptobuddy.dialog.BaseDialogFragment;
import com.musicslayer.cryptobuddy.dialog.SearchDialog;
import com.musicslayer.cryptobuddy.persistence.Purchases;
import com.musicslayer.cryptobuddy.persistence.Settings;
import com.musicslayer.cryptobuddy.util.Serialization;
import com.musicslayer.cryptobuddy.util.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

// This class is hardcoded to deal with all crypto, and optionally Fiat as well.

// TODO We show a token multiple times if it is >1 of downloaded, found, and custom.
// In the help, we should also explain this somewhere...?

public class SelectAndSearchView extends CrashLinearLayout {
    public BorderedSpinnerView bsv;
    BaseDialogFragment searchAssetDialogFragment;
    public String lastButton;
    public Asset lastSearchAsset;
    public boolean includesFiat;

    public ArrayList<String> options_fiat_text_sorted;
    public ArrayList<Fiat> options_fiat_sorted;
    public ArrayList<String> options_coin_text_sorted;
    public ArrayList<Coin> options_coin_sorted;
    public ArrayList<String> options_token_text_sorted;
    public ArrayList<Token> options_token_sorted;

    AppCompatButton B_FIAT;
    AppCompatButton B_COIN;
    AppCompatButton B_TOKEN;
    AppCompatButton B_SEARCH;

    public SelectAndSearchView(Context context) {
        this(context, null);
    }

    public SelectAndSearchView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);

        // Initialize the sorted lists for Fiats and Coins.
        options_fiat_sorted = Fiat.fiats;
        options_coin_sorted = Coin.coins;

        if("full".equals(Settings.setting_asset)) {
            options_fiat_text_sorted = Fiat.fiat_display_names;
            options_coin_text_sorted = Coin.coin_display_names;

            Collections.sort(options_fiat_text_sorted, getComparatorString());
            Collections.sort(options_fiat_sorted, getNameComparatorAsset());

            Collections.sort(options_coin_text_sorted, getComparatorString());
            Collections.sort(options_coin_sorted, getNameComparatorAsset());
        }
        else {
            options_fiat_text_sorted = Fiat.fiat_names;
            options_coin_text_sorted = Coin.coin_names;

            Collections.sort(options_fiat_text_sorted, getComparatorString());
            Collections.sort(options_fiat_sorted, getSymbolComparatorAsset());

            Collections.sort(options_coin_text_sorted, getComparatorString());
            Collections.sort(options_coin_sorted, getSymbolComparatorAsset());
        }

        this.makeLayout();
    }

    public Comparator<String> getComparatorString() {
        return new Comparator<String>() {
            @Override
            public int compare(String a, String b) {
                return a.toLowerCase().compareTo(b.toLowerCase());
            }
        };
    }

    public Comparator<Asset> getSymbolComparatorAsset() {
        return new Comparator<Asset>() {
            @Override
            public int compare(Asset a, Asset b) {
                return a.getName().toLowerCase().compareTo(b.getName().toLowerCase());
            }
        };
    }

    public Comparator<Asset> getNameComparatorAsset() {
        return new Comparator<Asset>() {
            @Override
            public int compare(Asset a, Asset b) {
                return a.getDisplayName().toLowerCase().compareTo(b.getDisplayName().toLowerCase());
            }
        };
    }

    public void setIncludesFiat(boolean includesFiat) {
        this.includesFiat = includesFiat;

        // Remake layout to refresh Fiat button visibility and search options.
        this.removeAllViews();
        makeLayout();
    }

    public void makeLayout() {
        // Top row are buttons to filter spinner, or open search dialog.
        // Bottom row is the spinner.
        this.setOrientation(VERTICAL);

        Context context = getContext();

        B_FIAT = new AppCompatButton(context);
        B_FIAT.setText("FIAT");
        B_FIAT.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_radio_button_unchecked_small_24, 0, 0, 0);
        B_FIAT.setOnClickListener(new CrashOnClickListener(context) {
            public void onClickImpl(View v) {
                setOptionsFiat();
            }
        });

        if(includesFiat) {
            B_FIAT.setVisibility(VISIBLE);
        }
        else {
            B_FIAT.setVisibility(GONE);
        }

        B_COIN = new AppCompatButton(context);
        B_COIN.setText("COIN");
        B_COIN.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_radio_button_unchecked_small_24, 0, 0, 0);
        B_COIN.setOnClickListener(new CrashOnClickListener(context) {
            public void onClickImpl(View v) {
                setOptionsCoin();
            }
        });

        B_TOKEN = new AppCompatButton(context);
        B_TOKEN.setText("TOKEN");
        B_TOKEN.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_radio_button_unchecked_small_24, 0, 0, 0);
        B_TOKEN.setOnClickListener(new CrashOnClickListener(context) {
            public void onClickImpl(View v) {
                // Show list of token types, or tell user there are none.
                if(TokenManager.getAllTokens().isEmpty()) {
                    Toast.showToast(context,"no_tokens");
                }
                else {
                    PopupMenu popup = new PopupMenu(context, B_TOKEN);

                    ArrayList<String> options = new ArrayList<>();
                    for(TokenManager tokenManager : TokenManager.tokenManagers) {
                        ArrayList<Token> tokens = tokenManager.getTokens();
                        if(!tokens.isEmpty()) {
                            options.add(tokenManager.getTokenType());
                        }
                    }

                    Collections.sort(options, getComparatorString());
                    for(String o : options) {
                        popup.getMenu().add(o);
                    }

                    popup.setOnMenuItemClickListener(new CrashOnMenuItemClickListener(context) {
                        public boolean onMenuItemClickImpl(MenuItem item) {
                            // Since we exclude token types with 0 items, we cannot rely on index position.
                            setOptionsToken(item.toString());
                            return true;
                        }
                    });
                    popup.show();
                }
            }
        });

        bsv = new BorderedSpinnerView(context);

        searchAssetDialogFragment = BaseDialogFragment.newInstance(SearchDialog.class, getSearchAssets(), getSearchOptionsSymbols(), getSearchOptionsNames());
        searchAssetDialogFragment.setOnDismissListener(new CrashOnDismissListener(context) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                if(((SearchDialog)dialog).isComplete) {
                    // Just show the one chosen option.
                    setOptionsSearch(((SearchDialog)dialog).user_OPTION);
                }
            }
        });
        searchAssetDialogFragment.restoreListeners(context, "search");

        B_SEARCH = new AppCompatButton(context);
        B_SEARCH.setText("");
        B_SEARCH.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_radio_button_unchecked_small_24, 0, R.drawable.ic_baseline_search_24, 0);
        B_SEARCH.setOnClickListener(new CrashOnClickListener(context) {
            public void onClickImpl(View v) {
                searchAssetDialogFragment.show(context, "search");
            }
        });

        LinearLayout L_A = new LinearLayout(context);
        L_A.setOrientation(HORIZONTAL);
        L_A.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        L_A.addView(B_FIAT);
        L_A.addView(B_COIN);

        if(Purchases.isUnlockTokensPurchased) {
            L_A.addView(B_TOKEN);
        }

        L_A.addView(B_SEARCH);

        LinearLayout L_B = new LinearLayout(context);
        L_B.setOrientation(HORIZONTAL);
        L_B.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        L_B.addView(bsv);

        this.addView(L_A);
        this.addView(L_B);
    }

    public void setOptionsFiat() {
        bsv.setOptions(options_fiat_text_sorted);

        lastButton = "!FIAT!";

        B_FIAT.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_radio_button_checked_small_24, 0, 0, 0);
        B_COIN.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_radio_button_unchecked_small_24, 0, 0, 0);
        B_TOKEN.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_radio_button_unchecked_small_24, 0, 0, 0);
        B_SEARCH.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_radio_button_unchecked_small_24, 0, R.drawable.ic_baseline_search_24, 0);
    }

    public void setOptionsCoin() {
        bsv.setOptions(options_coin_text_sorted);

        lastButton = "!COIN!";

        B_FIAT.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_radio_button_unchecked_small_24, 0, 0, 0);
        B_COIN.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_radio_button_checked_small_24, 0, 0, 0);
        B_TOKEN.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_radio_button_unchecked_small_24, 0, 0, 0);
        B_SEARCH.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_radio_button_unchecked_small_24, 0, R.drawable.ic_baseline_search_24, 0);
    }

    public void setOptionsToken(String tokenType) {
        TokenManager tokenManager = TokenManager.getTokenManagerFromTokenType(tokenType);

        // Initialize the sorted lists for this kind of token.
        options_token_sorted = tokenManager.getTokens();

        if("full".equals(Settings.setting_asset)) {
            options_token_text_sorted = tokenManager.getTokenDisplayNames();

            Collections.sort(options_token_text_sorted, getComparatorString());
            Collections.sort(options_token_sorted, getNameComparatorAsset());
        }
        else {
            options_token_text_sorted = tokenManager.getTokenNames();

            Collections.sort(options_token_text_sorted, getComparatorString());
            Collections.sort(options_token_sorted, getSymbolComparatorAsset());
        }

        bsv.setOptions(options_token_text_sorted);

        lastButton = tokenType;

        B_FIAT.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_radio_button_unchecked_small_24, 0, 0, 0);
        B_COIN.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_radio_button_unchecked_small_24, 0, 0, 0);
        B_TOKEN.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_radio_button_checked_small_24, 0, 0, 0);
        B_SEARCH.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_radio_button_unchecked_small_24, 0, R.drawable.ic_baseline_search_24, 0);
    }

    public void setOptionsSearch(Asset asset) {
        if("full".equals(Settings.setting_asset)) {
            bsv.setOptions(asset.getDisplayName());
        }
        else {
            bsv.setOptions(asset.getName());
        }

        lastButton = "!SEARCH!";
        lastSearchAsset = asset;

        B_FIAT.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_radio_button_unchecked_small_24, 0, 0, 0);
        B_COIN.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_radio_button_unchecked_small_24, 0, 0, 0);
        B_TOKEN.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_radio_button_unchecked_small_24, 0, 0, 0);
        B_SEARCH.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_radio_button_checked_small_24, 0, R.drawable.ic_baseline_search_24, 0);
    }

    public Asset getChosenAsset() {
        int idx = bsv.spinner.getSelectedItemPosition();

        if("!FIAT!".equals(lastButton)) {
            //return Fiat.fiats.get(idx);
            return options_fiat_sorted.get(idx);
        }
        else if("!COIN!".equals(lastButton)) {
            //return Coin.coins.get(idx);
            return options_coin_sorted.get(idx);
        }
        else if("!SEARCH!".equals(lastButton)) {
            return lastSearchAsset;
        }
        else {
            // Token
            return options_token_sorted.get(idx);
        }
    }

    public void restoreOptions(String lastButton, Asset lastSearchAsset) {
        if("!FIAT!".equals(lastButton)) {
            setOptionsFiat();
        }
        else if("!COIN!".equals(lastButton)) {
            setOptionsCoin();
        }
        else if("!SEARCH!".equals(lastButton)) {
            setOptionsSearch(lastSearchAsset);
        }
        else {
            // Token
            setOptionsToken(lastButton);
        }
    }

    private ArrayList<String> getSearchOptionsSymbols() {
        ArrayList<String> options = new ArrayList<>();
        if(includesFiat) {
            options.addAll(Fiat.fiat_names);
        }
        options.addAll(Coin.coin_names);
        options.addAll(TokenManager.getAllTokenNames());
        return options;
    }

    private ArrayList<String> getSearchOptionsNames() {
        ArrayList<String> options = new ArrayList<>();
        if(includesFiat) {
            options.addAll(Fiat.fiat_display_names);
        }
        options.addAll(Coin.coin_display_names);
        options.addAll(TokenManager.getAllTokenDisplayNames());
        return options;
    }

    private ArrayList<Asset> getSearchAssets() {
        ArrayList<Asset> assets = new ArrayList<>();
        if(includesFiat) {
            assets.addAll(Fiat.fiats);
        }
        assets.addAll(Coin.coins);
        assets.addAll(TokenManager.getAllTokens());
        return assets;
    }

    public static void swap(SelectAndSearchView ssvA, SelectAndSearchView ssvB) {
        // Switch the states of 2 instances.
        String lastButtonA = ssvA.lastButton;
        String lastButtonB = ssvB.lastButton;
        Asset laseSearchAssetA = ssvA.lastSearchAsset;
        Asset laseSearchAssetB = ssvB.lastSearchAsset;
        int idxA = ssvA.bsv.spinner.getSelectedItemPosition();
        int idxB = ssvB.bsv.spinner.getSelectedItemPosition();

        ssvA.restoreOptions(lastButtonB, laseSearchAssetB);
        ssvB.restoreOptions(lastButtonA, laseSearchAssetA);

        ssvA.bsv.setSelection(idxB);
        ssvB.bsv.setSelection(idxA);
    }

    @Override
    public Parcelable onSaveInstanceStateImpl(Parcelable state)
    {
        Bundle bundle = new Bundle();
        bundle.putParcelable("superState", state);
        bundle.putInt("selection", this.bsv.spinner.getSelectedItemPosition());
        bundle.putString("lastButton", lastButton);

        String lastSearchAsset_s = lastSearchAsset == null ? "{}" : Serialization.serialize(lastSearchAsset);
        bundle.putString("lastSearchAsset", lastSearchAsset_s);

        return bundle;
    }

    @Override
    public Parcelable onRestoreInstanceStateImpl(Parcelable state)
    {
        if (state instanceof Bundle) // implicit null check
        {
            Bundle bundle = (Bundle) state;
            state = bundle.getParcelable("superState");
            lastButton = bundle.getString("lastButton");

            String lastSearchAsset_s = bundle.getString("lastSearchAsset");
            lastSearchAsset = "{}".equals(lastSearchAsset_s) ? null : Serialization.deserialize(lastSearchAsset_s, Asset.class);

            restoreOptions(lastButton, lastSearchAsset);
            this.bsv.setSelection(bundle.getInt("selection"));
        }
        return state;
    }
}
