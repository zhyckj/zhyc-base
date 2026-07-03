/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.secret.mapper;

import com.zhyc.system.secret.domain.SysSecret;
import org.apache.ibatis.annotations.DeleteProvider;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.annotations.UpdateProvider;

import java.util.List;

/**
 * 系统密钥 MyBatis Mapper。
 */
@Mapper
public interface SysSecretMapper {

    /**
     * 查询当前租户的密钥列表。
     *
     * @param tenantId 租户业务编码
     * @return 密钥列表
     */
    @SelectProvider(type = SysSecretSqlProvider.class, method = "selectByTenantId")
    List<SysSecret> selectByTenantId(@Param("tenantId") String tenantId);

    /**
     * 按租户和主键查询密钥。
     *
     * @param tenantId 租户业务编码
     * @param id 密钥主键
     * @return 密钥列表
     */
    @SelectProvider(type = SysSecretSqlProvider.class, method = "selectByTenantIdAndId")
    List<SysSecret> selectByTenantIdAndId(@Param("tenantId") String tenantId, @Param("id") Long id);

    /**
     * 按租户和密钥编码查询密钥。
     *
     * @param tenantId 租户业务编码
     * @param secretCode 密钥编码
     * @return 密钥列表
     */
    @SelectProvider(type = SysSecretSqlProvider.class, method = "selectByTenantIdAndSecretCode")
    List<SysSecret> selectByTenantIdAndSecretCode(@Param("tenantId") String tenantId,
                                                  @Param("secretCode") String secretCode);

    /**
     * 查询当前租户可用于下拉选择的密钥。
     *
     * @param tenantId 租户业务编码
     * @param secretKind 密钥类型，可为空；为空时返回数据源兼容类型
     * @param status 密钥状态
     * @return 密钥列表
     */
    @SelectProvider(type = SysSecretSqlProvider.class, method = "selectSelectableSecrets")
    List<SysSecret> selectSelectableSecrets(@Param("tenantId") String tenantId,
                                            @Param("secretKind") String secretKind,
                                            @Param("status") String status);

    /**
     * 新增系统密钥。
     *
     * @param secret 系统密钥
     */
    @InsertProvider(type = SysSecretSqlProvider.class, method = "insert")
    void insert(SysSecret secret);

    /**
     * 更新系统密钥。
     *
     * @param secret 系统密钥
     */
    @UpdateProvider(type = SysSecretSqlProvider.class, method = "update")
    void update(SysSecret secret);

    /**
     * 删除当前租户的系统密钥。
     *
     * @param tenantId 租户业务编码
     * @param id 密钥主键
     */
    @DeleteProvider(type = SysSecretSqlProvider.class, method = "deleteByTenantIdAndId")
    void deleteByTenantIdAndId(@Param("tenantId") String tenantId, @Param("id") Long id);
}
