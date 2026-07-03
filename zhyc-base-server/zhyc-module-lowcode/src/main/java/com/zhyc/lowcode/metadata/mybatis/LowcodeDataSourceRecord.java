/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.lowcode.metadata.mybatis;

/**
 * 低代码数据源持久化记录。
 *
 * <p>该记录用于 MyBatis 参数绑定和结果映射，只保存密码密钥引用，不保存数据库明文密码。</p>
 *
 * @param id 数据库主键
 * @param tenantId 租户业务编码
 * @param code 数据源编码，租户内唯一
 * @param name 数据源名称
 * @param dialect 数据库方言编码
 * @param jdbcUrl JDBC 连接地址
 * @param username 数据库登录用户名
 * @param passwordSecretRef 密码密钥引用，由密钥组件托管真实密文
 * @param enabled 数据源是否启用
 */
public record LowcodeDataSourceRecord(
    Long id,
    String tenantId,
    String code,
    String name,
    String dialect,
    String jdbcUrl,
    String username,
    String passwordSecretRef,
    Boolean enabled
) {
}
