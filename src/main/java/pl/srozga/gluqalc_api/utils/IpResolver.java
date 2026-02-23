package pl.srozga.gluqalc_api.utils;

import jakarta.servlet.http.HttpServletRequest;
import lombok.NonNull;
import lombok.experimental.UtilityClass;

@UtilityClass
public class IpResolver {
    public static String getClientIp(@NonNull HttpServletRequest request) {
        String ip = request.getRemoteAddr();

        if (ip == null)
            return "unknown";

        if ("0:0:0:0:0:0:0:1".equals(ip))
            ip = "127.0.0.1";

        return ip;
    }
}
