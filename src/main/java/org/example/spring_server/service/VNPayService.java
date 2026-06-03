package org.example.spring_server.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.example.spring_server.config.VNPayConfig;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
@RequiredArgsConstructor
public class VNPayService {

    private final VNPayConfig vnPayConfig;

    public String createPaymentUrl(long amount, String txnRef,
                                   String orderInfo, HttpServletRequest request) {
        String vnpVersion   = "2.1.0";
        String vnpCommand   = "pay";
        String vnpCurrCode  = "VND";
        String vnpLocale    = "vn";
        String vnpOrderType = "other";
        String vnpIpAddr    = VNPayConfig.getIpAddress(request);

        Map<String, String> vnpParams = new HashMap<>();
        vnpParams.put("vnp_Version",    vnpVersion);
        vnpParams.put("vnp_Command",    vnpCommand);
        vnpParams.put("vnp_TmnCode",    vnPayConfig.getTmnCode());
        vnpParams.put("vnp_Amount",     String.valueOf(amount * 100)); // VNPAY uses x100
        vnpParams.put("vnp_CurrCode",   vnpCurrCode);
        vnpParams.put("vnp_TxnRef",     txnRef);
        vnpParams.put("vnp_OrderInfo",  orderInfo);
        vnpParams.put("vnp_OrderType",  vnpOrderType);
        vnpParams.put("vnp_Locale",     vnpLocale);
        vnpParams.put("vnp_ReturnUrl",  vnPayConfig.getReturnUrl());
        vnpParams.put("vnp_IpAddr",     vnpIpAddr);

        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        vnpParams.put("vnp_CreateDate", formatter.format(cld.getTime()));

        cld.add(Calendar.MINUTE, 15);
        vnpParams.put("vnp_ExpireDate", formatter.format(cld.getTime()));

        // Sort and build hash + query — same logic as ajaxServlet.java
        List<String> fieldNames = new ArrayList<>(vnpParams.keySet());
        Collections.sort(fieldNames);

        StringBuilder hashData = new StringBuilder();
        StringBuilder query    = new StringBuilder();
        Iterator<String> itr   = fieldNames.iterator();

        while (itr.hasNext()) {
            String fieldName  = itr.next();
            String fieldValue = vnpParams.get(fieldName);
            if (fieldValue != null && !fieldValue.isEmpty()) {
                hashData.append(fieldName).append('=')
                        .append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII))
                        .append('=')
                        .append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                if (itr.hasNext()) {
                    query.append('&');
                    hashData.append('&');
                }
            }
        }

        String secureHash = VNPayConfig.hmacSHA512(vnPayConfig.getHashSecret(), hashData.toString());
        return vnPayConfig.getPayUrl() + "?" + query + "&vnp_SecureHash=" + secureHash;
    }

    public boolean verifyReturn(Map<String, String> params) {
        String receivedHash = params.get("vnp_SecureHash");

        // Remove hash fields before computing
        Map<String, String> filtered = new HashMap<>(params);
        filtered.remove("vnp_SecureHash");
        filtered.remove("vnp_SecureHashType");

        List<String> fieldNames = new ArrayList<>(filtered.keySet());
        Collections.sort(fieldNames);

        StringBuilder hashData = new StringBuilder();
        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName  = itr.next();
            String fieldValue = filtered.get(fieldName);
            if (fieldValue != null && !fieldValue.isEmpty()) {
                hashData.append(fieldName).append('=')
                        .append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                if (itr.hasNext()) hashData.append('&');
            }
        }

        String computed = VNPayConfig.hmacSHA512(vnPayConfig.getHashSecret(), hashData.toString());
        return computed.equalsIgnoreCase(receivedHash);
    }
}