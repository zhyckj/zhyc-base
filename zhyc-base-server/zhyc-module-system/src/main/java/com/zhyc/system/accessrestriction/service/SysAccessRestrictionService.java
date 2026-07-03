/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.accessrestriction.service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 系统访问限制业务服务。
 */
public interface SysAccessRestrictionService {

    /**
     * 查询当前生效的访问限制列表。
     *
     * @param tenantId 租户业务编码
     * @param restrictionType 限制类型
     * @param now 当前时间
     * @return 当前生效的访问限制列表
     */
    List<SysAccessRestrictionResponse> listActiveRestrictions(String tenantId, String restrictionType,
                                                              LocalDateTime now);

    /**
     * 判定指定访问标识是否允许访问。
     *
     * @param tenantId 租户业务编码
     * @param restrictionType 限制类型
     * @param accessValue 待判定访问标识
     * @param now 当前时间
     * @return 访问限制判定结果
     */
    SysAccessRestrictionEvaluationResult evaluateAccess(String tenantId, String restrictionType,
                                                        String accessValue, LocalDateTime now);

    /**
     * 保存或更新访问限制。
     *
     * @param command 系统访问限制保存命令
     */
    void save(SysAccessRestrictionSaveCommand command);
}
