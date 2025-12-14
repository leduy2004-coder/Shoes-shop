package com.java.shoes_service.config;

import com.java.shoes_service.utility.VNPayUtil;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

@Configuration
@Slf4j
public class VNPAYConfig {
    @Getter
    @Value("${payment.vnPay.url}")
    private String vnp_PayUrl;

    @Getter
    @Value("${payment.vnPay.returnUrl}")
    private String vnp_ReturnUrl;

    @Value("${payment.vnPay.tmnCode}")
    private String vnp_TmnCode;
    @Getter
    @Value("${payment.vnPay.secretKey}")
    private String secretKey;
    @Value("${payment.vnPay.version}")
    private String vnp_Version;
    @Value("${payment.vnPay.command}")
    private String vnp_Command;
    @Value("${payment.vnPay.orderType}")
    private String orderType;

    @Value("${payment.vnPay.environment:local}")
    private String environment;

    public Map<String, String> getVNPayConfig() {
        Map<String, String> vnpParamsMap = new HashMap<>();
        vnpParamsMap.put("vnp_Version", this.vnp_Version);
        vnpParamsMap.put("vnp_Command", this.vnp_Command);
        vnpParamsMap.put("vnp_TmnCode", this.vnp_TmnCode);
        vnpParamsMap.put("vnp_CurrCode", "VND");

        // Tạo mã giao dịch
        String txnRef;
        if ("docker".equals(environment)) {
            // Tạo mã giao dịch đặc biệt cho môi trường Docker
            Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
            txnRef = formatter.format(cal.getTime()) + VNPayUtil.getRandomNumber(6);
        } else {
            // Mã giao dịch thông thường cho môi trường local
            txnRef = System.currentTimeMillis() + VNPayUtil.getRandomNumber(4);
        }
        vnpParamsMap.put("vnp_TxnRef", txnRef);

        vnpParamsMap.put("vnp_OrderInfo", "Thanh toan don hang:" + txnRef);
        vnpParamsMap.put("vnp_OrderType", this.orderType);
        vnpParamsMap.put("vnp_Locale", "vn");

        // Thời gian giao dịch
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String vnpCreateDate = formatter.format(calendar.getTime());
        vnpParamsMap.put("vnp_CreateDate", vnpCreateDate);

        // Môi trường Docker có thể cần thời gian hết hạn dài hơn
        int expireMinutes = "docker".equals(environment) ? 30 : 15;
        calendar.add(Calendar.MINUTE, expireMinutes);
        String vnp_ExpireDate = formatter.format(calendar.getTime());
        vnpParamsMap.put("vnp_ExpireDate", vnp_ExpireDate);

        // Log debug
        log.debug("Environment: {}", environment);

        return vnpParamsMap;
    }
}