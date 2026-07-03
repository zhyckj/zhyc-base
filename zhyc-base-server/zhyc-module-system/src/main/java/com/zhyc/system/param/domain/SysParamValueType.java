/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.param.domain;

/**
 * 系统参数值类型枚举。
 *
 * <p>类型编码持久化到 sys_param.value_type 和 sys_tenant_param.value_type 字段，
 * 用于指导后台展示、参数解析和后续低代码模板生成。</p>
 */
public enum SysParamValueType {

    /** 字符串参数，按原始文本保存和展示。 */
    STRING("string", "字符串"),

    /** 数值参数，保存后由业务使用方按数字解析。 */
    NUMBER("number", "数值"),

    /** 布尔参数，保存后由业务使用方按 true 或 false 解析。 */
    BOOLEAN("boolean", "布尔值"),

    /** JSON 参数，用于保存结构化配置文本。 */
    JSON("json", "JSON");

    /** 持久化类型编码；对应参数表 value_type 字段。 */
    private final String code;

    /** 类型中文说明；用于后台展示和生成模板说明。 */
    private final String description;

    /**
     * 创建系统参数值类型枚举。
     *
     * @param code 持久化类型编码
     * @param description 类型中文说明
     */
    SysParamValueType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 获取持久化类型编码。
     *
     * @return 参数表 value_type 字段使用的类型编码
     */
    public String getCode() {
        return code;
    }

    /**
     * 获取类型中文说明。
     *
     * @return 类型中文说明
     */
    public String getDescription() {
        return description;
    }

    /**
     * 根据持久化编码解析系统参数值类型。
     *
     * <p>只允许系统约定的基础类型，避免无法解析的配置类型进入运行期参数表。</p>
     *
     * @param code 持久化类型编码
     * @return 匹配的系统参数值类型
     */
    public static SysParamValueType fromCode(String code) {
        for (SysParamValueType valueType : values()) {
            if (valueType.code.equals(code)) {
                return valueType;
            }
        }
        throw new IllegalArgumentException("参数值类型不支持: " + code);
    }
}
