package com.admin.common.util;

import org.springframework.stereotype.Component;

@Component
public class FieldUtils {

    /**
     * 驼峰转下划线（支持连续大写）
     * deptId → dept_id
     * userID → user_id
     */
    public static String camelToUnderline(String camel) {
        if (camel == null || camel.isEmpty()) return camel;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < camel.length(); i++) {
            char c = camel.charAt(i);
            if (Character.isUpperCase(c)) {
                // 处理连续大写：HTMLParser → html_parser
                if (i > 0 && Character.isLowerCase(camel.charAt(i - 1))) {
                    sb.append('_');
                } else if (i < camel.length() - 1 && Character.isLowerCase(camel.charAt(i + 1))) {
                    sb.append('_');
                }
            }
            sb.append(Character.toLowerCase(c));
        }
        return sb.toString();
    }
}