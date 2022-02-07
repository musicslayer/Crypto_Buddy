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
import androidx.appcompat.widget.AppCompatTextView;

import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.asset.Asset;
import com.musicslayer.cryptobuddy.asset.coinmanager.CoinManager;
import com.musicslayer.cryptobuddy.asset.crypto.coin.Coin;
import com.musicslayer.cryptobuddy.asset.crypto.token.Token;
import com.musicslayer.cryptobuddy.asset.fiat.Fiat;
import com.musicslayer.cryptobuddy.asset.fiatmanager.FiatManager;
import com.musicslayer.cryptobuddy.asset.tokenmanager.TokenManager;
import com.musicslayer.cryptobuddy.crash.CrashDialogInterface;
import com.musicslayer.cryptobuddy.crash.CrashLinearLayout;
import com.musicslayer.cryptobuddy.crash.CrashPopupMenu;
import com.musicslayer.cryptobuddy.crash.CrashView;
import com.musicslayer.cryptobuddy.dialog.BaseDialogFragment;
import com.musicslayer.cryptobuddy.dialog.SearchDialog;
import com.musicslayer.cryptobuddy.persistence.Purchases;
import com.musicslayer.cryptobuddy.settings.setting.AssetDisplaySetting;
import com.musicslayer.cryptobuddy.settings.setting.DefaultCoinSetting;
import com.musicslayer.cryptobuddy.settings.setting.DefaultFiatSetting;
import com.musicslayer.cryptobuddy.state.StateObj;
import com.musicslayer.cryptobuddy.util.HashMapUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

public class SelectAndSearchView extends CrashLinearLayout {
    public BorderedSpinnerView bsv;
    BaseDialogFragment searchAssetDialogFragment;
    public String lastButton;
    public Asset lastSearchAsset;

    public boolean includesFiat;
    public boolean includesCoin;
    public boolean includesToken;

    public ArrayList<String> options_fiat_setting_names_sorted = new ArrayList<>();
    public ArrayList<Fiat> options_fiat_sorted = new ArrayList<>();
    public ArrayList<String> options_coin_setting_names_sorted = new ArrayList<>();
    public ArrayList<Coin> options_coin_sorted = new ArrayList<>();
    public HashMap<String, ArrayList<String>> options_token_setting_names_sorted = new HashMap<>();
    public HashMap<String, ArrayList<Token>> options_token_sorted = new HashMap<>();

    public ArrayList<String> options_token_types = new ArrayList<>();

    // Create separate set of arrays for SearchDialog
    public ArrayList<Fiat> search_options_fiats = new ArrayList<>();
    public ArrayList<String> search_options_fiat_names = new ArrayList<>();
    public ArrayList<String> search_options_fiat_display_names = new ArrayList<>();

    public ArrayList<Coin> search_options_coins = new ArrayList<>();
    public ArrayList<String> search_options_coin_names = new ArrayList<>();
    public ArrayList<String> search_options_coin_display_names = new ArrayList<>();

    public HashMap<String, ArrayList<Token>> search_options_tokens = new HashMap<>();
    public HashMap<String, ArrayList<String>> search_options_tokens_names = new HashMap<>();
    public HashMap<String, ArrayList<String>> search_options_tokens_display_names = new HashMap<>();

    public ArrayList<String> search_options_token_types = new ArrayList<>();

    AppCompatButton B_FIAT;
    AppCompatButton B_COIN;
    AppCompatButton B_TOKEN;
    AppCompatButton B_SEARCH;

    public SelectAndSearchView(Context context) {
        this(context, null);
    }

