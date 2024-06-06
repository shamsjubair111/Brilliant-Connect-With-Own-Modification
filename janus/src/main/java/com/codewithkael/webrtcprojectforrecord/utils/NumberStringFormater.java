package com.codewithkael.webrtcprojectforrecord.utils;

public class NumberStringFormater {
    public static String reformatPhoneNumber(String input) {
        String[] parts = input.split("@");
        // Consider only the part before the "@" for digit extraction
        String phonePart = parts[0];
        // Removing any non-digit characters
        String digits = phonePart.replaceAll("\\D", "");

        // Checking the length of the digits and prefixing accordingly
        if (digits.startsWith("880") && digits.length() == 13) {
            return "00" + digits;
        } else if (digits.startsWith("880") && digits.length() == 12) {
            return "00880" + digits.substring(3);
        } else if (digits.startsWith("0") && digits.length() == 11) {
            return "00880" + digits.substring(1);
        } else if (digits.length() == 10 && digits.startsWith("1")) {
            return "00880" + digits;
        }  else {
            return "";
        }
    }
}
