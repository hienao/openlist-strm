package com.hienao.openlist2strm.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.quartz.CronExpression;

/**
 * Cron表达式验证器
 *
 * @author hienao
 * @since 2024-01-01
 */
public class CronExpressionValidator implements ConstraintValidator<ValidCronExpression, String> {

    @Override
    public boolean isValid(String cronExpression, ConstraintValidatorContext context) {
        // 如果为空字符串，则视为有效（允许为空）
        if (cronExpression == null || cronExpression.trim().isEmpty()) {
            return true;
        }
        
        // 尝试直接验证 Quartz 格式
        if (CronExpression.isValidExpression(cronExpression)) {
            return true;
        }
        
        // 如果不是 Quartz 格式，尝试转换为 Quartz 格式
        String convertedExpression = convertToQuartzFormat(cronExpression);
        if (convertedExpression != null && CronExpression.isValidExpression(convertedExpression)) {
            return true;
        }
        
        return false;
    }
    
    /**
     * 将 Unix Cron 格式转换为 Quartz Cron 格式
     * Unix Cron: 分 时 日 月 周 (5个字段)
     * Quartz Cron: 秒 分 时 日 月 周 (6个字段)
     */
    private String convertToQuartzFormat(String cronExpression) {
        if (cronExpression == null || cronExpression.trim().isEmpty()) {
            return null;
        }
        
        String[] parts = cronExpression.trim().split("\\s+");
        
        // 如果是 5 个字段，转换为 6 个字段的 Quartz 格式
        if (parts.length == 5) {
            // Unix格式: 分 时 日 月 周
            // Quartz格式: 秒 分 时 日 月 周
            // 需要将周几字段转换为 Quartz 格式
            String minute = parts[0];
            String hour = parts[1];
            String day = parts[2];
            String month = parts[3];
            String week = parts[4];
            
            // 在 Quartz 中，如果指定了周几，日期字段应该用 ?
            if (!week.equals("*")) {
                return "0 " + minute + " " + hour + " ? " + month + " " + week;
            } else {
                return "0 " + minute + " " + hour + " " + day + " " + month + " ?";
            }
        }
        
        // 如果是 6 个字段但最后一个不是问号，尝试修复
        if (parts.length == 6) {
            // 检查是否是 Unix 格式的 6 个字段（周几字段不是问号）
            if (!parts[5].equals("?")) {
                // 如果是 6 个字段但格式不对，返回 null
                return null;
            }
        }
        
        return null;
    }
}