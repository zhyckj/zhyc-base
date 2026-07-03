/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.support;

import java.util.regex.Pattern;

/**
 * 开放 API OAuth2 授权范围校验器。
 *
 * <p>用于统一约束第三方应用可申请的 scope 编码格式，避免认证中心、开发者门户和开放 API 管理端出现不同校验规则。</p>
 */
public final class OpenApiOauthScopeValidator {

    /** OAuth2 授权范围编码白名单，避免逗号、斜杠等分隔符混入造成授权语义歧义。 */
    private static final Pattern SUPPORTED_SCOPE_PATTERN = Pattern.compile("[A-Za-z0-9._:-]+");

    private OpenApiOauthScopeValidator() {
    }

    /**
     * 校验单个 OAuth2 授权范围编码。
     *
     * @param scope OAuth2 授权范围编码
     */
    public static void requireSupportedScope(String scope) {
        if (scope.contains("*")) {
            throw new IllegalArgumentException("OAuth2 授权范围不能包含通配符 *");
        }
        if (!SUPPORTED_SCOPE_PATTERN.matcher(scope).matches()) {
            throw new IllegalArgumentException("OAuth2 授权范围只能包含字母、数字、点、下划线、短横线或冒号");
        }
    }
}
