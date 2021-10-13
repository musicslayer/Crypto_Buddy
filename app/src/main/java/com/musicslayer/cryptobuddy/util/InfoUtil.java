package com.musicslayer.cryptobuddy.util;

import android.content.Context;

import com.musicslayer.cryptobuddy.api.address.AddressData;
import com.musicslayer.cryptobuddy.dialog.BaseDialogFragment;
import com.musicslayer.cryptobuddy.dialog.InfoDialog;

import java.util.ArrayList;

// Class to store crypto-specific info we want to show the user.
public class InfoUtil {
    public static boolean hasInfo(ArrayList<AddressData> addressDataArray) {
        // Return true if there is any info to show for any crypto.
        for(AddressData addressData : addressDataArray) {
            if(InfoUtil.getInfo(addressData) != null) {
                return true;
            }
        }

        return false;
    }

    public static void showInfo(Context context, ArrayList<AddressData> addressDataArray) {
        StringBuilder infoText = new StringBuilder();
        ArrayList<String> seenNames = new ArrayList<>();

        for(AddressData addressData : addressDataArray) {
            if(seenNames.contains(addressData.cryptoAddress.getCrypto().getName())) { continue; }

            String info = InfoUtil.getInfo(addressData);
            if(info != null) {
                infoText.append(InfoUtil.getInfo(addressData));
            }

            seenNames.add(addressData.cryptoAddress.getCrypto().getName());
        }

        BaseDialogFragment infoDialogFragment = BaseDialogFragment.newInstance(InfoDialog.class, infoText.toString());
        infoDialogFragment.show(context, "crypto_info");
    }

    public static String getInfo(AddressData addressData) {
        String cryptoName = addressData.cryptoAddress.getCrypto().getName();
        boolean isMainnet = addressData.cryptoAddress.network.isMainnet();
        String cryptoDisplayName = addressData.cryptoAddress.getCrypto().getDisplayName();

        String info;
        switch(cryptoName) {
            case "ATOM":
                info = "Some token swap operations will not show up as transactions.";
                break;
            case "BNBc":
                if(isMainnet) {
                    return null;
                }
                else {
                    info = "Transactions for testnet addresses are not available.";
                }
                break;
            case "BNBs":
            case "MATIC":
                info = "Transactions may not include smart contract operations, such as reflections and taxes.";
                break;
            case "BTC":
            case "DASH":
            case "ZEC":
            case "DOGE":
            case "LTC":
                info = "Fees are included in the transaction amount. Fees for unsuccessful transactions are not shown.";
                break;
            case "ETH":
                if(isMainnet) {
                    info = "Transactions may not include smart contract operations, such as reflections and taxes.";
                }
                else {
                    info = "Transactions may not include smart contract operations, such as reflections and taxes. Also, token balances for testnet addresses are not available.";
                }
                break;
            case "VET":
                info = "VTHO balance includes generated rewards from holding VET, but these rewards do not show up as transactions.";
                break;
            case "XRP":
                info = "Negative balances of an asset represent obligations in the XRP Ledger.";
                break;
            default:
                return null;
        }

        return cryptoDisplayName + " (" + cryptoName + ")" + ": " + info + "\n\n";
    }
}
