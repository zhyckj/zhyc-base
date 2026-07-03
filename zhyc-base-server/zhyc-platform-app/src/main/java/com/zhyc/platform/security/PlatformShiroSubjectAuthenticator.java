/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.platform.security;

import org.apache.shiro.SecurityUtils;

/**
 * 平台 Shiro Subject 登录适配器。
 *
 * <p>封装 {@link SecurityUtils} 静态调用，便于认证中心 Bearer 过滤器在测试中替换登录行为。</p>
 */
public class PlatformShiroSubjectAuthenticator {

    /**
     * 使用认证中心 Claims Token 登录当前 Shiro Subject。
     *
     * @param token 认证中心 Claims 认证令牌
     */
    public void authenticate(PlatformAuthCenterAuthenticationToken token) {
        SecurityUtils.getSubject().login(token);
    }
}
