/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.loginlog.domain;

/**
 * 系统登录结果枚举。
 *
 * <p>结果编码持久化到 sys_login_log.result 字段，用于安全审计、登录失败分析和后台监控统计。</p>
 */
public enum SysLoginResult {

    /** 登录成功，表示认证通过并进入平台会话。 */
    SUCCESS("success", "成功"),

    /** 登录失败，表示认证未通过或安全策略拒绝。 */
    FAILURE("failure", "失败");

    /** 持久化结果编码；对应登录日志表 result 字段。 */
    private final String code;

    /** 结果中文说明；用于后台展示和审计说明。 */
    private final String description;

    /**
     * 创建系统登录结果枚举。
     *
     * @param code 持久化结果编码
     * @param description 结果中文说明
     */
    SysLoginResult(String code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 获取持久化结果编码。
     *
     * @return sys_login_log.result 字段使用的结果编码
     */
    public String getCode() {
        return code;
    }

    /**
     * 获取结果中文说明。
     *
     * @return 结果中文说明
     */
    public String getDescription() {
        return description;
    }

    /**
     * 根据持久化编码解析登录结果。
     *
     * <p>只允许安全审计支持的登录结果，避免未定义结果影响登录监控统计。</p>
     *
     * @param code 持久化结果编码
     * @return 匹配的登录结果枚举
     */
    public static SysLoginResult fromCode(String code) {
        for (SysLoginResult result : values()) {
            if (result.code.equals(code)) {
                return result;
            }
        }
        throw new IllegalArgumentException("登录结果不支持: " + code);
    }
}
