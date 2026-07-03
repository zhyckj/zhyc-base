/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.param.service;

import java.util.List;
import java.util.Optional;

/**
 * 系统参数业务服务。
 */
public interface SysParamService {

    /**
     * 查询租户系统参数列表。
     *
     * @param tenantId 租户业务编码
     * @return 系统参数列表
     */
    List<SysParamResponse> listParams(String tenantId);

    /**
     * 按参数键查询租户系统参数。
     *
     * @param tenantId 租户业务编码
     * @param paramKey 参数键
     * @return 系统参数，不存在时为空
     */
    Optional<SysParamResponse> findByKey(String tenantId, String paramKey);

    /**
     * 保存或更新租户系统参数。
     *
     * @param command 系统参数保存命令
     */
    void save(SysParamSaveCommand command);
}
