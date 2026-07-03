/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.passwordpolicy.mapper;

import com.zhyc.system.passwordpolicy.domain.SysPasswordPolicy;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.SelectProvider;

/**
 * 系统密码策略 MyBatis Mapper。
 */
@Mapper
public interface SysPasswordPolicyMapper {

    /**
     * 查询租户默认密码策略。
     *
     * @param tenantId 租户业务编码
     * @return 默认密码策略，不存在时为空
     */
    @SelectProvider(type = SysPasswordPolicySqlProvider.class, method = "selectDefaultByTenantId")
    SysPasswordPolicy selectDefaultByTenantId(@Param("tenantId") String tenantId);

    /**
     * 保存或更新密码策略。
     *
     * @param policy 系统密码策略
     */
    @InsertProvider(type = SysPasswordPolicySqlProvider.class, method = "upsert")
    void upsert(SysPasswordPolicy policy);
}
