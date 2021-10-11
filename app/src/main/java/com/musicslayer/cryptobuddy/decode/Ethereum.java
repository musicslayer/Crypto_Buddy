package com.musicslayer.cryptobuddy.decode;

// Ethereum addresses don't generally use checksums.

import java.util.Arrays;

public class Ethereum {
    final private static String charList = "0123456789ABCDEFabcdef";
    private static final int[] indexList = new int[128];
    static {
        Arrays.fill(indexList, -1);

        char[] charList_c = charList.toCharArray();

        for (int i = 0; i < charList_c.length; i++) {
            indexList[charList_c[i]] = i;
        }
    }

    public static boolean isAddress(String address) {
        // All ETH formatted addresses start with 0x and then have 40 hexadecimal characters.
        char[] address_char = address.toCharArray();
        if(address_char.length != 42 || address_char[0] != '0' || address_char[1] != 'x') {
            return false;
        }

        for(int i = 2; i < address_char.length; i++) {
            int p = indexList[address_char[i]];
            if (p == -1) { return false; }
        }

        return true;
    }
}
