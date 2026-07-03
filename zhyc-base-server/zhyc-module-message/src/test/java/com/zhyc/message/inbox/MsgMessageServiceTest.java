/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.message.inbox;

import com.zhyc.common.api.PageResult;
import com.zhyc.common.exception.BusinessException;
import com.zhyc.message.inbox.domain.MsgMessage;
import com.zhyc.message.inbox.repository.MsgMessageRepository;
import com.zhyc.message.inbox.service.DefaultMsgMessageService;
import com.zhyc.message.inbox.service.MsgMessageQuery;
import com.zhyc.message.inbox.service.MsgMessageResponse;
import com.zhyc.message.inbox.service.MsgMessageSendCommand;
import com.zhyc.message.inbox.service.MsgMessageService;
import com.zhyc.message.inbox.service.MsgMessageTemplateSendCommand;
import com.zhyc.message.template.domain.MsgTemplate;
import com.zhyc.message.template.repository.MsgTemplateRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 站内消息业务服务测试。
 */
class MsgMessageServiceTest {

  /**
   * 验证发送站内消息会写入租户、接收人和未读状态。
   */
  @Test
  void shouldSendMessage() {
    RecordingMessageRepository repository = new RecordingMessageRepository();
    MsgMessageService service = new DefaultMsgMessageService(repository);

    String messageCode = service.send(new MsgMessageSendCommand(" tenant_a ", 1001L,
        "张三", " approval ", " 审批提醒 ", " 请处理采购申请 "));

    assertTrue(messageCode.startsWith("MSG"));
    assertEquals("tenant_a", repository.lastSaved.getTenantId());
    assertEquals(1001L, repository.lastSaved.getReceiverId());
    assertEquals("approval", repository.lastSaved.getMessageType());
    assertFalse(repository.lastSaved.isReadFlag());
  }

  /**
   * 验证发送站内消息拒绝未知消息类型，避免消息分类被任意字符串污染。
   */
  @Test
  void shouldRejectInvalidMessageType() {
    MsgMessageService service = new DefaultMsgMessageService(new RecordingMessageRepository());

    BusinessException exception = assertThrows(BusinessException.class,
        () -> service.send(new MsgMessageSendCommand("tenant_a", 1001L, "张三",
            "marketing", "营销消息", "无效消息类型")));

    assertEquals("ZHYC_MESSAGE_INBOX_TYPE_UNSUPPORTED", exception.getCode());
    assertEquals("消息类型不支持: marketing", exception.getMessage());
  }

  /**
   * 验证站内消息支持后台管理端流程消息类型。
   */
  @Test
  void shouldSendWorkflowMessageType() {
    RecordingMessageRepository repository = new RecordingMessageRepository();
    MsgMessageService service = new DefaultMsgMessageService(repository);

    service.send(new MsgMessageSendCommand("tenant_a", 1001L, "张三",
        "workflow", "流程提醒", "请处理流程任务"));

    assertEquals("workflow", repository.lastSaved.getMessageType());
  }

  /**
   * 验证按启用模板发送站内消息时会渲染标题和内容变量，避免调用方重复拼装消息正文。
   */
  @Test
  void shouldSendMessageByEnabledTemplate() {
    RecordingMessageRepository messageRepository = new RecordingMessageRepository();
    RecordingTemplateRepository templateRepository = new RecordingTemplateRepository();
    templateRepository.templates = List.of(new MsgTemplate(1L, "tenant_a", "purchase-approved",
        "采购审批通过", "in_app", "采购申请 ${requestNo} 已通过",
        "申请人 ${applicantName}，金额 ${amount} 元", "enabled", null, null));
    MsgMessageService service = new DefaultMsgMessageService(messageRepository, templateRepository);

    String messageCode = service.sendByTemplate(new MsgMessageTemplateSendCommand(" tenant_a ",
        " purchase-approved ", 1002L, "李四", "approval", Map.of(
            "requestNo", "PR202606260001",
            "applicantName", "张三",
            "amount", "1280.50"
        )));

    assertTrue(messageCode.startsWith("MSG"));
    assertEquals("tenant_a", templateRepository.lastTenantId);
    assertEquals("purchase-approved", templateRepository.lastTemplateCode);
    assertEquals("采购申请 PR202606260001 已通过", messageRepository.lastSaved.getTitle());
    assertEquals("申请人 张三，金额 1280.50 元", messageRepository.lastSaved.getContent());
    assertEquals(1002L, messageRepository.lastSaved.getReceiverId());
  }

