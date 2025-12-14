package com.java.shoes_service.utility;
import jakarta.servlet.http.HttpServletRequest;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

public class VNPayUtil {
    public static String hmacSHA512(final String key, final String data) {
        try {
            if (key == null || data == null) {
                throw new NullPointerException();
            }
            final Mac hmac512 = Mac.getInstance("HmacSHA512");
            byte[] hmacKeyBytes = key.getBytes();
            final SecretKeySpec secretKey = new SecretKeySpec(hmacKeyBytes, "HmacSHA512");
            hmac512.init(secretKey);
            byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);
            byte[] result = hmac512.doFinal(dataBytes);
            StringBuilder sb = new StringBuilder(2 * result.length);
            for (byte b : result) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();

        } catch (Exception ex) {
            return "";
        }
    }

    public static String getIpAddress(HttpServletRequest request) {
        String ipAddress;

        try {
            // Kiểm tra các header proxy phổ biến
            String[] headers = {
                    "X-Forwarded-For",
                    "Proxy-Client-IP",
                    "WL-Proxy-Client-IP",
                    "HTTP_X_FORWARDED_FOR",
                    "HTTP_X_FORWARDED",
                    "HTTP_X_CLUSTER_CLIENT_IP",
                    "HTTP_CLIENT_IP",
                    "HTTP_FORWARDED_FOR",
                    "HTTP_FORWARDED",
                    "HTTP_VIA",
                    "REMOTE_ADDR"
            };

            ipAddress = null;
            for (String header : headers) {
                ipAddress = request.getHeader(header);
                if (ipAddress != null && !ipAddress.isEmpty() && !"unknown".equalsIgnoreCase(ipAddress)) {
                    break;
                }
            }

            if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
                ipAddress = request.getRemoteAddr();
            }

            // Nếu là địa chỉ IPv6 localhost
            if (ipAddress.equals("0:0:0:0:0:0:0:1")) {
                ipAddress = "127.0.0.1";
            }

            // Nếu là địa chỉ docker nội bộ (172.x.x.x), thay bằng địa chỉ mặc định
            if (ipAddress.startsWith("172.") && ipAddress.split("\\.").length == 4) {
                // Sử dụng IP mặc định nếu đang ở môi trường Docker
                // Bạn có thể thay thế bằng một địa chỉ IP công khai của bạn
                ipAddress = "127.0.0.1";
            }

            // Nếu có nhiều IP, lấy IP đầu tiên
            if (ipAddress.length() > 15 && ipAddress.indexOf(",") > 0) {
                ipAddress = ipAddress.substring(0, ipAddress.indexOf(","));
            }
        } catch (Exception e) {
            // Trả về địa chỉ IP mặc định thay vì chuỗi lỗi
            ipAddress = "127.0.0.1";
        }

        return ipAddress;
    }

    public static String getRandomNumber(int len) {
        Random rnd = new Random();
        String chars = "0123456789";
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            sb.append(chars.charAt(rnd.nextInt(chars.length())));
        }
        return sb.toString();
    }
    public static String getPaymentURL(Map<String, String> paramsMap, boolean encodeKey) {
        return paramsMap.entrySet().stream()
                .filter(entry -> entry.getValue() != null && !entry.getValue().isEmpty())
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> {
                    try {
                        String key = encodeKey ?
                                URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8) :
                                entry.getKey();
                        String value = URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8);
                        return key + "=" + value;
                    } catch (Exception e) {
                        // Log lỗi và trả về cặp key-value không mã hóa trong trường hợp lỗi
                        System.err.println("Error encoding URL parameter: " + e.getMessage());
                        return entry.getKey() + "=" + entry.getValue();
                    }
                })
                .collect(Collectors.joining("&"));
    }
    public static boolean isRunningInDocker() {
        try {
            // Đọc cgroups để kiểm tra xem có đang chạy trong Docker không
            java.nio.file.Path path = java.nio.file.Paths.get("/proc/1/cgroup");
            if (java.nio.file.Files.exists(path)) {
                String content = new String(java.nio.file.Files.readAllBytes(path));
                return content.contains("docker");
            }
        } catch (Exception e) {
            // Không làm gì cả
        }
        return false;
    }

    public static String getEnvironmentInfo() {
        StringBuilder info = new StringBuilder();
        info.append("Environment Info:\n");
        info.append("- Running in Docker: ").append(isRunningInDocker()).append("\n");
        info.append("- Java Version: ").append(System.getProperty("java.version")).append("\n");
        info.append("- OS: ").append(System.getProperty("os.name")).append(" ").append(System.getProperty("os.version")).append("\n");
        info.append("- Timezone: ").append(java.util.TimeZone.getDefault().getID()).append("\n");

        // Kiểm tra các biến môi trường quan trọng
        String[] envVars = {"HOSTNAME", "TZ"};
        for (String var : envVars) {
            info.append("- ").append(var).append(": ").append(System.getenv(var)).append("\n");
        }

        return info.toString();
    }
}
