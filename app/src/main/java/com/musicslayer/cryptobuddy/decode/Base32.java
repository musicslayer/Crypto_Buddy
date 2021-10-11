package com.musicslayer.cryptobuddy.decode;

import java.util.Arrays;

// Currently only Algorand uses this. We don't check any checksums here.

public class Base32 {
    final private static String charList = "234567ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final int[] indexList = new int[128];
    static {
        Arrays.fill(indexList, -1);

        char[] charList_c = charList.toCharArray();

        for (int i = 0; i < charList_c.length; i++) {
            indexList[charList_c[i]] = i;
        }
    }

    public static boolean isAddress(String address) {
        for(char t : address.toCharArray()) {
            int p = indexList[t];
            if (p == -1) { return false; }
        }

        return true;
    }
}
