package com.musicslayer.cryptobuddy.decode;

import java.util.Arrays;

public class Bech32 {
    final private static String charList = "qpzry9x8gf2tvdw0s3jn54khce6mua7l";
    private static final int[] indexList = new int[128];
    static {
        Arrays.fill(indexList, -1);

        char[] charList_c = charList.toCharArray();

        for (int i = 0; i < charList_c.length; i++) {
            indexList[charList_c[i]] = i;
        }
    }

    // Constants used in polymod algorithm.
    private static final int[] generator = {0x3b6a57b2, 0x26508e6d, 0x1ea119fa, 0x3d4233dd, 0x2a1462b3};

    public static boolean hasValidChecksum(String address) {
        // Address must have at least 1 letter.
        String lowercased = address.toLowerCase();
        String uppercased = address.toUpperCase();
        if (lowercased.equals(address) && uppercased.equals(address)) {
            return false;
        }

        address = lowercased;

        // The human readable part (HRP) is before the "1", which should be only present once.
        // Also, the HRP has to have at least one character to it.
        int pos = address.indexOf('1');
        int posLast = address.lastIndexOf('1');
        if (pos < 1 || pos != posLast) {
            return false;
        }

        String hrp = address.substring(0, pos);
        for (int i = 0; i < hrp.length(); i++) {
            char c = hrp.charAt(i);
            if (c < 33 || c > 126) { // Boundaries of "normal" ascii characters.
                return false;
            }
        }

        byte[] data = new byte[address.length() - pos - 1];
        for (int a = pos + 1, i = 0; a < address.length(); a++, i++) {
            int p = indexList[address.charAt(a)];
            if (p == -1) { return false; }
            data[i] = (byte) p;
        }

        // Use polymod algorithm to verify the checksum.
        byte[] ehrp = hrpExpand(hrp);
        byte[] values = new byte[ehrp.length + data.length];
        System.arraycopy(ehrp, 0, values, 0, ehrp.length);
        System.arraycopy(data, 0, values, ehrp.length, data.length);
        return polymod(values) == 1;
    }

    private static byte[] hrpExpand(String hrp) {
        byte[] ret = new byte[hrp.length() * 2 + 1];
        for (int i = 0; i < hrp.length(); i++) {
            char c = hrp.charAt(i);
            ret[i] = (byte) (c >> 5);
        }
        for (int i = 0; i < hrp.length(); i++) {
            char c = hrp.charAt(i);
            ret[i + hrp.length() + 1] = (byte) (c & 31);
        }
        return ret;
    }

    private static int polymod(byte[] values) {
        int chk = 1;
        for (byte value : values) {
            int v = value & 0xff;
            int top = chk >>> 25;
            chk = (chk & 0x1ffffff) << 5 ^ v;
            for (int j = 0; j < 5; j++) {
                if (((top >> j) & 1) == 1) {
                    chk ^= generator[j];
                }
            }
        }
        return chk;
    }
}
