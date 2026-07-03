/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.cms;

import com.zhyc.cms.domain.CmsChannel;
import com.zhyc.cms.domain.CmsContent;
import com.zhyc.cms.repository.CmsRepository;
import com.zhyc.cms.service.CmsChannelResponse;
import com.zhyc.cms.service.CmsChannelSaveCommand;
import com.zhyc.cms.service.CmsContentResponse;
import com.zhyc.cms.service.CmsContentSaveCommand;
import com.zhyc.cms.service.CmsService;
import com.zhyc.cms.service.DefaultCmsService;
import com.zhyc.common.exception.BusinessException;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * 内容管理业务服务测试。
 */
class CmsServiceTest {

  /**
   * 验证保存内容栏目会裁剪租户、栏目编码和栏目名称。
   */
  @Test
  void shouldSaveChannelWithNormalizedFields() {
    RecordingRepository repository = new RecordingRepository();
    CmsService service = new DefaultCmsService(repository);

    service.saveChannel(new CmsChannelSaveCommand(" tenant_a ", " news ", " 新闻资讯 ", 1L,
        10, "enabled"));

    assertEquals("tenant_a", repository.lastChannel.getTenantId());
    assertEquals("news", repository.lastChannel.getChannelCode());
    assertEquals("新闻资讯", repository.lastChannel.getChannelName());
    assertEquals(1L, repository.lastChannel.getParentId());
  }

  /**
   * 验证保存内容文章会校验标题和栏目编码。
   */
  @Test
  void shouldRejectContentWithoutTitle() {
    CmsService service = new DefaultCmsService(new RecordingRepository());

    BusinessException exception = assertThrows(BusinessException.class,
        () -> service.saveContent(new CmsContentSaveCommand(null, "tenant_a", "news", " ",
            "摘要", "正文", "draft", 1001L)));

    assertEquals("ZHYC_CMS_CONTENT_TITLE_REQUIRED", exception.getCode());
    assertEquals("文章标题不能为空", exception.getMessage());
  }

  /**
   * 验证保存内容文章会裁剪基础字段并默认草稿状态。
   */
  @Test
  void shouldSaveContentWithDraftStatus() {
    RecordingRepository repository = new RecordingRepository();
    CmsService service = new DefaultCmsService(repository);

    service.saveContent(new CmsContentSaveCommand(null, " tenant_a ", " news ", " 平台上线 ",
        " 首期平台上线 ", " 正文内容 ", null, 1001L));

    assertEquals("tenant_a", repository.lastContent.getTenantId());
    assertEquals("news", repository.lastContent.getChannelCode());
    assertEquals("平台上线", repository.lastContent.getTitle());
    assertEquals("draft", repository.lastContent.getStatus());
  }

  /**
   * 验证保存已有内容文章会保留文章主键，供仓储执行租户内更新。
   */
  @Test
  void shouldKeepContentIdWhenSavingExistingContent() {
    RecordingRepository repository = new RecordingRepository();
    CmsService service = new DefaultCmsService(repository);

    service.saveContent(new CmsContentSaveCommand(1001L, " tenant_a ", " news ", " 平台上线更新 ",
        " 摘要更新 ", " 正文更新 ", "published", 2002L));

    assertEquals(1001L, repository.lastContent.getId());
    assertEquals("tenant_a", repository.lastContent.getTenantId());
    assertEquals("news", repository.lastContent.getChannelCode());
    assertEquals("平台上线更新", repository.lastContent.getTitle());
    assertEquals("published", repository.lastContent.getStatus());
  }

  /**
   * 验证内容管理拒绝非法栏目状态和文章状态，避免跨端字典出现脏值。
   */
  @Test
  void shouldRejectInvalidChannelAndContentStatus() {
    CmsService service = new DefaultCmsService(new RecordingRepository());

    BusinessException channelException = assertThrows(BusinessException.class,
        () -> service.saveChannel(new CmsChannelSaveCommand("tenant_a", "news", "新闻资讯",
            null, 0, "archived")));
    BusinessException contentException = assertThrows(BusinessException.class,
        () -> service.saveContent(new CmsContentSaveCommand(null, "tenant_a", "news", "平台上线",
            "摘要", "正文", "enabled", 1001L)));
    BusinessException updateException = assertThrows(BusinessException.class,
        () -> service.updateContentStatus("tenant_a", 1L, "deleted"));

    assertEquals("ZHYC_CMS_CHANNEL_STATUS_UNSUPPORTED", channelException.getCode());
    assertEquals("栏目状态不支持: archived", channelException.getMessage());
    assertEquals("ZHYC_CMS_CONTENT_STATUS_UNSUPPORTED", contentException.getCode());
    assertEquals("文章状态不支持: enabled", contentException.getMessage());
    assertEquals("ZHYC_CMS_CONTENT_STATUS_UNSUPPORTED", updateException.getCode());
    assertEquals("文章状态不支持: deleted", updateException.getMessage());
  }

  /**
   * 验证查询栏目和文章会按租户隔离。
   */
  @Test
  void shouldListChannelsAndContentsByTenant() {
    RecordingRepository repository = new RecordingRepository();
    CmsService service = new DefaultCmsService(repository);

    List<CmsChannelResponse> channels = service.listChannels(" tenant_a ", " enabled ");
    List<CmsContentResponse> contents = service.listContents(" tenant_a ", " news ", " published ");

    assertEquals("tenant_a", repository.lastTenantId);
    assertEquals("enabled", repository.lastChannelStatus);
    assertEquals("news", repository.lastChannelCode);
    assertEquals("published", repository.lastContentStatus);
    assertEquals("news", channels.getFirst().channelCode());
    assertEquals("平台上线", contents.getFirst().title());
  }

  /**
   * 测试用内容管理仓储。
   */
  private static class RecordingRepository implements CmsRepository {

    /** 最近一次保存的栏目。 */
    private CmsChannel lastChannel;
    /** 最近一次保存的文章。 */
    private CmsContent lastContent;
    /** 最近一次查询租户。 */
    private String lastTenantId;
    /** 最近一次栏目状态。 */
    private String lastChannelStatus;
    /** 最近一次栏目编码。 */
    private String lastChannelCode;
    /** 最近一次文章状态。 */
    private String lastContentStatus;

    @Override
    public List<CmsChannel> findChannels(String tenantId, String status) {
      lastTenantId = tenantId;
      lastChannelStatus = status;
      return List.of(new CmsChannel(1L, tenantId, null, "news", "新闻资讯", 10,
          "enabled", LocalDateTime.now(), LocalDateTime.now()));
    }

    @Override
    public void saveChannel(CmsChannel channel) {
      lastChannel = channel;
    }

    @Override
    public List<CmsContent> findContents(String tenantId, String channelCode, String status) {
      lastTenantId = tenantId;
      lastChannelCode = channelCode;
      lastContentStatus = status;
      return List.of(new CmsContent(1L, tenantId, channelCode, "平台上线", "首期平台上线",
          "正文内容", status, 1001L, LocalDateTime.now(), LocalDateTime.now()));
    }

    @Override
    public void saveContent(CmsContent content) {
      lastContent = content;
    }

    @Override
    public void updateContentStatus(String tenantId, Long id, String status) {
      lastTenantId = tenantId;
      lastContentStatus = status;
    }
  }
}
