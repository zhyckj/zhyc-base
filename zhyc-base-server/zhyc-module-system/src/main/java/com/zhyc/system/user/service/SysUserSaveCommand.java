/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.user.service;

/**
 * 系统用户保存命令。
 *
 * <p>由 Controller 完成请求对象转换，Service 负责校验密码策略、租户隔离和持久化边界。</p>
 */
public class SysUserSaveCommand {

    /** 用户主键；为空表示新增。 */
    private final Long id;
    /** 租户业务编码。 */
    private final String tenantId;
    /** 登录账号。 */
    private final String username;
    /** 用户显示名称。 */
    private final String nickname;
    /** 明文密码；新增必填，编辑为空表示不修改。 */
    private final String password;
    /** 用户状态。 */
    private final String status;

    public SysUserSaveCommand(Long id, String tenantId, String username, String nickname, String password,
                              String status) {
        this.id = id;
        this.tenantId = tenantId;
        this.username = username;
        this.nickname = nickname;
        this.password = password;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public String getTenantId() {
        return tenantId;
    }

    public String getUsername() {
        return username;
    }

    public String getNickname() {
        return nickname;
    }

    public String getPassword() {
        return password;
    }

    public String getStatus() {
        return status;
    }
}
