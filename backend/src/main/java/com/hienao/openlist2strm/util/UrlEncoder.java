package com.hienao.openlist2strm.util;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * URL智能编码工具类
 *
 * <p>提供智能URL编码功能，能够正确处理URL中的协议、域名、路径和查询参数，
 * 只对需要编码的部分进行编码，保留URL结构的完整性。
 *
 * @author hienao
 * @since 2024-01-01
 */
public class UrlEncoder {

    /**
     * 对整个URL进行编码（不推荐，会编码协议和域名）
     */
    public static String encodeFullUrl(String url) {
        try {
            // 这种方法会编码整个URL，包括协议和域名部分
            return URLEncoder.encode(url, StandardCharsets.UTF_8)
                    .replace("+", "%20"); // 将+替换为%20
        } catch (Exception e) {
            throw new RuntimeException("URL编码失败", e);
        }
    }

    /**
     * 智能URL编码 - 只编码路径部分，保留协议、域名、查询参数结构
     */
    public static String encodeUrlSmart(String url) {
        try {
            // 分离查询参数
            String[] parts = url.split("\\?", 2);
            String baseUrl = parts[0];
            String query = parts.length > 1 ? parts[1] : null;

            // 编码路径部分
            String encodedPath = encodePath(baseUrl);

            // 如果有查询参数，处理查询参数
            if (query != null) {
                return encodedPath + "?" + encodeQueryParams(query);
            }

            return encodedPath;
        } catch (Exception e) {
            throw new RuntimeException("URL编码失败", e);
        }
    }

    /**
     * 编码路径部分
     */
    private static String encodePath(String path) {
        // 分离协议和域名部分
        int protocolIndex = path.indexOf("://");
        if (protocolIndex == -1) {
            return encodePathSegment(path);
        }

        String protocol = path.substring(0, protocolIndex + 3);
        String rest = path.substring(protocolIndex + 3);

        // 分离域名和路径
        int domainEnd = rest.indexOf("/");
        if (domainEnd == -1) {
            return protocol + rest; // 没有路径，直接返回
        }

        String domain = rest.substring(0, domainEnd);
        String pathSegment = rest.substring(domainEnd);

        return protocol + domain + encodePathSegment(pathSegment);
    }

    /**
     * 编码路径段
     */
    private static String encodePathSegment(String path) {
        String[] segments = path.split("/");
        StringBuilder result = new StringBuilder();

        for (String segment : segments) {
            if (!segment.isEmpty()) {
                result.append("/").append(URLEncoder.encode(segment, StandardCharsets.UTF_8)
                        .replace("+", "%20"));
            }
        }

        return result.toString();
    }

    /**
     * 编码查询参数（保持参数名和值结构）
     */
    private static String encodeQueryParams(String query) {
        String[] params = query.split("&");
        StringBuilder result = new StringBuilder();

        for (String param : params) {
            if (!param.isEmpty()) {
                if (result.length() > 0) {
                    result.append("&");
                }

                String[] keyValue = param.split("=", 2);
                String encodedKey = URLEncoder.encode(keyValue[0], StandardCharsets.UTF_8);

                if (keyValue.length == 2) {
                    String encodedValue = URLEncoder.encode(keyValue[1], StandardCharsets.UTF_8);
                    result.append(encodedKey).append("=").append(encodedValue);
                } else {
                    result.append(encodedKey);
                }
            }
        }

        return result.toString();
    }
}