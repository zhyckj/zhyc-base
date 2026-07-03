/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.cms.service;

/**
 * 内容栏目保存命令。
 *
 * @param tenantId 租户业务编码
 * @param channelCode 栏目编码
 * @param channelName 栏目名称
 * @param parentId 父栏目主键
 * @param sortOrder 排序号
 * @param status 栏目状态
 */
public record CmsChannelSaveCommand(String tenantId, String channelCode, String channelName,
                                    Long parentId, Integer sortOrder, String status) {
}
