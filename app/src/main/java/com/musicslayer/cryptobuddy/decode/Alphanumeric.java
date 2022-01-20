package com.musicslayer.cryptobuddy.decode;

import java.util.Arrays;
import java.util.Random;

public class Alphanumeric {
    final private static String charList = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final int[] indexList = new int[128];
    static {
        Arrays.fill(indexList, -1);

        char[] charList_c = charList.toCharArray();

        for (int i = 0; i < charList_c.length; i++) {
            indexList[charList_c[i]] = i;
        }
    }

    public static boolean isAlphanumeric(String address) {
        for(char t : address.toCharArray()) {
            int p = indexList[t];
            if (p == -1) { return false; }
        }

        return true;
    }

    public static String createRandomString(int length) {
        StringBuilder s = new StringBuilder();
        Random rand = new Random();
        for(int i = 0; i < length; i++) {
            int idx = rand.nextInt(charList.length());
            s.append(charList.charAt(idx));
        }
        return s.toString();
    }
}
