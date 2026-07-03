/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.file.object.service;

/**
 * 文件对象查询条件。
 *
 * @param tenantId 租户业务编码
 * @param keyword 文件名关键词
 * @param pageNo 当前页码
 * @param pageSize 每页记录数
 */
public record FileObjectQuery(String tenantId, String keyword, int pageNo, int pageSize) {
}
