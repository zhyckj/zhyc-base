/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.message.template;

import com.zhyc.message.template.domain.MsgTemplate;
import com.zhyc.message.template.repository.MsgTemplateRepository;
import com.zhyc.message.template.service.DefaultMsgTemplateService;
import com.zhyc.message.template.service.MsgTemplateResponse;
import com.zhyc.message.template.service.MsgTemplateSaveCommand;
import com.zhyc.message.template.service.MsgTemplateService;
import com.zhyc.common.exception.BusinessException;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * 消息模板业务服务测试。
 */
class MsgTemplateServiceTest {

  /**
   * 验证消息模板按租户查询，防止跨租户返回模板。
   */
  @Test
  void shouldListTemplatesByTenant() {
    RecordingTemplateRepository repository = new RecordingTemplateRepository();
    MsgTemplateService service = new DefaultMsgTemplateService(repository);

    List<MsgTemplateResponse> responses = service.listTemplates(" tenant_a ");

    assertEquals("tenant_a", repository.lastTenantId);
    assertEquals(1, responses.size());
    assertEquals("purchase.approved", responses.get(0).templateCode());
  }

  /**
   * 验证消息模板保存会裁剪核心字段并默认启用模板。
   */
  @Test
  void shouldSaveTemplateWithNormalizedFields() {
    RecordingTemplateRepository repository = new RecordingTemplateRepository();
    MsgTemplateService service = new DefaultMsgTemplateService(repository);

    service.save(new MsgTemplateSaveCommand(" tenant_a ", " purchase.approved ", " 审批通过 ",
        " in_app ", " 标题 ", " 内容 ", null));

    assertEquals("tenant_a", repository.lastSaved.getTenantId());
    assertEquals("purchase.approved", repository.lastSaved.getTemplateCode());
    assertEquals("审批通过", repository.lastSaved.getTemplateName());
    assertEquals("in_app", repository.lastSaved.getChannelType());
    assertEquals("enabled", repository.lastSaved.getStatus());
  }

  /**
   * 验证消息模板拒绝非法通道和状态，避免不可投递配置进入模板库。
   */
  @Test
  void shouldRejectInvalidTemplateChannelAndStatus() {
    MsgTemplateService service = new DefaultMsgTemplateService(new RecordingTemplateRepository());

    BusinessException channelException = assertThrows(BusinessException.class,
        () -> service.save(new MsgTemplateSaveCommand("tenant_a", "purchase.approved", "审批通过",
            "webhook", "标题", "内容", "enabled")));
    BusinessException statusException = assertThrows(BusinessException.class,
        () -> service.save(new MsgTemplateSaveCommand("tenant_a", "purchase.approved", "审批通过",
            "in_app", "标题", "内容", "archived")));

    assertEquals("ZHYC_MESSAGE_TEMPLATE_CHANNEL_UNSUPPORTED", channelException.getCode());
    assertEquals("消息通道类型不支持: webhook", channelException.getMessage());
    assertEquals("ZHYC_MESSAGE_TEMPLATE_STATUS_UNSUPPORTED", statusException.getCode());
    assertEquals("模板状态不支持: archived", statusException.getMessage());
  }

  /**
   * 测试用消息模板仓储。
   */
  private static class RecordingTemplateRepository implements MsgTemplateRepository {

    /** 最近一次查询的租户业务编码。 */
    private String lastTenantId;
    /** 最近一次保存的消息模板。 */
    private MsgTemplate lastSaved;

    @Override
    public List<MsgTemplate> findByTenantId(String tenantId) {
      lastTenantId = tenantId;
      return List.of(new MsgTemplate(1L, tenantId, "purchase.approved", "审批通过", "in_app",
          "审批通过", "你的采购申请已审批通过", "enabled", LocalDateTime.now(), LocalDateTime.now()));
    }

    @Override
    public MsgTemplate findEnabledByTenantIdAndTemplateCode(String tenantId, String templateCode) {
      return null;
    }

    @Override
    public void save(MsgTemplate template) {
      lastSaved = template;
    }
  }
}
