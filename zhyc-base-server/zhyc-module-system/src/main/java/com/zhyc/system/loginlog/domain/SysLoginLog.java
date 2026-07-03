/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.loginlog.domain;

import java.time.LocalDateTime;

/**
 * 系统登录日志领域模型。
 *
 * <p>登录日志按 {@code tenantId} 做租户隔离，用于记录登录成功、失败和安全审计信息。</p>
 */
public class SysLoginLog {

    /** 数据库主键。 */
    private Long id;
    /** 租户业务编码。 */
    private String tenantId;
    /** 登录用户主键，未知用户可为空。 */
    private Long userId;
    /** 登录账号。 */
    private String username;
    /** 登录方式，例如 password、sso、oauth2。 */
    private String loginType;
    /** 登录结果，例如 success、failure。 */
    private String result;
    /** 客户端 IP。 */
    private String clientIp;
    /** 浏览器或客户端 User-Agent。 */
    private String userAgent;
    /** 创建时间。 */
    private LocalDateTime createdAt;

    /**
     * 创建空登录日志对象。
     */
    public SysLoginLog() {
    }

    /**
     * 创建完整登录日志对象。
     *
     * @param id 数据库主键
     * @param tenantId 租户业务编码
     * @param userId 登录用户主键
     * @param username 登录账号
     * @param loginType 登录方式
     * @param result 登录结果
     * @param clientIp 客户端 IP
     * @param userAgent 浏览器或客户端 User-Agent
     * @param createdAt 创建时间
     */
    public SysLoginLog(Long id, String tenantId, Long userId, String username, String loginType, String result,
                       String clientIp, String userAgent, LocalDateTime createdAt) {
        this.id = id;
        this.tenantId = tenantId;
        this.userId = userId;
        this.username = username;
        this.loginType = loginType;
        this.result = result;
        this.clientIp = clientIp;
        this.userAgent = userAgent;
        this.createdAt = createdAt;
    }

    /**
     * 返回数据库主键。
     *
     * @return 数据库主键
     */
    public Long getId() {
        return id;
    }

    /**
     * 设置数据库主键。
     *
     * @param id 数据库主键
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * 返回租户业务编码。
     *
     * @return 租户业务编码
     */
    public String getTenantId() {
        return tenantId;
    }

    /**
     * 设置租户业务编码。
     *
     * @param tenantId 租户业务编码
     */
    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    /**
     * 返回登录用户主键。
     *
     * @return 登录用户主键
     */
    public Long getUserId() {
        return userId;
    }

    /**
     * 设置登录用户主键。
     *
     * @param userId 登录用户主键
     */
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    /**
     * 返回登录账号。
     *
     * @return 登录账号
     */
    public String getUsername() {
        return username;
    }

    /**
     * 设置登录账号。
     *
     * @param username 登录账号
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * 返回登录方式。
     *
     * @return 登录方式
     */
    public String getLoginType() {
        return loginType;
    }

    /**
     * 设置登录方式。
     *
     * @param loginType 登录方式
     */
    public void setLoginType(String loginType) {
        this.loginType = loginType;
    }

    /**
     * 返回登录结果。
     *
     * @return 登录结果
     */
    public String getResult() {
        return result;
    }

    /**
     * 设置登录结果。
     *
     * @param result 登录结果
     */
    public void setResult(String result) {
        this.result = result;
    }

    /**
     * 返回客户端 IP。
     *
     * @return 客户端 IP
     */
    public String getClientIp() {
        return clientIp;
    }

    /**
     * 设置客户端 IP。
     *
     * @param clientIp 客户端 IP
     */
    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
    }

    /**
     * 返回浏览器或客户端 User-Agent。
     *
     * @return 浏览器或客户端 User-Agent
     */
    public String getUserAgent() {
        return userAgent;
    }

    /**
     * 设置浏览器或客户端 User-Agent。
     *
     * @param userAgent 浏览器或客户端 User-Agent
     */
    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    /**
     * 返回创建时间。
     *
     * @return 创建时间
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * 设置创建时间。
     *
     * @param createdAt 创建时间
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
