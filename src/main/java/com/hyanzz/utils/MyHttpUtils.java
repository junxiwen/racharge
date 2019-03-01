package com.hyanzz.utils;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by Administrator on 2019/2/19.
 */
public class MyHttpUtils {
    /**
     * 获取当前请求对象
     *
     * @return
     */
    public static HttpServletRequest getRequest() {
        try {
            return ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        } catch (Exception e) {
            return null;
        }
    }


    /**
     * 获取客户端真实ip地址
     *
     * @return
     */
    public static String getRealClientIP() {
        return getRealIP(getRequest());
    }

    private static String getRealIP(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (filterIp(ip) != null) {
            return ip;
        }

        ip = request.getHeader("X-Real-IP");
        if (filterIp(ip) != null) {
            return ip;
        }

        ip = request.getHeader("Proxy-Client-IP");
        if (filterIp(ip) != null) {
            return ip;
        }

        ip = request.getHeader("WL-Proxy-Client-IP");
        if (filterIp(ip) != null) {
            return ip;
        }

        return request.getRemoteAddr();
    }

    private static String filterIp(String ip) {
        try {
            if (ip == null) {
                return null;
            }
            int index = ip.indexOf(",");
            if (index != -1) {
                String[] allForward = ip.split(",");
                for (int i = 0; i < allForward.length; i++) {
                    String oneIp = allForward[i].trim();
                    if (StringUtils.isNotBlank(oneIp) && !"unKnown".equalsIgnoreCase(oneIp) && !oneIp.startsWith("127.")
                            && !oneIp.startsWith("10.") && !oneIp.startsWith("100.") && !oneIp.startsWith("192.168")) {
                        return oneIp;
                    }
                }
            } else {
                String oneIp = ip.trim();
                if (StringUtils.isNotBlank(oneIp) && !"unKnown".equalsIgnoreCase(oneIp) && !oneIp.startsWith("127.")
                        && !oneIp.startsWith("10.") && !oneIp.startsWith("100.") && !oneIp.startsWith("192.168")) {
                    return oneIp;
                }
            }
        } catch (Exception e) {

        }
        return null;
    }

}
