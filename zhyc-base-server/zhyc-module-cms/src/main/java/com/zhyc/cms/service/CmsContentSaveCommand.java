/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.cms.service;

/**
 * 内容文章保存命令。
 *
 * @param id 内容文章主键，存在时执行租户内更新
 * @param tenantId 租户业务编码
 * @param channelCode 栏目编码
 * @param title 文章标题
 * @param summary 文章摘要
 * @param bodyContent 文章正文
 * @param status 文章状态
 * @param authorId 作者用户主键
 */
public record CmsContentSaveCommand(Long id, String tenantId, String channelCode, String title,
                                    String summary, String bodyContent, String status,
                                    Long authorId) {
}
