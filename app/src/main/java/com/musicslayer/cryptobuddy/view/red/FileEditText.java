package com.musicslayer.cryptobuddy.view.red;

import android.content.Context;
import android.util.AttributeSet;

import java.util.Arrays;

public class FileEditText extends RedEditText {
    final private static String charList = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz_. ";
    private static final int[] indexList = new int[128];
    static {
        Arrays.fill(indexList, -1);

        char[] charList_c = charList.toCharArray();

        for (int i = 0; i < charList_c.length; i++) {
            indexList[charList_c[i]] = i;
        }
    }

    public FileEditText(Context context) {
        this(context, null);
    }

    public FileEditText(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);

        // Enforce a maximum length to protect algorithms from having to process large values.
        // Give this text 100 characters.
        setMaxLength(100);
    }

    // Returns if the value is a valid file.
    public boolean condition() {
        try {
            String text = this.getTextString();
            return !text.isEmpty() && isValidFile(this.getTextString());
        }
        catch(Exception ignored) {
            return false;
        }
    }

    public static boolean isValidFile(String str) {
        for(char t : str.toCharArray()) {
            int p = indexList[t];
            if (p == -1) { return false; }
        }

        return true;
    }
}
