/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.coderule.mapper;

import com.zhyc.system.coderule.domain.SysCodeRule;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.annotations.UpdateProvider;

import java.util.List;

/**
 * 系统编码规则 MyBatis Mapper。
 */
@Mapper
public interface SysCodeRuleMapper {

    /**
     * 查询租户编码规则列表。
     *
     * @param tenantId 租户业务编码
     * @return 编码规则列表
     */
    @SelectProvider(type = SysCodeRuleSqlProvider.class, method = "selectByTenantId")
    List<SysCodeRule> selectByTenantId(@Param("tenantId") String tenantId);

    /**
     * 按编码规则编码查询租户编码规则。
     *
     * @param tenantId 租户业务编码
     * @param ruleCode 编码规则编码
     * @return 编码规则，不存在时为空
     */
    @SelectProvider(type = SysCodeRuleSqlProvider.class, method = "selectByTenantIdAndRuleCode")
    SysCodeRule selectByTenantIdAndRuleCode(@Param("tenantId") String tenantId,
                                            @Param("ruleCode") String ruleCode);

    /**
     * 保存或更新编码规则。
     *
     * @param rule 系统编码规则
     */
    @InsertProvider(type = SysCodeRuleSqlProvider.class, method = "upsert")
    void upsert(SysCodeRule rule);

    /**
     * 更新编码规则当前序列值。
     *
     * @param tenantId 租户业务编码
     * @param ruleCode 编码规则编码
     * @param currentValue 当前序列值
     */
    @UpdateProvider(type = SysCodeRuleSqlProvider.class, method = "updateCurrentValue")
    void updateCurrentValue(@Param("tenantId") String tenantId, @Param("ruleCode") String ruleCode,
                            @Param("currentValue") Integer currentValue);
}