  /**
   * 验证按模板发送时必须命中启用模板。
   */
  @Test
  void shouldRejectDisabledTemplateWhenSendingByTemplate() {
    RecordingTemplateRepository templateRepository = new RecordingTemplateRepository();
    templateRepository.templates = List.of(new MsgTemplate(1L, "tenant_a", "purchase-approved",
        "采购审批通过", "in_app", "标题", "内容", "disabled", null, null));
    MsgMessageService service = new DefaultMsgMessageService(new RecordingMessageRepository(), templateRepository);

    BusinessException exception = assertThrows(BusinessException.class,
        () -> service.sendByTemplate(new MsgMessageTemplateSendCommand("tenant_a",
            "purchase-approved", 1002L, "李四", "approval", Map.of())));

    assertEquals("ZHYC_MESSAGE_TEMPLATE_NOT_FOUND", exception.getCode());
    assertEquals("消息模板不存在或未启用", exception.getMessage());
  }

  /**
   * 验证分页查询会裁剪租户并限制页大小。
   */
  @Test
  void shouldListMessagesByReceiver() {
    RecordingMessageRepository repository = new RecordingMessageRepository();
    MsgMessageService service = new DefaultMsgMessageService(repository);

    PageResult<MsgMessageResponse> page = service.listMessages(new MsgMessageQuery(" tenant_a ", 1001L,
        false, 0, 200));

    assertEquals("tenant_a", repository.lastQuery.tenantId());
    assertEquals(1001L, repository.lastQuery.receiverId());
    assertEquals(1, repository.lastQuery.pageNo());
    assertEquals(100, repository.lastQuery.pageSize());
    assertEquals(1, page.getTotal());
  }

  /**
   * 验证标记已读必须命中当前租户和接收人消息。
   */
  @Test
  void shouldRejectMissingMessageWhenMarkRead() {
    RecordingMessageRepository repository = new RecordingMessageRepository();
    repository.markReadResult = 0;
    MsgMessageService service = new DefaultMsgMessageService(repository);

    BusinessException exception = assertThrows(BusinessException.class,
        () -> service.markRead("tenant_a", "MSG001", 1001L));

    assertEquals("ZHYC_MESSAGE_INBOX_NOT_FOUND", exception.getCode());
    assertEquals("消息不存在或无权读取", exception.getMessage());
  }

  /**
   * 测试用站内消息仓储。
   */
  private static class RecordingMessageRepository implements MsgMessageRepository {

    /** 最近一次保存的站内消息。 */
    private MsgMessage lastSaved;
    /** 最近一次查询条件。 */
    private MsgMessageQuery lastQuery;
    /** 标记已读返回值。 */
    private int markReadResult = 1;

    @Override
    public long countByQuery(MsgMessageQuery query) {
      lastQuery = query;
      return 1;
    }

    @Override
    public List<MsgMessage> findPageByQuery(MsgMessageQuery query, int offset) {
      lastQuery = query;
      return List.of(new MsgMessage(1L, query.tenantId(), "MSG001", query.receiverId(), "张三",
          "approval", "审批提醒", "请处理采购申请", false, null, LocalDateTime.now()));
    }

    @Override
    public void save(MsgMessage message) {
      lastSaved = message;
    }

    @Override
    public int markRead(String tenantId, String messageCode, Long receiverId) {
      return markReadResult;
    }
  }

  /**
   * 测试用消息模板仓储。
   */
  private static class RecordingTemplateRepository implements MsgTemplateRepository {

    /** 当前测试模板集合。 */
    private List<MsgTemplate> templates = List.of();
    /** 最近一次租户业务编码。 */
    private String lastTenantId;
    /** 最近一次模板编码。 */
    private String lastTemplateCode;

    @Override
    public List<MsgTemplate> findByTenantId(String tenantId) {
      lastTenantId = tenantId;
      return templates;
    }

    @Override
    public MsgTemplate findEnabledByTenantIdAndTemplateCode(String tenantId, String templateCode) {
      lastTenantId = tenantId;
      lastTemplateCode = templateCode;
      return templates.stream()
          .filter(template -> tenantId.equals(template.getTenantId()))
          .filter(template -> templateCode.equals(template.getTemplateCode()))
          .filter(template -> "enabled".equals(template.getStatus()))
          .findFirst()
          .orElse(null);
    }

    @Override
    public void save(MsgTemplate template) {
    }
  }
}