    public SelectAndSearchView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.makeLayout();
    }

    public Comparator<String> getComparatorString() {
        return Comparator.comparing(String::toLowerCase);
    }

    public Comparator<Asset> getSettingComparatorAsset() {
        if("full".equals(AssetDisplaySetting.value)) {
            return getDisplayNameComparatorAsset();
        }
        else {
            return getNameComparatorAsset();
        }
    }

    public Comparator<Asset> getNameComparatorAsset() {
        return Comparator.comparing(a -> a.getName().toLowerCase());
    }

    public Comparator<Asset> getDisplayNameComparatorAsset() {
        return Comparator.comparing(a -> a.getDisplayName().toLowerCase());
    }

    public void setIncludesFiat(boolean includesFiat) {
        this.includesFiat = includesFiat;
    }

    public void setIncludesCoin(boolean includesCoin) {
        this.includesCoin = includesCoin;
    }

    public void setIncludesToken(boolean includesToken) {
        this.includesToken = includesToken;
    }

    public void setCompleteOptions() {
        // All available assets will be shown as options.
        // TODO Act like tokens ("base" is the only option)
        setFiatOptions(FiatManager.getAllFiats());
        setCoinOptions(CoinManager.getAllCoins());
        setTokenOptions(TokenManager.getAllTokens());
        setTokenManagerOptions(TokenManager.tokenManagers);
    }

    public void setFiatOptions(ArrayList<Fiat> fiatArrayList) {
        // Initialize the sorted lists for Fiats.
        search_options_fiats.clear();
        search_options_fiat_names.clear();
        search_options_fiat_display_names.clear();
        options_fiat_setting_names_sorted.clear();
        options_fiat_sorted.clear();

        for(Fiat fiat : fiatArrayList) {
            search_options_fiats.add(fiat);
            search_options_fiat_names.add(fiat.getName());
            search_options_fiat_display_names.add(fiat.getDisplayName());

            options_fiat_setting_names_sorted.add(fiat.getSettingName());
            options_fiat_sorted.add(fiat);
        }

        Collections.sort(options_fiat_setting_names_sorted, getComparatorString());
        Collections.sort(options_fiat_sorted, getSettingComparatorAsset());
    }

    public void setCoinOptions(ArrayList<Coin> coinArrayList) {
        // Initialize the sorted lists for Coins.
        search_options_coins.clear();
        search_options_coin_names.clear();
        search_options_coin_display_names.clear();
        options_coin_setting_names_sorted.clear();
        options_coin_sorted.clear();

        for(Coin coin : coinArrayList) {
            search_options_coins.add(coin);
            search_options_coin_names.add(coin.getName());
            search_options_coin_display_names.add(coin.getDisplayName());

            options_coin_setting_names_sorted.add(coin.getSettingName());
            options_coin_sorted.add(coin);
        }

        Collections.sort(options_coin_setting_names_sorted, getComparatorString());
        Collections.sort(options_coin_sorted, getSettingComparatorAsset());
    }

    public void setTokenOptions(ArrayList<Token> tokenArrayList) {
        // For each token, separate it by type.
        // The user can only see one type of token at a time.

        // Initialize the maps for Tokens.
        search_options_tokens.clear();
        search_options_tokens_names.clear();
        search_options_tokens_display_names.clear();
        options_token_setting_names_sorted.clear();
        options_token_sorted.clear();

        for(Token token : tokenArrayList) {
            String tokenType = token.getAssetType();

            ArrayList<Token> searchTokens = HashMapUtil.getValueFromMap(search_options_tokens, tokenType);
            if(searchTokens == null) {
                searchTokens = new ArrayList<>();
            }
            searchTokens.add(token);
            HashMapUtil.putValueInMap(search_options_tokens, tokenType, searchTokens);

            ArrayList<String> searchNames = HashMapUtil.getValueFromMap(search_options_tokens_names, tokenType);
            if(searchNames == null) {
                searchNames = new ArrayList<>();
            }
            searchNames.add(token.getName());
            HashMapUtil.putValueInMap(search_options_tokens_names, tokenType, searchNames);

            ArrayList<String> searchDisplayNames = HashMapUtil.getValueFromMap(search_options_tokens_display_names, tokenType);
            if(searchDisplayNames == null) {
                searchDisplayNames = new ArrayList<>();
            }
            searchDisplayNames.add(token.getDisplayName());
            HashMapUtil.putValueInMap(search_options_tokens_display_names, tokenType, searchDisplayNames);

            ArrayList<String> tokenSettingNamesSorted = HashMapUtil.getValueFromMap(options_token_setting_names_sorted, tokenType);
            if(tokenSettingNamesSorted == null) {
                tokenSettingNamesSorted = new ArrayList<>();
            }
            tokenSettingNamesSorted.add(token.getSettingName());
            HashMapUtil.putValueInMap(options_token_setting_names_sorted, tokenType, tokenSettingNamesSorted);

            ArrayList<Token> tokensSorted = HashMapUtil.getValueFromMap(options_token_sorted, tokenType);
            if(tokensSorted == null) {
                tokensSorted = new ArrayList<>();
            }
            tokensSorted.add(token);
            HashMapUtil.putValueInMap(options_token_sorted, tokenType, tokensSorted);
        }

        for(String tokenType : new ArrayList<>(options_token_setting_names_sorted.keySet())) {
            ArrayList<String> tokenSettingNamesSorted = HashMapUtil.getValueFromMap(options_token_setting_names_sorted, tokenType);
            Collections.sort(tokenSettingNamesSorted, getComparatorString());
            HashMapUtil.putValueInMap(options_token_setting_names_sorted, tokenType, tokenSettingNamesSorted);
        }

        for(String tokenType : new ArrayList<>(options_token_sorted.keySet())) {
            ArrayList<Token> tokensSorted = HashMapUtil.getValueFromMap(options_token_sorted, tokenType);
            Collections.sort(tokensSorted, getSettingComparatorAsset());
            HashMapUtil.putValueInMap(options_token_sorted, tokenType, tokensSorted);
        }
    }

    public void setTokenManagerOptions(ArrayList<TokenManager> tokenManagerArrayList) {
        // For now, we only choose the token types.
        // All tokens with a chosen token type are available.
        search_options_token_types.clear();
        options_token_types.clear();

        for(TokenManager tokenManager : tokenManagerArrayList) {
            search_options_token_types.add(tokenManager.getTokenType());
            options_token_types.add(tokenManager.getTokenType());
        }

        Collections.sort(options_token_types, getComparatorString());
    }

    public void chooseFiat() {
        lastButton = "!FIAT!";

        // Remake layout to refresh button visibility and search options.
        this.removeAllViews();
        makeLayout();

        bsv.setOptions(options_fiat_setting_names_sorted);

        // Choose the default, but if that is not an option then choose the first.
        Fiat defaultFiat = DefaultFiatSetting.value;
        int idx = options_fiat_sorted.indexOf(defaultFiat);
        if(idx == -1) {
            idx = 0;
        }
        this.bsv.setSelection(idx);

        B_FIAT.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_radio_button_checked_small_24, 0, 0, 0);
        B_COIN.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_radio_button_unchecked_small_24, 0, 0, 0);
        B_TOKEN.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_radio_button_unchecked_small_24, 0, 0, 0);
        B_SEARCH.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_radio_button_unchecked_small_24, 0, R.drawable.ic_baseline_search_24, 0);
    }

    public void chooseCoin() {
        lastButton = "!COIN!";

        // Remake layout to refresh button visibility and search options.
        this.removeAllViews();
        makeLayout();

        bsv.setOptions(options_coin_setting_names_sorted);

        // Choose the default, but if that is not an option then choose the first.
        Coin defaultCoin = DefaultCoinSetting.value;
        int idx = options_coin_sorted.indexOf(defaultCoin);
        if(idx == -1) {
            idx = 0;
        }
        this.bsv.setSelection(idx);

        B_FIAT.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_radio_button_unchecked_small_24, 0, 0, 0);
        B_COIN.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_radio_button_checked_small_24, 0, 0, 0);
        B_TOKEN.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_radio_button_unchecked_small_24, 0, 0, 0);
        B_SEARCH.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_radio_button_unchecked_small_24, 0, R.drawable.ic_baseline_search_24, 0);
    }

    public void chooseToken(String tokenType) {
        // Initialize the sorted lists for this kind of token.
        // There is no "DefaultTokenSetting" - the first token will always be chosen.
        lastButton = tokenType;

        if(tokenType != null) {
            ArrayList<Token> tokenArrayList = TokenManager.getTokenManagerFromTokenType(tokenType).getTokens();
            setTokenOptions(tokenArrayList);
        }

        // Remake layout to refresh button visibility and search options.
        this.removeAllViews();
        makeLayout();

        if(tokenType != null) {
            ArrayList<String> settingNames = HashMapUtil.getValueFromMap(options_token_setting_names_sorted, tokenType);
            if(settingNames != null) {
                bsv.setOptions(settingNames);
            }
        }

        B_FIAT.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_radio_button_unchecked_small_24, 0, 0, 0);
        B_COIN.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_radio_button_unchecked_small_24, 0, 0, 0);
        B_TOKEN.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_radio_button_checked_small_24, 0, 0, 0);
        B_SEARCH.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_radio_button_unchecked_small_24, 0, R.drawable.ic_baseline_search_24, 0);
    }

    public void chooseSearch(Asset asset) {
        lastButton = "!SEARCH!";
        lastSearchAsset = asset;

        // Remake layout to refresh button visibility and search options.
        this.removeAllViews();
        makeLayout();

        if(asset != null) {
            bsv.setOptions(asset.getSettingName());
        }

        B_FIAT.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_radio_button_unchecked_small_24, 0, 0, 0);
        B_COIN.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_radio_button_unchecked_small_24, 0, 0, 0);
        B_TOKEN.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_radio_button_unchecked_small_24, 0, 0, 0);
        B_SEARCH.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_radio_button_checked_small_24, 0, R.drawable.ic_baseline_search_24, 0);
    }

    public void makeLayout() {
        // Top row are buttons to filter spinner, or open search dialog.
        // Bottom row is the spinner.
        this.setOrientation(VERTICAL);

        Context context = getContext();

        B_FIAT = new AppCompatButton(context);
        B_FIAT.setText("FIAT");
        B_FIAT.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_radio_button_unchecked_small_24, 0, 0, 0);
        B_FIAT.setOnClickListener(new CrashView.CrashOnClickListener(context) {
            public void onClickImpl(View v) {
                chooseFiat();
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
        B_COIN.setOnClickListener(new CrashView.CrashOnClickListener(context) {
            public void onClickImpl(View v) {
                chooseCoin();
            }
        });

        if(includesCoin) {
            B_COIN.setVisibility(VISIBLE);
        }
        else {
            B_COIN.setVisibility(GONE);
        }

        B_TOKEN = new AppCompatButton(context);
        B_TOKEN.setText("TOKEN");
        B_TOKEN.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_radio_button_unchecked_small_24, 0, 0, 0);
        B_TOKEN.setOnClickListener(new CrashView.CrashOnClickListener(context) {
            public void onClickImpl(View v) {
                if(options_token_types.isEmpty()) {
                    // There are no token types to choose from.
                    chooseToken(null);
                }
                else if(options_token_types.size() == 1) {
                    // There is only one token type, so just choose it.
                    chooseToken(options_token_types.get(0));
                }
                else {
                    // Present a list of available token types to choose from.
                    PopupMenu popup = new PopupMenu(context, B_TOKEN);

                    ArrayList<String> options = new ArrayList<>(options_token_types);
                    Collections.sort(options, getComparatorString());
                    for(String o : options) {
                        popup.getMenu().add(o);
                    }

                    popup.setOnMenuItemClickListener(new CrashPopupMenu.CrashOnMenuItemClickListener(context) {
                        public boolean onMenuItemClickImpl(MenuItem item) {
                            chooseToken(item.toString());
                            return true;
                        }
                    });
                    popup.show();
                }
            }
        });

        if(includesToken) {
            B_TOKEN.setVisibility(VISIBLE);
        }
        else {
            B_TOKEN.setVisibility(GONE);
        }

        AppCompatTextView T = new AppCompatTextView(context);
        bsv = new BorderedSpinnerView(context);

        searchAssetDialogFragment = BaseDialogFragment.newInstance(SearchDialog.class);
        searchAssetDialogFragment.setOnDismissListener(new CrashDialogInterface.CrashOnDismissListener(context) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                if(((SearchDialog)dialog).isComplete) {
                    // Just show the one chosen option.
                    chooseSearch(((SearchDialog)dialog).user_OPTION);
                }
            }
        });
        searchAssetDialogFragment.restoreListeners(context, "search");

        B_SEARCH = new AppCompatButton(context);
        B_SEARCH.setText("");
        B_SEARCH.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_radio_button_unchecked_small_24, 0, R.drawable.ic_baseline_search_24, 0);
        B_SEARCH.setOnClickListener(new CrashView.CrashOnClickListener(context) {
            public void onClickImpl(View v) {
                if(getSearchOptionsAssets().isEmpty()) {
                    chooseSearch(null);
                }
                else {
                    StateObj.search_options_assets = getSearchOptionsAssets();
                    StateObj.search_options_asset_names = getSearchOptionsNames();
                    StateObj.search_options_asset_display_names = getSearchOptionsDisplayNames();
                    searchAssetDialogFragment.show(context, "search");
                }
            }
        });

        LinearLayout L_A = new LinearLayout(context);
        L_A.setOrientation(HORIZONTAL);
        L_A.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        L_A.addView(B_FIAT);
        L_A.addView(B_COIN);

        if(Purchases.isUnlockTokensPurchased()) {
            L_A.addView(B_TOKEN);
        }

        L_A.addView(B_SEARCH);

        LinearLayout L_B = new LinearLayout(context);
        L_B.setOrientation(HORIZONTAL);
        L_B.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        boolean isEmpty;
        String text;
        if("!FIAT!".equals(lastButton)) {
            text = "No Fiats Available";
            isEmpty = options_fiat_sorted.isEmpty();
        }
        else if("!COIN!".equals(lastButton)) {
            text = "No Coins Available";
            isEmpty = options_coin_sorted.isEmpty();
        }
        else if("!SEARCH!".equals(lastButton)) {
            // If "search" is selected, there must have been at least one option to choose from.
            text = "No Assets to Search";
            isEmpty = lastSearchAsset == null;
        }
        else if(lastButton != null) {
            // Token and token type were selected.
            text = "No " + lastButton + " Tokens Available";
            ArrayList<Token> tokens = HashMapUtil.getValueFromMap(options_token_sorted, lastButton);
            isEmpty = tokens == null || tokens.isEmpty();
        }
        else {
            // Token was selected, but there are no token types available.
            text = "No Tokens Available";
            isEmpty = true;
        }

        if(isEmpty) {
            T.setText(text);
            L_B.addView(T);
        }
        else {
            L_B.addView(bsv);
        }

        this.addView(L_A);
        this.addView(L_B);
    }

    public Asset getChosenAsset() {
        int idx = bsv.spinner.getSelectedItemPosition();

        if("!FIAT!".equals(lastButton)) {
            if(options_fiat_sorted.isEmpty()) {
                return null;
            }
            else {
                return options_fiat_sorted.get(idx);
            }
        }
        else if("!COIN!".equals(lastButton)) {
            if(options_coin_sorted.isEmpty()) {
                return null;
            }
            else {
                return options_coin_sorted.get(idx);
            }
        }
        else if("!SEARCH!".equals(lastButton)) {
            return lastSearchAsset;
        }
        else if(lastButton != null) {
            // Token and token type were selected.
            ArrayList<Token> tokens = HashMapUtil.getValueFromMap(options_token_sorted, lastButton);
            if(tokens == null || tokens.isEmpty()) {
                return null;
            }
            else {
                return tokens.get(idx);
            }
        }
        else {
            // Token was selected, but there are no token types available.
            return null;
        }
    }

    public void restoreOptions(String lastButton, Asset lastSearchAsset) {
        if("!FIAT!".equals(lastButton)) {
            chooseFiat();
        }
        else if("!COIN!".equals(lastButton)) {
            chooseCoin();
        }
        else if("!SEARCH!".equals(lastButton)) {
            chooseSearch(lastSearchAsset);
        }
        else {
            // Token was selected.
            // There may or may not be any token types available.
            chooseToken(lastButton);
        }
    }

    private ArrayList<String> getSearchOptionsNames() {
        ArrayList<String> options = new ArrayList<>();
        if(includesFiat) {
            options.addAll(search_options_fiat_names);
        }
        if(includesCoin) {
            options.addAll(search_options_coin_names);
        }
        if(includesToken) {
            for(String tokenType : search_options_token_types) {
                ArrayList<String> searchNames = HashMapUtil.getValueFromMap(search_options_tokens_names, tokenType);
                if(searchNames != null) {
                    options.addAll(searchNames);
                }
            }
        }
        return options;
    }

    private ArrayList<String> getSearchOptionsDisplayNames() {
        ArrayList<String> options = new ArrayList<>();
        if(includesFiat) {
            options.addAll(search_options_fiat_display_names);
        }
        if(includesCoin) {
            options.addAll(search_options_coin_display_names);
        }
        if(includesToken) {
            for(String tokenType : search_options_token_types) {
                ArrayList<String> searchDisplayNames = HashMapUtil.getValueFromMap(search_options_tokens_display_names, tokenType);
                if(searchDisplayNames != null) {
                    options.addAll(searchDisplayNames);
                }
            }
        }
        return options;
    }

    private ArrayList<Asset> getSearchOptionsAssets() {
        ArrayList<Asset> assets = new ArrayList<>();
        if(includesFiat) {
            assets.addAll(search_options_fiats);
        }
        if(includesCoin) {
            assets.addAll(search_options_coins);
        }
        if(includesToken) {
            for(String tokenType : search_options_token_types) {
                ArrayList<Token> searchTokens = HashMapUtil.getValueFromMap(search_options_tokens, tokenType);
                if(searchTokens != null) {
                    assets.addAll(searchTokens);
                }
            }
        }
        return assets;
    }

    public static void swap(SelectAndSearchView ssvA, SelectAndSearchView ssvB) {
        // Switch the states of 2 instances.
        // It is assumed that the two instances have the same available options.
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
        bundle.putParcelable("lastSearchAsset", lastSearchAsset);

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
            lastSearchAsset = bundle.getParcelable("lastSearchAsset");

            restoreOptions(lastButton, lastSearchAsset);
            this.bsv.setSelection(bundle.getInt("selection"));
        }
        return state;
    }
}
