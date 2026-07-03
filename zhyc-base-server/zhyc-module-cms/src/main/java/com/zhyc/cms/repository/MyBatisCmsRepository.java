/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.cms.repository;

import com.zhyc.cms.domain.CmsChannel;
import com.zhyc.cms.domain.CmsContent;
import com.zhyc.cms.mapper.CmsMapper;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Repository;

/**
 * 基于 MyBatis 的内容管理仓储实现。
 */
@Repository
public class MyBatisCmsRepository implements CmsRepository {

  /** 内容管理 Mapper。 */
  private final CmsMapper cmsMapper;

  /**
   * 创建内容管理仓储实现。
   *
   * @param cmsMapper 内容管理 Mapper
   */
  public MyBatisCmsRepository(CmsMapper cmsMapper) {
    this.cmsMapper = Objects.requireNonNull(cmsMapper, "内容管理 Mapper 不能为空");
  }

  @Override
  public List<CmsChannel> findChannels(String tenantId, String status) {
    return cmsMapper.selectChannels(tenantId, status);
  }

  @Override
  public void saveChannel(CmsChannel channel) {
    cmsMapper.upsertChannel(channel);
  }

  @Override
  public List<CmsContent> findContents(String tenantId, String channelCode, String status) {
    return cmsMapper.selectContents(tenantId, channelCode, status);
  }

  @Override
  public void saveContent(CmsContent content) {
    if (content.getId() == null) {
      cmsMapper.insertContent(content);
      return;
    }
    cmsMapper.updateContent(content);
  }

  @Override
  public void updateContentStatus(String tenantId, Long id, String status) {
    cmsMapper.updateContentStatus(tenantId, id, status);
  }
}
