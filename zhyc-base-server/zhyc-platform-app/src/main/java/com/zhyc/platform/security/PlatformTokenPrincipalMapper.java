/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.platform.security;

import java.util.Map;

/**
 * 认证中心令牌主体映射器。
 *
 * <p>负责把认证中心 JWT/OIDC Claims 转换为核心平台 Shiro 主体，避免核心平台直接依赖认证中心内部实现。</p>
 */
public class PlatformTokenPrincipalMapper {

    /** 租户业务编码 Claims 名称，用于建立平台租户授权上下文。 */
    private static final String CLAIM_TENANT_ID = "tenant_id";
    /** 用户主键 Claims 名称，用于映射平台本地用户权限。 */
    private static final String CLAIM_USER_ID = "user_id";
    /** 首选登录账号 Claims 名称，优先作为平台登录账号。 */
    private static final String CLAIM_PREFERRED_USERNAME = "preferred_username";
    /** OIDC 主体 Claims 名称，作为登录账号兜底来源。 */
    private static final String CLAIM_SUBJECT = "sub";
    /** 用户显示名称 Claims 名称，用于平台审计和后台展示。 */
    private static final String CLAIM_NAME = "name";

    /**
     * 映射认证中心 Claims 为平台 Shiro 主体。
     *
     * <p>租户编码、用户主键和账号是进入平台授权上下文的最小字段，缺失或格式异常时直接拒绝。</p>
     *
     * @param claims 认证中心签发令牌解析后的 Claims
     * @return 平台后台用户 Shiro 主体
     */
    public PlatformUserPrincipal mapClaims(Map<String, Object> claims) {
        if (claims == null) {
            throw new IllegalArgumentException("认证中心令牌 Claims 不能为空");
        }
        String tenantId = requireText(claims.get(CLAIM_TENANT_ID), "认证中心令牌缺少租户业务编码");
        Long userId = requireUserId(claims.get(CLAIM_USER_ID));
        String username = firstText(claims.get(CLAIM_PREFERRED_USERNAME), claims.get(CLAIM_SUBJECT));
        if (username == null) {
            throw new IllegalArgumentException("认证中心令牌缺少登录账号");
        }
        String nickname = firstText(claims.get(CLAIM_NAME), username);
        return new PlatformUserPrincipal(userId, tenantId, username, nickname);
    }

    /**
     * 提取必填文本字段。
     *
     * @param value Claims 原始字段值
     * @param message 字段缺失时返回的业务错误信息
     * @return 去除首尾空白后的文本值
     */
    private String requireText(Object value, String message) {
        String text = textValue(value);
        if (text == null) {
            throw new IllegalArgumentException(message);
        }
        return text;
    }

    /**
     * 提取平台用户主键。
     *
     * @param value Claims 中的用户主键字段值
     * @return 平台用户主键
     */
    private Long requireUserId(Object value) {
        if (value == null) {
            throw new IllegalArgumentException("认证中心令牌缺少用户主键");
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        String text = textValue(value);
        if (text == null) {
            throw new IllegalArgumentException("认证中心令牌缺少用户主键");
        }
        try {
            return Long.parseLong(text);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("认证中心令牌用户主键格式不正确", exception);
        }
    }

    /**
     * 返回首个有效文本字段。
     *
     * @param values 候选 Claims 字段值
     * @return 首个非空白文本，不存在时返回 null
     */
    private String firstText(Object... values) {
        for (Object value : values) {
            String text = textValue(value);
            if (text != null) {
                return text;
            }
        }
        return null;
    }

    /**
     * 将 Claims 字段转换为去除首尾空白的文本。
     *
     * @param value Claims 字段原始值
     * @return 非空白文本，不存在时返回 null
     */
    private String textValue(Object value) {
        if (value == null) {
            return null;
        }
        String text = value.toString().trim();
        return text.isEmpty() ? null : text;
    }
}
