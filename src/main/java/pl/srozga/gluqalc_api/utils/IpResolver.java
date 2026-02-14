package pl.srozga.gluqalc_api.utils;

import jakarta.servlet.http.HttpServletRequest;

public class IpResolver {
    public static String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip))
            ip = request.getHeader("X-Real-IP");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip))
            ip = request.getRemoteAddr();

        if ("0:0:0:0:0:0:0:1".equals(ip))
            ip = "127.0.0.1";

        return ip != null ? ip.contains(",") ? ip.split(",")[0].trim() : ip : "unknown";
    }
}
