/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.accessrestriction.mapper;

import com.zhyc.system.accessrestriction.domain.SysAccessRestriction;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.SelectProvider;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 系统访问限制 MyBatis Mapper。
 */
@Mapper
public interface SysAccessRestrictionMapper {

    /**
     * 查询当前生效的访问限制。
     *
     * @param tenantId 租户业务编码
     * @param restrictionType 限制类型
     * @param now 当前时间
     * @return 当前生效的访问限制列表
     */
    @SelectProvider(type = SysAccessRestrictionSqlProvider.class, method = "selectActiveRestrictions")
    List<SysAccessRestriction> selectActiveRestrictions(@Param("tenantId") String tenantId,
                                                        @Param("restrictionType") String restrictionType,
                                                        @Param("now") LocalDateTime now);

    /**
     * 保存或更新访问限制。
     *
     * @param restriction 系统访问限制
     */
    @InsertProvider(type = SysAccessRestrictionSqlProvider.class, method = "saveRestriction")
    void saveRestriction(SysAccessRestriction restriction);
}
