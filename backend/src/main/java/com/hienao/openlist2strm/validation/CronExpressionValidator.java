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
        
        // 使用Quartz的CronExpression来验证表达式格式
        try {
            return CronExpression.isValidExpression(cronExpression);
        } catch (Exception e) {
            // 如果验证过程中出现异常，则视为无效
            return false;
        }
    }
}