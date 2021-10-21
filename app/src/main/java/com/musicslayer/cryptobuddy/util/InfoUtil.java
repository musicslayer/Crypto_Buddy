package com.musicslayer.cryptobuddy.util;

import android.content.Context;

import com.musicslayer.cryptobuddy.api.address.CryptoAddress;
import com.musicslayer.cryptobuddy.dialog.BaseDialogFragment;
import com.musicslayer.cryptobuddy.dialog.InfoDialog;

import java.util.ArrayList;

// Class to store crypto-specific info we want to show the user.
public class InfoUtil {
    public static boolean hasInfo(ArrayList<CryptoAddress> cryptoAddressArrayList) {
        // Return true if there is any info to show for any crypto.
        for(CryptoAddress cryptoAddress : cryptoAddressArrayList) {
            if(InfoUtil.getInfo(cryptoAddress) != null) {
                return true;
            }
        }

        return false;
    }

    public static void showInfo(Context context, ArrayList<CryptoAddress> cryptoAddressArrayList) {
        StringBuilder infoText = new StringBuilder();
        ArrayList<String> seenNames = new ArrayList<>();

        for(CryptoAddress cryptoAddress : cryptoAddressArrayList) {
            if(seenNames.contains(cryptoAddress.getCrypto().getName())) { continue; }

            String info = InfoUtil.getInfo(cryptoAddress);
            if(info != null) {
                infoText.append(info);
            }

            seenNames.add(cryptoAddress.getCrypto().getName());
        }

        BaseDialogFragment infoDialogFragment = BaseDialogFragment.newInstance(InfoDialog.class, infoText.toString());
        infoDialogFragment.show(context, "crypto_info");
    }

    public static String getInfo(CryptoAddress cryptoAddress) {
        String cryptoName = cryptoAddress.getCrypto().getName();
        boolean isMainnet = cryptoAddress.network.isMainnet();
        String cryptoDisplayName = cryptoAddress.getCrypto().getDisplayName();

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
            case "SOL":
                info = "Transactions do not include block rewards, such as rent, that occur on the block level outside of any particular transaction.";
                break;
            default:
                return null;
        }

        return cryptoDisplayName + " (" + cryptoName + ")" + ": " + info + "\n\n";
    }
}
