package com.java.auth_service.utility;


import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.Normalizer;
import java.util.regex.Pattern;

public class StringUtils {
    public static String deAccent(String str) {
        String nfdNormalizedString = Normalizer.normalize(str, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(nfdNormalizedString).replaceAll("").replaceAll("đ", "d").replaceAll("Đ", "D");
    }

    public static String decodeUtil(String code) throws UnsupportedEncodingException {
        return URLDecoder.decode(code, "UTF-8");
    }

    public static String setCodeWithPrefix(String prefix, String code, int length) {
        int zerosToAdd = length - code.length();
        StringBuilder sb = new StringBuilder(prefix);
        for (int i = 0; i < zerosToAdd; i++) {
            sb.append('0');
        }
        sb.append(code);
        return sb.toString();
    }

    public static String formatString(String data) {
        return data == null ? "" : data;
    }

    public static String incrementCode(String code, int i) {
        int numericCode = Integer.parseInt(code);
        numericCode += i;
        return String.valueOf(numericCode);
    }
}
