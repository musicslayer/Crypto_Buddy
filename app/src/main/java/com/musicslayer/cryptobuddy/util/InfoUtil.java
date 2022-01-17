package com.musicslayer.cryptobuddy.util;

import android.content.Context;

import com.musicslayer.cryptobuddy.api.address.CryptoAddress;
import com.musicslayer.cryptobuddy.asset.exchange.Exchange;
import com.musicslayer.cryptobuddy.dialog.BaseDialogFragment;
import com.musicslayer.cryptobuddy.dialog.InfoDialog;

import java.util.ArrayList;

// Class to store specific info we want to show the user.
public class InfoUtil {
    public static boolean hasInfo_CryptoAddress(ArrayList<CryptoAddress> cryptoAddressArrayList) {
        // Return true if there is any info to show for any crypto.
        for(CryptoAddress cryptoAddress : cryptoAddressArrayList) {
            if(InfoUtil.getInfo_CryptoAddress(cryptoAddress) != null) {
                return true;
            }
        }

        return false;
    }

    public static void showInfo_CryptoAddress(Context context, ArrayList<CryptoAddress> cryptoAddressArrayList) {
        StringBuilder infoText = new StringBuilder();
        ArrayList<String> seenNames = new ArrayList<>();

        for(CryptoAddress cryptoAddress : cryptoAddressArrayList) {
            if(seenNames.contains(cryptoAddress.getCrypto().getName())) { continue; }

            String info = InfoUtil.getInfo_CryptoAddress(cryptoAddress);
            if(info != null) {
                infoText.append(info);
            }

            seenNames.add(cryptoAddress.getCrypto().getName());
        }

        BaseDialogFragment infoDialogFragment = BaseDialogFragment.newInstance(InfoDialog.class, infoText.toString());
        infoDialogFragment.show(context, "info");
    }

    private static String getInfo_CryptoAddress(CryptoAddress cryptoAddress) {
        String cryptoName = cryptoAddress.getCrypto().getName();
        boolean isMainnet = cryptoAddress.network.isMainnet();
        String cryptoDisplayName = cryptoAddress.getCrypto().getDisplayName();

        String info;
        switch(cryptoName) {
            case "ATOM":
                info = "Some liquidity pool token swap operations will not show up as transactions.";
                break;
            case "BNBc":
                if(isMainnet) {
                    return null;
                }
                else {
                    info = "Transactions for testnet addresses are not available.";
                }
                break;
            case "ETH":
                if(isMainnet) {
                    return null;
                }
                else {
                    info = "Token balances for testnet addresses are not available.";
                }
                break;
            case "VET":
                info = "VTHO balance includes generated rewards from holding VET, but these rewards do not show up as transactions.";
                break;
            case "SOL":
                info = "Transactions do not include block rewards, such as rent, that occur on the block level outside of any particular transaction.";
                break;
            default:
                return null;
        }

        return cryptoDisplayName + " (" + cryptoName + ")" + ": " + info + "\n\n";
    }

    public static boolean hasInfo_Exchange(ArrayList<Exchange> exchangeArrayList) {
        // Return true if there is any info to show for any exchange.
        for(Exchange exchange : exchangeArrayList) {
            if(InfoUtil.getInfo_Exchange(exchange) != null) {
                return true;
            }
        }

        return false;
    }

    public static void showInfo_Exchange(Context context, ArrayList<Exchange> exchangeArrayList) {
        StringBuilder infoText = new StringBuilder();
        ArrayList<String> seenNames = new ArrayList<>();

        for(Exchange exchange : exchangeArrayList) {
            if(seenNames.contains(exchange.getName())) { continue; }

            String info = InfoUtil.getInfo_Exchange(exchange);
            if(info != null) {
                infoText.append(info);
            }

            seenNames.add(exchange.getName());
        }

        BaseDialogFragment infoDialogFragment = BaseDialogFragment.newInstance(InfoDialog.class, infoText.toString());
        infoDialogFragment.show(context, "info");
    }

    private static String getInfo_Exchange(Exchange exchange) {
        // Currently no exchanges actually have any problems.
        return null;
    }
}
