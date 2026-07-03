/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.cms.mapper;

import com.zhyc.cms.domain.CmsChannel;
import com.zhyc.cms.domain.CmsContent;
import java.util.List;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.annotations.UpdateProvider;

/**
 * 内容管理 MyBatis Mapper。
 */
@Mapper
public interface CmsMapper {

  /**
   * 查询内容栏目列表。
   *
   * @param tenantId 租户业务编码
   * @param status 栏目状态
   * @return 内容栏目列表
   */
  @SelectProvider(type = CmsSqlProvider.class, method = "selectChannels")
  List<CmsChannel> selectChannels(@Param("tenantId") String tenantId, @Param("status") String status);

  /**
   * 保存或更新内容栏目。
   *
   * @param channel 内容栏目
   */
  @InsertProvider(type = CmsSqlProvider.class, method = "upsertChannel")
  void upsertChannel(CmsChannel channel);

  /**
   * 查询内容文章列表。
   *
   * @param tenantId 租户业务编码
   * @param channelCode 栏目编码
   * @param status 文章状态
   * @return 内容文章列表
   */
  @SelectProvider(type = CmsSqlProvider.class, method = "selectContents")
  List<CmsContent> selectContents(@Param("tenantId") String tenantId,
      @Param("channelCode") String channelCode, @Param("status") String status);

  /**
   * 保存内容文章。
   *
   * @param content 内容文章
   */
  @InsertProvider(type = CmsSqlProvider.class, method = "insertContent")
  void insertContent(CmsContent content);

  /**
   * 更新内容文章。
   *
   * @param content 内容文章
   */
  @UpdateProvider(type = CmsSqlProvider.class, method = "updateContent")
  void updateContent(CmsContent content);

  /**
   * 更新内容文章状态。
   *
   * @param tenantId 租户业务编码
   * @param id 文章主键
   * @param status 文章状态
   */
  @UpdateProvider(type = CmsSqlProvider.class, method = "updateContentStatus")
  void updateContentStatus(@Param("tenantId") String tenantId, @Param("id") Long id,
      @Param("status") String status);
}
