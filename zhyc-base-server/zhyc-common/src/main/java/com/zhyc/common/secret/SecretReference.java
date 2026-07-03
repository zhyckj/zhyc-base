/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.common.secret;

import com.zhyc.common.util.TextHelper;

/**
 * 密钥引用对象。
 *
 * <p>统一承载低代码、系统模块和开放 API 使用的密钥引用格式，当前仅允许
 * {@code secret:<secretCode>} 这一种稳定引用形式。</p>
 */
public final class SecretReference {

    /** 密钥引用前缀。 */
    public static final String PREFIX = "secret:";

    /** 密钥编码。 */
    private final String code;

    /** 规范化后的密钥引用值。 */
    private final String value;

    private SecretReference(String code) {
        this.code = code;
        this.value = PREFIX + code;
    }

    /**
     * 解析密钥引用。
     *
     * <p>仅接受 {@code secret:<secretCode>} 格式；空值、缺少前缀、包含空白字符或密钥编码为空都会直接失败。</p>
     *
     * @param value 待解析的密钥引用
     * @return 密钥引用对象
     */
    public static SecretReference parse(String value) {
        String requiredValue = TextHelper.requireNoWhitespaceText(value, "密钥引用不能为空", "密钥引用不能包含空白");
        if (!requiredValue.startsWith(PREFIX)) {
            throw new IllegalArgumentException("密钥引用必须以 secret: 开头");
        }
        String code = TextHelper.requireNoWhitespaceText(requiredValue.substring(PREFIX.length()),
                "密钥引用必须包含密钥编码", "密钥编码不能包含空白");
        return new SecretReference(code);
    }

    /**
     * 根据密钥编码构建规范化引用。
     *
     * <p>用于由配置项或数据库字段中的密钥编码反向生成稳定引用值，输出始终为
     * {@code secret:<secretCode>}。</p>
     *
     * @param code 密钥编码
     * @return 密钥引用对象
     */
    public static SecretReference ofCode(String code) {
        return new SecretReference(TextHelper.requireNoWhitespaceText(code, "密钥编码不能为空", "密钥编码不能包含空白"));
    }

    /**
     * 获取密钥编码。
     *
     * @return 密钥编码
     */
    public String getCode() {
        return code;
    }

    /**
     * 获取规范化后的密钥引用值。
     *
     * @return 规范化后的密钥引用值
     */
    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }
}
