/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.loginlog.service;

/**
 * 系统登录日志记录命令。
 */
public class SysLoginLogRecordCommand {

    /** 租户业务编码。 */
    private final String tenantId;
    /** 登录用户主键。 */
    private final Long userId;
    /** 登录账号。 */
    private final String username;
    /** 登录方式。 */
    private final String loginType;
    /** 登录结果。 */
    private final String result;
    /** 客户端 IP。 */
    private final String clientIp;
    /** 浏览器或客户端 User-Agent。 */
    private final String userAgent;

    /**
     * 创建系统登录日志记录命令。
     *
     * @param tenantId 租户业务编码
     * @param userId 登录用户主键
     * @param username 登录账号
     * @param loginType 登录方式
     * @param result 登录结果
     * @param clientIp 客户端 IP
     * @param userAgent 浏览器或客户端 User-Agent
     */
    public SysLoginLogRecordCommand(String tenantId, Long userId, String username, String loginType, String result,
                                    String clientIp, String userAgent) {
        this.tenantId = tenantId;
        this.userId = userId;
        this.username = username;
        this.loginType = loginType;
        this.result = result;
        this.clientIp = clientIp;
        this.userAgent = userAgent;
    }

    /** @return 租户业务编码 */
    public String getTenantId() {
        return tenantId;
    }

    /** @return 登录用户主键 */
    public Long getUserId() {
        return userId;
    }

    /** @return 登录账号 */
    public String getUsername() {
        return username;
    }

    /** @return 登录方式 */
    public String getLoginType() {
        return loginType;
    }

    /** @return 登录结果 */
    public String getResult() {
        return result;
    }

    /** @return 客户端 IP */
    public String getClientIp() {
        return clientIp;
    }

    /** @return 浏览器或客户端 User-Agent */
    public String getUserAgent() {
        return userAgent;
    }
}
