package com.musicslayer.cryptobuddy.activity;

import android.content.Intent;
import android.text.TextUtils;
import android.text.util.Linkify;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;

import com.musicslayer.cryptobuddy.BuildConfig;
import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.api.address.AddressAPI;
import com.musicslayer.cryptobuddy.api.exchange.ExchangeAPI;
import com.musicslayer.cryptobuddy.api.price.PriceAPI;
import com.musicslayer.cryptobuddy.asset.crypto.coin.Coin;
import com.musicslayer.cryptobuddy.asset.exchange.Exchange;
import com.musicslayer.cryptobuddy.asset.fiat.Fiat;
import com.musicslayer.cryptobuddy.asset.network.Network;
import com.musicslayer.cryptobuddy.asset.tokenmanager.TokenManager;
import com.musicslayer.cryptobuddy.util.FileUtil;

import java.util.ArrayList;
import java.util.Collections;

public class AboutActivity extends BaseActivity {
    public int getAdLayoutViewID() {
        return -1;
    }

    @Override
    public void onBackPressedImpl() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    public void createLayout () {
        setContentView(R.layout.activity_about);

        Toolbar toolbar = findViewById(R.id.about_toolbar);
        setSupportActionBar(toolbar);

        TextView T_CONTACT_TEXT = findViewById(R.id.about_contactText);
        T_CONTACT_TEXT.setText("Email: musicslayer@gmail.com\n");

        TextView T_VERSION_TEXT = findViewById(R.id.about_versionText);
        T_VERSION_TEXT.setText(BuildConfig.VERSION_NAME + "\n");

        // List all policies
        TextView T_PRIVACYPOLICY_TEXT = findViewById(R.id.about_privacyPolicyText);
        T_PRIVACYPOLICY_TEXT.setAutoLinkMask(Linkify.WEB_URLS);
        T_PRIVACYPOLICY_TEXT.setText("https://sites.google.com/view/crypto-buddy-privacy-policy/home" + "\n");

        String disclaimerText = FileUtil.readFile(this, R.raw.policy_disclaimer);
        TextView T_DISCLAIMER_TEXT = findViewById(R.id.about_disclaimerText);
        T_DISCLAIMER_TEXT.setText(disclaimerText);

        // List of all exchanges
        ArrayList<String> sortedExchangeNames = Exchange.exchange_display_names;
        Collections.sort(sortedExchangeNames);

        String exchangesText = "  " + TextUtils.join("\n  ", sortedExchangeNames);

        TextView T_EXCHANGES_TEXT = findViewById(R.id.about_exchangesText);
        T_EXCHANGES_TEXT.setText(exchangesText + "\n");

        // List of all assets
        ArrayList<String> sortedFiatDisplayNames = Fiat.fiat_display_names;
        Collections.sort(sortedFiatDisplayNames);

        ArrayList<String> sortedCoinDisplayNames = Coin.coin_display_names;
        Collections.sort(sortedCoinDisplayNames);

        ArrayList<String> sortedTokenManagerDisplayNames = TokenManager.tokenManagers_token_types;
        Collections.sort(sortedTokenManagerDisplayNames);

        String assetText = "FIAT:\n  " + TextUtils.join("\n  ", sortedFiatDisplayNames) +
            "\n\nCOINS:\n  " + TextUtils.join("\n  ", sortedCoinDisplayNames) +
            "\n\nTOKEN TYPES:\n  " + TextUtils.join("\n  ", sortedTokenManagerDisplayNames);

        TextView T_ASSETS_TEXT = findViewById(R.id.about_assetsText);
        T_ASSETS_TEXT.setText(assetText + "\n");

        // List of all apis
        ArrayList<String> sortedAddressAPIDisplayNames = AddressAPI.address_api_display_names;
        Collections.sort(sortedAddressAPIDisplayNames);

        ArrayList<String> sortedPriceAPIDisplayNames = PriceAPI.price_api_display_names;
        Collections.sort(sortedPriceAPIDisplayNames);

        ArrayList<String> sortedExchangeAPIDisplayNames = ExchangeAPI.exchange_api_display_names;
        Collections.sort(sortedExchangeAPIDisplayNames);

        String apiText = "ADDRESS:\n  " + TextUtils.join("\n  ", sortedAddressAPIDisplayNames) +
            "\n\nPRICE:\n  " + TextUtils.join("\n  ", sortedPriceAPIDisplayNames) +
            "\n\nEXCHANGE:\n  " + TextUtils.join("\n  ", sortedExchangeAPIDisplayNames);

        TextView T_SOURCES_TEXT = findViewById(R.id.about_sourcesText);
        T_SOURCES_TEXT.setText(apiText + "\n");

        // List of all networks
        ArrayList<String> sortedNetworkDisplayNames = Network.network_display_names;
        Collections.sort(sortedNetworkDisplayNames);

        String networkText = "  " + TextUtils.join("\n  ", sortedNetworkDisplayNames);

        TextView T_NETWORKS_TEXT = findViewById(R.id.about_networksText);
        T_NETWORKS_TEXT.setText(networkText + "\n");
    }
}