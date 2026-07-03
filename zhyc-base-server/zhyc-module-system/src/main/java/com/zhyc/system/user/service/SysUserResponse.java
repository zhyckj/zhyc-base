/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.user.service;

/**
 * 系统用户响应对象。
 */
public class SysUserResponse {

    /** 用户主键。 */
    private final Long id;
    /** 租户业务编码。 */
    private final String tenantId;
    /** 登录账号。 */
    private final String username;
    /** 用户显示名称。 */
    private final String nickname;
    /** 用户状态。 */
    private final String status;

    /**
     * 创建系统用户响应对象。
     *
     * @param id 用户主键
     * @param tenantId 租户业务编码
     * @param username 登录账号
     * @param nickname 用户显示名称
     * @param status 用户状态
     */
    public SysUserResponse(Long id, String tenantId, String username, String nickname, String status) {
        this.id = id;
        this.tenantId = tenantId;
        this.username = username;
        this.nickname = nickname;
        this.status = status;
    }

    /**
     * 返回用户主键。
     *
     * @return 用户主键
     */
    public Long getId() {
        return id;
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
     * 返回登录账号。
     *
     * @return 登录账号
     */
    public String getUsername() {
        return username;
    }

    /**
     * 返回用户显示名称。
     *
     * @return 用户显示名称
     */
    public String getNickname() {
        return nickname;
    }

    /**
     * 返回用户状态。
     *
     * @return 用户状态
     */
    public String getStatus() {
        return status;
    }
}
