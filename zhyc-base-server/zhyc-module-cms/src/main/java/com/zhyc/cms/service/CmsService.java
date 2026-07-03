/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.cms.service;

import java.util.List;

/**
 * 内容管理业务服务。
 */
public interface CmsService {

  /**
   * 查询内容栏目列表。
   *
   * @param tenantId 租户业务编码
   * @param status 栏目状态
   * @return 内容栏目列表
   */
  List<CmsChannelResponse> listChannels(String tenantId, String status);

  /**
   * 保存内容栏目。
   *
   * @param command 内容栏目保存命令
   */
  void saveChannel(CmsChannelSaveCommand command);

  /**
   * 查询内容文章列表。
   *
   * @param tenantId 租户业务编码
   * @param channelCode 栏目编码
   * @param status 文章状态
   * @return 内容文章列表
   */
  List<CmsContentResponse> listContents(String tenantId, String channelCode, String status);

  /**
   * 保存内容文章。
   *
   * @param command 内容文章保存命令
   */
  void saveContent(CmsContentSaveCommand command);

  /**
   * 更新内容文章状态。
   *
   * @param tenantId 租户业务编码
   * @param id 文章主键
   * @param status 文章状态
   */
  void updateContentStatus(String tenantId, Long id, String status);
}
