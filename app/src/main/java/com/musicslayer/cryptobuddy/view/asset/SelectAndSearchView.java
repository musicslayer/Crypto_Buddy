package com.musicslayer.cryptobuddy.view.asset;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;

import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatTextView;

import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.asset.Asset;
import com.musicslayer.cryptobuddy.asset.coinmanager.CoinManager;
import com.musicslayer.cryptobuddy.asset.crypto.coin.Coin;
import com.musicslayer.cryptobuddy.asset.crypto.token.Token;
import com.musicslayer.cryptobuddy.asset.fiat.Fiat;
import com.musicslayer.cryptobuddy.asset.fiatmanager.FiatManager;
import com.musicslayer.cryptobuddy.asset.tokenmanager.TokenManager;
import com.musicslayer.cryptobuddy.crash.CrashAdapterView;
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
import com.musicslayer.cryptobuddy.util.ToastUtil;
import com.musicslayer.cryptobuddy.view.BorderedSpinnerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

public class SelectAndSearchView extends CrashLinearLayout {
    public BorderedSpinnerView bsv;
    BaseDialogFragment searchAssetDialogFragment;
    ChooseAssetListener chooseAssetListener;

    public String lastButtonKind;
    public String lastButtonType;

    public boolean includesFiat;
    public boolean includesCoin;
    public boolean includesToken;

    public HashMap<String, ArrayList<Fiat>> options_fiats_sorted = new HashMap<>();
    public HashMap<String, ArrayList<String>> options_fiat_setting_names_sorted = new HashMap<>();
    public ArrayList<String> options_fiat_types = new ArrayList<>();

    public HashMap<String, ArrayList<Coin>> options_coins_sorted = new HashMap<>();
    public HashMap<String, ArrayList<String>> options_coin_setting_names_sorted = new HashMap<>();
    public ArrayList<String> options_coin_types = new ArrayList<>();

    public HashMap<String, ArrayList<Token>> options_tokens_sorted = new HashMap<>();
    public HashMap<String, ArrayList<String>> options_token_setting_names_sorted = new HashMap<>();
    public ArrayList<String> options_token_types = new ArrayList<>();

    // Create separate set of arrays for SearchDialog
    public HashMap<String, ArrayList<Fiat>> search_options_fiats = new HashMap<>();
    public HashMap<String, ArrayList<String>> search_options_fiat_names = new HashMap<>();
    public HashMap<String, ArrayList<String>> search_options_fiat_display_names = new HashMap<>();
    public ArrayList<String> search_options_fiat_types = new ArrayList<>();

    public HashMap<String, ArrayList<Coin>> search_options_coins = new HashMap<>();
    public HashMap<String, ArrayList<String>> search_options_coin_names = new HashMap<>();
    public HashMap<String, ArrayList<String>> search_options_coin_display_names = new HashMap<>();
    public ArrayList<String> search_options_coin_types = new ArrayList<>();

    public HashMap<String, ArrayList<Token>> search_options_tokens = new HashMap<>();
    public HashMap<String, ArrayList<String>> search_options_token_names = new HashMap<>();
    public HashMap<String, ArrayList<String>> search_options_token_display_names = new HashMap<>();
    public ArrayList<String> search_options_token_types = new ArrayList<>();

    AppCompatButton B_FIAT;
    AppCompatButton B_COIN;
    AppCompatButton B_TOKEN;
    AppCompatImageButton B_SEARCH;

    public SelectAndSearchView(Context context) {
        this(context, null);
    }

