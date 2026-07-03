/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.user.service;

/**
 * 系统用户修改密码命令。
 *
 * <p>仅在内存中承载本次改密所需明文密码，服务层完成校验和哈希后不得持久化明文或输出日志。</p>
 */
public class SysUserPasswordChangeCommand {

    /** 租户业务编码，用于限定改密用户所属租户。 */
    private final String tenantId;
    /** 登录账号，用于定位当前改密用户。 */
    private final String username;
    /** 当前密码明文，仅用于校验旧密码是否正确。 */
    private final String oldPassword;
    /** 新密码明文，仅用于密码策略校验和生成哈希。 */
    private final String newPassword;

    /**
     * 创建系统用户修改密码命令。
     *
     * @param tenantId 租户业务编码
     * @param username 登录账号
     * @param oldPassword 当前密码明文
     * @param newPassword 新密码明文
     */
    public SysUserPasswordChangeCommand(String tenantId, String username, String oldPassword, String newPassword) {
        this.tenantId = tenantId;
        this.username = username;
        this.oldPassword = oldPassword;
        this.newPassword = newPassword;
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
     * 返回当前密码明文。
     *
     * @return 当前密码明文
     */
    public String getOldPassword() {
        return oldPassword;
    }

    /**
     * 返回新密码明文。
     *
     * @return 新密码明文
     */
    public String getNewPassword() {
        return newPassword;
    }
}
