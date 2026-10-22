package com.finvane.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;

public final class IndianCurrencyUtility {

    private static final String[] ONES = {
        "", "One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine", "Ten",
        "Eleven", "Twelve", "Thirteen", "Fourteen", "Fifteen", "Sixteen", "Seventeen", "Eighteen", "Nineteen"
    };

    private static final String[] TENS = {
        "", "", "Twenty", "Thirty", "Forty", "Fifty", "Sixty", "Seventy", "Eighty", "Ninety"
    };

    private IndianCurrencyUtility() {}

    /**
     * Formats number in Indian comma format: e.g. 12,34,567.89
     */
    public static String formatCurrency(BigDecimal amount) {
        if (amount == null) return "₹0.00";
        BigDecimal rounded = amount.setScale(2, RoundingMode.HALF_UP);
        boolean isNegative = rounded.compareTo(BigDecimal.ZERO) < 0;
        BigDecimal abs = rounded.abs();

        long integerPart = abs.longValue();
        int decimalPart = abs.remainder(BigDecimal.ONE).movePointRight(2).intValue();

        String intStr = String.valueOf(integerPart);
        StringBuilder sb = new StringBuilder();

        if (intStr.length() <= 3) {
            sb.append(intStr);
        } else {
            String lastThree = intStr.substring(intStr.length() - 3);
            String rest = intStr.substring(0, intStr.length() - 3);

            StringBuilder restFormatted = new StringBuilder();
            int count = 0;
            for (int i = rest.length() - 1; i >= 0; i--) {
                restFormatted.insert(0, rest.charAt(i));
                count++;
                if (count % 2 == 0 && i > 0) {
                    restFormatted.insert(0, ",");
                }
            }
            sb.append(restFormatted).append(",").append(lastThree);
        }

        String formatted = (isNegative ? "-₹" : "₹") + sb + "." + String.format("%02d", decimalPart);
        return formatted;
    }

    /**
     * Converts amount to Indian words (Rupees ... and Paise ... Only)
     */
    public static String amountToWords(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) == 0) {
            return "Rupees Zero Only";
        }

        BigDecimal abs = amount.abs().setScale(2, RoundingMode.HALF_UP);
        long rupees = abs.longValue();
        long paise = abs.remainder(BigDecimal.ONE).movePointRight(2).longValue();

        StringBuilder words = new StringBuilder();
        if (rupees > 0) {
            words.append("Rupees ").append(convertToIndianWords(rupees));
        }

        if (paise > 0) {
            if (rupees > 0) words.append(" and ");
            words.append(convertToIndianWords(paise)).append(" Paise");
        }

        words.append(" Only");
        return words.toString();
    }

    private static String convertToIndianWords(long n) {
        if (n == 0) return "";
        if (n < 20) return ONES[(int) n];
        if (n < 100) return TENS[(int) (n / 10)] + ((n % 10 != 0) ? " " + ONES[(int) (n % 10)] : "");
        if (n < 1000) return ONES[(int) (n / 100)] + " Hundred" + ((n % 100 != 0) ? " and " + convertToIndianWords(n % 100) : "");
        if (n < 100000) return convertToIndianWords(n / 1000) + " Thousand" + ((n % 1000 != 0) ? " " + convertToIndianWords(n % 1000) : "");
        if (n < 10000000) return convertToIndianWords(n / 100000) + " Lakh" + ((n % 100000 != 0) ? " " + convertToIndianWords(n % 100000) : "");
        return convertToIndianWords(n / 10000000) + " Crore" + ((n % 10000000 != 0) ? " " + convertToIndianWords(n % 10000000) : "");
    }
}
