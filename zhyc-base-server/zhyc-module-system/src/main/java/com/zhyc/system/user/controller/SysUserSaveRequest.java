/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.user.controller;

/**
 * 系统用户保存请求。
 *
 * <p>用于后台新增或编辑系统用户，租户编码必须与当前后台上下文一致。</p>
 */
public class SysUserSaveRequest {

    /** 租户业务编码，用于共享表模式数据隔离。 */
    private String tenantId;
    /** 登录账号，新建后不建议修改。 */
    private String username;
    /** 用户显示名称。 */
    private String nickname;
    /** 初始密码或重置密码，编辑时为空则不修改密码。 */
    private String password;
    /** 用户状态，例如 enabled、disabled。 */
    private String status;

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
