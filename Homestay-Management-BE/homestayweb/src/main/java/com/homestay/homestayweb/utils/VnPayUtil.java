package com.homestay.homestayweb.utils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class VnPayUtil {

    public static String hmacSHA512(final String key, final String data) throws Exception {
        if (key == null || data == null) return null;

        Mac hmac512 = Mac.getInstance("HmacSHA512");
        byte[] hmacKeyBytes = key.getBytes(StandardCharsets.UTF_8);
        SecretKeySpec secretKey = new SecretKeySpec(hmacKeyBytes, "HmacSHA512");
        hmac512.init(secretKey);

        byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);
        byte[] result = hmac512.doFinal(dataBytes);
        StringBuilder sb = new StringBuilder(2 * result.length);
        for (byte b : result) {
            sb.append(String.format("%02x", b & 0xff));
        }
        return sb.toString();
    }

    public static String getPaymentUrl(Map<String, String> params, String vnpHashSecret, String vnpUrl) throws Exception {
        // Bước 1: Sắp xếp các tham số theo thứ tự alphabet
        List<String> sortedKeys = new ArrayList<>(params.keySet());
        Collections.sort(sortedKeys);

        // Bước 2: Tạo chuỗi dữ liệu để ký
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();

        for (String key : sortedKeys) {
            String value = params.get(key);
            if ((value != null) && (value.length() > 0)) {
                hashData.append(key).append('=').append(URLEncoder.encode(value, StandardCharsets.US_ASCII)).append('&');
                query.append(URLEncoder.encode(key, StandardCharsets.US_ASCII))
                        .append('=')
                        .append(URLEncoder.encode(value, StandardCharsets.US_ASCII))
                        .append('&');
            }
        }

        // Bỏ ký tự '&' cuối cùng
        if (hashData.length() > 0) hashData.setLength(hashData.length() - 1);
        if (query.length() > 0) query.setLength(query.length() - 1);

        // Bước 3: Tạo chữ ký
        String secureHash = hmacSHA512(vnpHashSecret, hashData.toString());

        // Bước 4: Gắn chữ ký vào URL
        String fullUrl = vnpUrl + "?" + query + "&vnp_SecureHash=" + secureHash;
        return fullUrl;
    }
}