    public SelectAndSearchView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.makeLayout();
    }

    public void setChooseAssetListener(ChooseAssetListener chooseAssetListener) {
        this.chooseAssetListener = chooseAssetListener;
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
        setFiatOptions(FiatManager.getAllFiats());
        setFiatManagerOptions(FiatManager.fiatManagers);
        setCoinOptions(CoinManager.getAllCoins());
        setCoinManagerOptions(CoinManager.coinManagers);
        setTokenOptions(TokenManager.getAllTokens());
        setTokenManagerOptions(TokenManager.tokenManagers);
    }

    public void setFiatOptions(ArrayList<Fiat> fiatArrayList) {
        // Reset the maps for Fiats.
        search_options_fiats.clear();
        search_options_fiat_names.clear();
        search_options_fiat_display_names.clear();
        options_fiats_sorted.clear();
        options_fiat_setting_names_sorted.clear();

        // For each fiat, separate it by type.
        // The user can only see one type of fiat at a time.
        for(Fiat fiat : fiatArrayList) {
            String fiatType = fiat.getAssetType();

            ArrayList<Fiat> searchFiats = HashMapUtil.getValueFromMap(search_options_fiats, fiatType);
            if(searchFiats == null) {
                searchFiats = new ArrayList<>();
            }
            searchFiats.add(fiat);
            HashMapUtil.putValueInMap(search_options_fiats, fiatType, searchFiats);

            ArrayList<String> searchNames = HashMapUtil.getValueFromMap(search_options_fiat_names, fiatType);
            if(searchNames == null) {
                searchNames = new ArrayList<>();
            }
            searchNames.add(fiat.getName());
            HashMapUtil.putValueInMap(search_options_fiat_names, fiatType, searchNames);

            ArrayList<String> searchDisplayNames = HashMapUtil.getValueFromMap(search_options_fiat_display_names, fiatType);
            if(searchDisplayNames == null) {
                searchDisplayNames = new ArrayList<>();
            }
            searchDisplayNames.add(fiat.getDisplayName());
            HashMapUtil.putValueInMap(search_options_fiat_display_names, fiatType, searchDisplayNames);

            ArrayList<Fiat> fiatsSorted = HashMapUtil.getValueFromMap(options_fiats_sorted, fiatType);
            if(fiatsSorted == null) {
                fiatsSorted = new ArrayList<>();
            }
            fiatsSorted.add(fiat);
            HashMapUtil.putValueInMap(options_fiats_sorted, fiatType, fiatsSorted);

            ArrayList<String> fiatSettingNamesSorted = HashMapUtil.getValueFromMap(options_fiat_setting_names_sorted, fiatType);
            if(fiatSettingNamesSorted == null) {
                fiatSettingNamesSorted = new ArrayList<>();
            }
            fiatSettingNamesSorted.add(fiat.getSettingName());
            HashMapUtil.putValueInMap(options_fiat_setting_names_sorted, fiatType, fiatSettingNamesSorted);
        }

        for(String fiatType : new ArrayList<>(options_fiats_sorted.keySet())) {
            ArrayList<Fiat> fiatsSorted = HashMapUtil.getValueFromMap(options_fiats_sorted, fiatType);
            if(fiatsSorted != null) {
                Collections.sort(fiatsSorted, getSettingComparatorAsset());
            }
            HashMapUtil.putValueInMap(options_fiats_sorted, fiatType, fiatsSorted);
        }

        for(String fiatType : new ArrayList<>(options_fiat_setting_names_sorted.keySet())) {
            ArrayList<String> fiatSettingNamesSorted = HashMapUtil.getValueFromMap(options_fiat_setting_names_sorted, fiatType);
            if(fiatSettingNamesSorted != null) {
                Collections.sort(fiatSettingNamesSorted, getComparatorString());
            }
            HashMapUtil.putValueInMap(options_fiat_setting_names_sorted, fiatType, fiatSettingNamesSorted);
        }
    }

    public void setCoinOptions(ArrayList<Coin> coinArrayList) {
        // Reset the maps for Coins.
        search_options_coins.clear();
        search_options_coin_names.clear();
        search_options_coin_display_names.clear();
        options_coins_sorted.clear();
        options_coin_setting_names_sorted.clear();

        // For each coin, separate it by type.
        // The user can only see one type of coin at a time.
        for(Coin coin : coinArrayList) {
            String coinType = coin.getAssetType();

            ArrayList<Coin> searchCoins = HashMapUtil.getValueFromMap(search_options_coins, coinType);
            if(searchCoins == null) {
                searchCoins = new ArrayList<>();
            }
            searchCoins.add(coin);
            HashMapUtil.putValueInMap(search_options_coins, coinType, searchCoins);

            ArrayList<String> searchNames = HashMapUtil.getValueFromMap(search_options_coin_names, coinType);
            if(searchNames == null) {
                searchNames = new ArrayList<>();
            }
            searchNames.add(coin.getName());
            HashMapUtil.putValueInMap(search_options_coin_names, coinType, searchNames);

            ArrayList<String> searchDisplayNames = HashMapUtil.getValueFromMap(search_options_coin_display_names, coinType);
            if(searchDisplayNames == null) {
                searchDisplayNames = new ArrayList<>();
            }
            searchDisplayNames.add(coin.getDisplayName());
            HashMapUtil.putValueInMap(search_options_coin_display_names, coinType, searchDisplayNames);

            ArrayList<Coin> coinsSorted = HashMapUtil.getValueFromMap(options_coins_sorted, coinType);
            if(coinsSorted == null) {
                coinsSorted = new ArrayList<>();
            }
            coinsSorted.add(coin);
            HashMapUtil.putValueInMap(options_coins_sorted, coinType, coinsSorted);

            ArrayList<String> coinSettingNamesSorted = HashMapUtil.getValueFromMap(options_coin_setting_names_sorted, coinType);
            if(coinSettingNamesSorted == null) {
                coinSettingNamesSorted = new ArrayList<>();
            }
            coinSettingNamesSorted.add(coin.getSettingName());
            HashMapUtil.putValueInMap(options_coin_setting_names_sorted, coinType, coinSettingNamesSorted);
        }

        for(String coinType : new ArrayList<>(options_coins_sorted.keySet())) {
            ArrayList<Coin> coinsSorted = HashMapUtil.getValueFromMap(options_coins_sorted, coinType);
            if(coinsSorted != null) {
                Collections.sort(coinsSorted, getSettingComparatorAsset());
            }
            HashMapUtil.putValueInMap(options_coins_sorted, coinType, coinsSorted);
        }

        for(String coinType : new ArrayList<>(options_coin_setting_names_sorted.keySet())) {
            ArrayList<String> coinSettingNamesSorted = HashMapUtil.getValueFromMap(options_coin_setting_names_sorted, coinType);
            if(coinSettingNamesSorted != null) {
                Collections.sort(coinSettingNamesSorted, getComparatorString());
            }
            HashMapUtil.putValueInMap(options_coin_setting_names_sorted, coinType, coinSettingNamesSorted);
        }
    }

    public void setTokenOptions(ArrayList<Token> tokenArrayList) {
        // Reset the maps for Tokens.
        search_options_tokens.clear();
        search_options_token_names.clear();
        search_options_token_display_names.clear();
        options_tokens_sorted.clear();
        options_token_setting_names_sorted.clear();

        // For each token, separate it by type.
        // The user can only see one type of token at a time.
        for(Token token : tokenArrayList) {
            String tokenType = token.getAssetType();

            ArrayList<Token> searchTokens = HashMapUtil.getValueFromMap(search_options_tokens, tokenType);
            if(searchTokens == null) {
                searchTokens = new ArrayList<>();
            }
            searchTokens.add(token);
            HashMapUtil.putValueInMap(search_options_tokens, tokenType, searchTokens);

            ArrayList<String> searchNames = HashMapUtil.getValueFromMap(search_options_token_names, tokenType);
            if(searchNames == null) {
                searchNames = new ArrayList<>();
            }
            searchNames.add(token.getName());
            HashMapUtil.putValueInMap(search_options_token_names, tokenType, searchNames);

            ArrayList<String> searchDisplayNames = HashMapUtil.getValueFromMap(search_options_token_display_names, tokenType);
            if(searchDisplayNames == null) {
                searchDisplayNames = new ArrayList<>();
            }
            searchDisplayNames.add(token.getDisplayName());
            HashMapUtil.putValueInMap(search_options_token_display_names, tokenType, searchDisplayNames);

            ArrayList<Token> tokensSorted = HashMapUtil.getValueFromMap(options_tokens_sorted, tokenType);
            if(tokensSorted == null) {
                tokensSorted = new ArrayList<>();
            }
            tokensSorted.add(token);
            HashMapUtil.putValueInMap(options_tokens_sorted, tokenType, tokensSorted);

            ArrayList<String> tokenSettingNamesSorted = HashMapUtil.getValueFromMap(options_token_setting_names_sorted, tokenType);
            if(tokenSettingNamesSorted == null) {
                tokenSettingNamesSorted = new ArrayList<>();
            }
            tokenSettingNamesSorted.add(token.getSettingName());
            HashMapUtil.putValueInMap(options_token_setting_names_sorted, tokenType, tokenSettingNamesSorted);
        }

        for(String tokenType : new ArrayList<>(options_tokens_sorted.keySet())) {
            ArrayList<Token> tokensSorted = HashMapUtil.getValueFromMap(options_tokens_sorted, tokenType);
            if(tokensSorted != null) {
                Collections.sort(tokensSorted, getSettingComparatorAsset());
            }
            HashMapUtil.putValueInMap(options_tokens_sorted, tokenType, tokensSorted);
        }

        for(String tokenType : new ArrayList<>(options_token_setting_names_sorted.keySet())) {
            ArrayList<String> tokenSettingNamesSorted = HashMapUtil.getValueFromMap(options_token_setting_names_sorted, tokenType);
            if(tokenSettingNamesSorted != null) {
                Collections.sort(tokenSettingNamesSorted, getComparatorString());
            }
            HashMapUtil.putValueInMap(options_token_setting_names_sorted, tokenType, tokenSettingNamesSorted);
        }
    }

    public void setFiatManagerOptions(ArrayList<FiatManager> fiatManagerArrayList) {
        search_options_fiat_types.clear();
        options_fiat_types.clear();

        for(FiatManager fiatManager : fiatManagerArrayList) {
            search_options_fiat_types.add(fiatManager.getFiatType());
            options_fiat_types.add(fiatManager.getFiatType());
        }

        Collections.sort(options_fiat_types, getComparatorString());
    }

    public void setCoinManagerOptions(ArrayList<CoinManager> coinManagerArrayList) {
        search_options_coin_types.clear();
        options_coin_types.clear();

        for(CoinManager coinManager : coinManagerArrayList) {
            search_options_coin_types.add(coinManager.getCoinType());
            options_coin_types.add(coinManager.getCoinType());
        }

        Collections.sort(options_coin_types, getComparatorString());
    }

    public void setTokenManagerOptions(ArrayList<TokenManager> tokenManagerArrayList) {
        search_options_token_types.clear();
        options_token_types.clear();

        for(TokenManager tokenManager : tokenManagerArrayList) {
            search_options_token_types.add(tokenManager.getTokenType());
            options_token_types.add(tokenManager.getTokenType());
        }

        Collections.sort(options_token_types, getComparatorString());
    }

    public void chooseFiat(String fiatType) {
        lastButtonKind = "!FIAT!";
        lastButtonType = fiatType;

        // Remake layout to refresh button visibility and search options.
        this.removeAllViews();
        makeLayout();

        if(fiatType != null) {
            ArrayList<String> settingNames = HashMapUtil.getValueFromMap(options_fiat_setting_names_sorted, fiatType);
            if(settingNames != null) {
                bsv.setOptions(settingNames);

                // Choose the default, but if that is not an option then choose the first.
                ArrayList<Fiat> fiatsSorted = HashMapUtil.getValueFromMap(options_fiats_sorted, fiatType);

                int idx = 0;
                if(fiatsSorted != null) {
                    idx = fiatsSorted.indexOf(DefaultFiatSetting.value);
                    if(idx == -1) {
                        idx = 0;
                    }
                }

                bsv.setSelection(idx);
            }
        }


        B_FIAT.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_radio_button_checked_small_24, 0, 0, 0);
        B_COIN.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_radio_button_unchecked_small_24, 0, 0, 0);
        B_TOKEN.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_radio_button_unchecked_small_24, 0, 0, 0);
    }

    public void chooseCoin(String coinType) {
        lastButtonKind = "!COIN!";
        lastButtonType = coinType;

        // Remake layout to refresh button visibility and search options.
        this.removeAllViews();
        makeLayout();

        if(coinType != null) {
            ArrayList<String> settingNames = HashMapUtil.getValueFromMap(options_coin_setting_names_sorted, coinType);
            if(settingNames != null) {
                bsv.setOptions(settingNames);

                // Choose the default, but if that is not an option then choose the first.
                ArrayList<Coin> coinsSorted = HashMapUtil.getValueFromMap(options_coins_sorted, coinType);

                int idx = 0;
                if(coinsSorted != null) {
                    idx = coinsSorted.indexOf(DefaultCoinSetting.value);
                    if(idx == -1) {
                        idx = 0;
                    }
                }

                bsv.setSelection(idx);
            }
        }

        B_FIAT.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_radio_button_unchecked_small_24, 0, 0, 0);
        B_COIN.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_radio_button_checked_small_24, 0, 0, 0);
        B_TOKEN.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_radio_button_unchecked_small_24, 0, 0, 0);
    }

    public void chooseToken(String tokenType) {
        // Initialize the sorted lists for this kind of token.
        lastButtonKind = "!TOKEN!";
        lastButtonType = tokenType;

        // Remake layout to refresh button visibility and search options.
        this.removeAllViews();
        makeLayout();

        if(tokenType != null) {
            ArrayList<String> settingNames = HashMapUtil.getValueFromMap(options_token_setting_names_sorted, tokenType);
            if(settingNames != null) {
                // There is no "DefaultTokenSetting" - the first token will always be chosen.
                bsv.setOptions(settingNames);
            }
        }

        B_FIAT.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_radio_button_unchecked_small_24, 0, 0, 0);
        B_COIN.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_radio_button_unchecked_small_24, 0, 0, 0);
        B_TOKEN.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_radio_button_checked_small_24, 0, 0, 0);
    }

    public void chooseSearch(Asset asset) {
        // When we choose "search", we immediately choose one of the other kinds.
        // This should not be called with a null input.
        ArrayList<? extends Asset> assetSorted = null;
        String assetKind = asset.getAssetKind();
        String assetType = asset.getAssetType();

        if("!FIAT!".equals(assetKind)) {
            chooseFiat(asset.getAssetType());
            assetSorted = HashMapUtil.getValueFromMap(options_fiats_sorted, assetType);
        }
        else if("!COIN!".equals(assetKind)) {
            chooseCoin(asset.getAssetType());
            assetSorted = HashMapUtil.getValueFromMap(options_coins_sorted, assetType);
        }
        else if("!TOKEN!".equals(assetKind)) {
            chooseToken(asset.getAssetType());
            assetSorted = HashMapUtil.getValueFromMap(options_tokens_sorted, assetType);
        }

        int idx = 0;
        if(assetSorted != null) {
            // All possible search selections exist in the ArrayList.
            idx = assetSorted.indexOf(asset);
        }
        bsv.setSelection(idx);
    }

    public void removeAsset(Asset asset) {
        String assetKind = asset.getAssetKind();
        String assetType = asset.getAssetType();
        int idx = 0;

        if("!FIAT!".equals(assetKind)) {
            ArrayList<Fiat> searchFiats = HashMapUtil.getValueFromMap(search_options_fiats, assetType);
            if(searchFiats != null) {
                searchFiats.remove((Fiat)asset);
            }
            HashMapUtil.putValueInMap(search_options_fiats, assetType, searchFiats);

            ArrayList<String> searchFiatNames = HashMapUtil.getValueFromMap(search_options_fiat_names, assetType);
            if(searchFiatNames != null) {
                searchFiatNames.remove(asset.getName());
            }
            HashMapUtil.putValueInMap(search_options_fiat_names, assetType, searchFiatNames);

            ArrayList<String> searchFiatDisplayNames = HashMapUtil.getValueFromMap(search_options_fiat_display_names, assetType);
            if(searchFiatDisplayNames != null) {
                searchFiatDisplayNames.remove(asset.getDisplayName());
            }
            HashMapUtil.putValueInMap(search_options_fiat_display_names, assetType, searchFiatDisplayNames);

            ArrayList<Fiat> fiatsSorted = HashMapUtil.getValueFromMap(options_fiats_sorted, assetType);
            if(fiatsSorted != null) {
                idx = fiatsSorted.indexOf((Fiat)asset) - 1;
                fiatsSorted.remove((Fiat)asset);
            }
            HashMapUtil.putValueInMap(options_fiats_sorted, assetType, fiatsSorted);

            ArrayList<String> fiatSettingNamesSorted = HashMapUtil.getValueFromMap(options_fiat_setting_names_sorted, assetType);
            if(fiatSettingNamesSorted != null) {
                fiatSettingNamesSorted.remove(asset.getSettingName());
            }
            HashMapUtil.putValueInMap(options_fiat_setting_names_sorted, assetType, fiatSettingNamesSorted);

            chooseFiat(lastButtonType);
        }
        else if("!COIN!".equals(assetKind)) {
            ArrayList<Coin> searchCoins = HashMapUtil.getValueFromMap(search_options_coins, assetType);
            if(searchCoins != null) {
                searchCoins.remove((Coin)asset);
            }
            HashMapUtil.putValueInMap(search_options_coins, assetType, searchCoins);

            ArrayList<String> searchCoinNames = HashMapUtil.getValueFromMap(search_options_coin_names, assetType);
            if(searchCoinNames != null) {
                searchCoinNames.remove(asset.getName());
            }
            HashMapUtil.putValueInMap(search_options_coin_names, assetType, searchCoinNames);

            ArrayList<String> searchCoinDisplayNames = HashMapUtil.getValueFromMap(search_options_coin_display_names, assetType);
            if(searchCoinDisplayNames != null) {
                searchCoinDisplayNames.remove(asset.getDisplayName());
            }
            HashMapUtil.putValueInMap(search_options_coin_display_names, assetType, searchCoinDisplayNames);

            ArrayList<Coin> coinsSorted = HashMapUtil.getValueFromMap(options_coins_sorted, assetType);
            if(coinsSorted != null) {
                idx = coinsSorted.indexOf((Coin)asset) - 1;
                coinsSorted.remove((Coin)asset);
            }
            HashMapUtil.putValueInMap(options_coins_sorted, assetType, coinsSorted);

            ArrayList<String> coinSettingNamesSorted = HashMapUtil.getValueFromMap(options_coin_setting_names_sorted, assetType);
            if(coinSettingNamesSorted != null) {
                coinSettingNamesSorted.remove(asset.getSettingName());
            }
            HashMapUtil.putValueInMap(options_coin_setting_names_sorted, assetType, coinSettingNamesSorted);

            chooseCoin(lastButtonType);
        }
        else if("!TOKEN!".equals(assetKind)) {
            ArrayList<Token> searchTokens = HashMapUtil.getValueFromMap(search_options_tokens, assetType);
            if(searchTokens != null) {
                searchTokens.remove((Token)asset);
            }
            HashMapUtil.putValueInMap(search_options_tokens, assetType, searchTokens);

            ArrayList<String> searchTokenNames = HashMapUtil.getValueFromMap(search_options_token_names, assetType);
            if(searchTokenNames != null) {
                searchTokenNames.remove(asset.getName());
            }
            HashMapUtil.putValueInMap(search_options_token_names, assetType, searchTokenNames);

            ArrayList<String> searchTokenDisplayNames = HashMapUtil.getValueFromMap(search_options_token_display_names, assetType);
            if(searchTokenDisplayNames != null) {
                searchTokenDisplayNames.remove(asset.getDisplayName());
            }
            HashMapUtil.putValueInMap(search_options_token_display_names, assetType, searchTokenDisplayNames);

            ArrayList<Token> tokensSorted = HashMapUtil.getValueFromMap(options_tokens_sorted, assetType);
            if(tokensSorted != null) {
                idx = tokensSorted.indexOf((Token)asset) - 1;
                tokensSorted.remove((Token)asset);
            }
            HashMapUtil.putValueInMap(options_tokens_sorted, assetType, tokensSorted);

            ArrayList<String> tokenSettingNamesSorted = HashMapUtil.getValueFromMap(options_token_setting_names_sorted, assetType);
            if(tokenSettingNamesSorted != null) {
                tokenSettingNamesSorted.remove(asset.getSettingName());
            }
            HashMapUtil.putValueInMap(options_token_setting_names_sorted, assetType, tokenSettingNamesSorted);

            chooseToken(lastButtonType);
        }

        // Whatever the index was, after this we want it to be one less, or zero if we can't determine a value.
        if(idx < 0) {
            idx = 0;
        }
        bsv.setSelection(idx);
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
                if(options_fiat_types.isEmpty()) {
                    // There are no fiat types to choose from.
                    chooseFiat(null);
                }
                else if(options_fiat_types.size() == 1) {
                    // There is only one fiat type, so just choose it.
                    chooseFiat(options_fiat_types.get(0));
                }
                else {
                    // Present a list of available fiat types to choose from.
                    PopupMenu popup = new PopupMenu(context, B_FIAT);

                    ArrayList<String> options = new ArrayList<>(options_coin_types);
                    Collections.sort(options, getComparatorString());
                    for(String o : options) {
                        popup.getMenu().add(o);
                    }

                    popup.setOnMenuItemClickListener(new CrashPopupMenu.CrashOnMenuItemClickListener(context) {
                        public boolean onMenuItemClickImpl(MenuItem item) {
                            chooseFiat(item.toString());
                            return true;
                        }
                    });
                    popup.show();
                }
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
                if(options_coin_types.isEmpty()) {
                    // There are no coin types to choose from.
                    chooseCoin(null);
                }
                else if(options_coin_types.size() == 1) {
                    // There is only one coin type, so just choose it.
                    chooseCoin(options_coin_types.get(0));
                }
                else {
                    // Present a list of available coin types to choose from.
                    PopupMenu popup = new PopupMenu(context, B_COIN);

                    ArrayList<String> options = new ArrayList<>(options_coin_types);
                    Collections.sort(options, getComparatorString());
                    for(String o : options) {
                        popup.getMenu().add(o);
                    }

                    popup.setOnMenuItemClickListener(new CrashPopupMenu.CrashOnMenuItemClickListener(context) {
                        public boolean onMenuItemClickImpl(MenuItem item) {
                            chooseCoin(item.toString());
                            return true;
                        }
                    });
                    popup.show();
                }
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

        if(chooseAssetListener != null) {
            chooseAssetListener.onAssetChosen(null);
        }

        bsv = new BorderedSpinnerView(context);
        bsv.setOnItemSelectedListener(new CrashAdapterView.CrashOnItemSelectedListener(this.activity) {
            public void onNothingSelectedImpl(AdapterView<?> parent) {}
            public void onItemSelectedImpl(AdapterView<?> parent, View view, int pos, long id) {
                if(chooseAssetListener != null) {
                    chooseAssetListener.onAssetChosen(getChosenAsset());
                }
            }
        });

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

        B_SEARCH = new AppCompatImageButton(context) {
            @Override
            protected void onMeasure(int width, int height) {
                // Change Width to match Height so we get a square.
                super.onMeasure(width, height);
                setMeasuredDimension(height, height);
            }
        };
        B_SEARCH.setImageResource(R.drawable.ic_baseline_search_24);
        B_SEARCH.setPadding(0, 0, 0, 0);
        B_SEARCH.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        B_SEARCH.setOnClickListener(new CrashView.CrashOnClickListener(context) {
            public void onClickImpl(View v) {
                ArrayList<Asset> searchAssets = getSearchOptionsAssets();
                if(searchAssets.isEmpty()) {
                    // We don't want to call "chooseSearch" with null, so just show a Toast.
                    ToastUtil.showToast(context, "no_search_assets");
                }
                else {
                    StateObj.search_options_assets = searchAssets;
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

        boolean isEmpty = true;
        String text = "";
        if("!FIAT!".equals(lastButtonKind)) {
            if(lastButtonType == null) {
                text = "No Fiats Available";
                isEmpty = true;
            }
            else {
                text = "No " + lastButtonType + " Fiats Available";
                ArrayList<Fiat> fiatsSorted = HashMapUtil.getValueFromMap(options_fiats_sorted, lastButtonType);
                isEmpty = fiatsSorted == null || fiatsSorted.isEmpty();
            }
        }
        else if("!COIN!".equals(lastButtonKind)) {
            if(lastButtonType == null) {
                text = "No Coins Available";
                isEmpty = true;
            }
            else {
                text = "No " + lastButtonType + " Coins Available";
                ArrayList<Coin> coinsSorted = HashMapUtil.getValueFromMap(options_coins_sorted, lastButtonType);
                isEmpty = coinsSorted == null || coinsSorted.isEmpty();
            }
        }
        else if("!TOKEN!".equals(lastButtonKind)) {
            if(lastButtonType == null) {
                text = "No Tokens Available";
                isEmpty = true;
            }
            else {
                text = "No " + lastButtonType + " Tokens Available";
                ArrayList<Token> tokensSorted = HashMapUtil.getValueFromMap(options_tokens_sorted, lastButtonType);
                isEmpty = tokensSorted == null || tokensSorted.isEmpty();
            }
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
        Asset asset = null;
        int idx = bsv.spinner.getSelectedItemPosition();

        if(idx != -1) {
            if("!FIAT!".equals(lastButtonKind)) {
                if(lastButtonType != null) {
                    ArrayList<Fiat> fiatsSorted = HashMapUtil.getValueFromMap(options_fiats_sorted, lastButtonType);
                    if(fiatsSorted != null && !fiatsSorted.isEmpty()) {
                        asset = fiatsSorted.get(idx);
                    }
                }
            }
            else if("!COIN!".equals(lastButtonKind)) {
                if(lastButtonType != null) {
                    ArrayList<Coin> coinsSorted = HashMapUtil.getValueFromMap(options_coins_sorted, lastButtonType);
                    if(coinsSorted != null && !coinsSorted.isEmpty()) {
                        asset = coinsSorted.get(idx);
                    }
                }
            }
            else if("!TOKEN!".equals(lastButtonKind)) {
                if(lastButtonType != null) {
                    ArrayList<Token> tokensSorted = HashMapUtil.getValueFromMap(options_tokens_sorted, lastButtonType);
                    if(tokensSorted != null && !tokensSorted.isEmpty()) {
                        asset = tokensSorted.get(idx);
                    }
                }
            }
        }

        return asset;
    }

    public void restoreOptions(String lastButtonKind, String lastButtonType) {
        if("!FIAT!".equals(lastButtonKind)) {
            chooseFiat(lastButtonType);
        }
        else if("!COIN!".equals(lastButtonKind)) {
            chooseCoin(lastButtonType);
        }
        else if("!TOKEN!".equals(lastButtonKind)) {
            chooseToken(lastButtonType);
        }
    }

    private ArrayList<String> getSearchOptionsNames() {
        ArrayList<String> options = new ArrayList<>();
        if(includesFiat) {
            for(String fiatType : search_options_fiat_types) {
                ArrayList<String> searchNames = HashMapUtil.getValueFromMap(search_options_fiat_names, fiatType);
                if(searchNames != null) {
                    options.addAll(searchNames);
                }
            }
        }
        if(includesCoin) {
            for(String coinType : search_options_coin_types) {
                ArrayList<String> searchNames = HashMapUtil.getValueFromMap(search_options_coin_names, coinType);
                if(searchNames != null) {
                    options.addAll(searchNames);
                }
            }
        }
        if(includesToken) {
            for(String tokenType : search_options_token_types) {
                ArrayList<String> searchNames = HashMapUtil.getValueFromMap(search_options_token_names, tokenType);
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
            for(String fiatType : search_options_fiat_types) {
                ArrayList<String> searchDisplayNames = HashMapUtil.getValueFromMap(search_options_fiat_display_names, fiatType);
                if(searchDisplayNames != null) {
                    options.addAll(searchDisplayNames);
                }
            }
        }
        if(includesCoin) {
            for(String coinType : search_options_coin_types) {
                ArrayList<String> searchDisplayNames = HashMapUtil.getValueFromMap(search_options_coin_display_names, coinType);
                if(searchDisplayNames != null) {
                    options.addAll(searchDisplayNames);
                }
            }
        }
        if(includesToken) {
            for(String tokenType : search_options_token_types) {
                ArrayList<String> searchDisplayNames = HashMapUtil.getValueFromMap(search_options_token_display_names, tokenType);
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
            for(String fiatType : search_options_fiat_types) {
                ArrayList<Fiat> searchFiats = HashMapUtil.getValueFromMap(search_options_fiats, fiatType);
                if(searchFiats != null) {
                    assets.addAll(searchFiats);
                }
            }
        }
        if(includesCoin) {
            for(String coinType : search_options_coin_types) {
                ArrayList<Coin> searchCoins = HashMapUtil.getValueFromMap(search_options_coins, coinType);
                if(searchCoins != null) {
                    assets.addAll(searchCoins);
                }
            }
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
        String lastButtonKindA = ssvA.lastButtonKind;
        String lastButtonKindB = ssvB.lastButtonKind;
        String lastButtonTypeA = ssvA.lastButtonType;
        String lastButtonTypeB = ssvB.lastButtonType;
        int idxA = ssvA.bsv.spinner.getSelectedItemPosition();
        int idxB = ssvB.bsv.spinner.getSelectedItemPosition();

        ssvA.restoreOptions(lastButtonKindB, lastButtonTypeB);
        ssvB.restoreOptions(lastButtonKindA, lastButtonTypeA);

        ssvA.bsv.setSelection(idxB);
        ssvB.bsv.setSelection(idxA);
    }

    @Override
    public Parcelable onSaveInstanceStateImpl(Parcelable state)
    {
        Bundle bundle = new Bundle();
        bundle.putParcelable("superState", state);
        bundle.putInt("selection", this.bsv.spinner.getSelectedItemPosition());
        bundle.putString("lastButtonKind", lastButtonKind);
        bundle.putString("lastButtonType", lastButtonType);

        return bundle;
    }

    @Override
    public Parcelable onRestoreInstanceStateImpl(Parcelable state)
    {
        if (state instanceof Bundle) // implicit null check
        {
            Bundle bundle = (Bundle) state;
            state = bundle.getParcelable("superState");
            lastButtonKind = bundle.getString("lastButtonKind");
            lastButtonType = bundle.getString("lastButtonType");

            restoreOptions(lastButtonKind, lastButtonType);
            this.bsv.setSelection(bundle.getInt("selection"));
        }
        return state;
    }

    abstract public static class ChooseAssetListener {
        abstract public void onAssetChosen(Asset asset);
    }
}
