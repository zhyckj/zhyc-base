/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.i18n;

import com.zhyc.i18n.domain.I18nMessage;
import com.zhyc.i18n.repository.I18nRepository;
import com.zhyc.i18n.service.DefaultI18nService;
import com.zhyc.i18n.service.I18nMessageResponse;
import com.zhyc.i18n.service.I18nMessageSaveCommand;
import com.zhyc.i18n.service.I18nResolveCommand;
import com.zhyc.i18n.service.I18nResolveResponse;
import com.zhyc.i18n.service.I18nService;
import com.zhyc.common.exception.BusinessException;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * 国际化词条业务服务测试。
 */
class I18nServiceTest {

  /**
   * 验证词条保存会裁剪租户、语言、词条键和值，并默认启用状态。
   */
  @Test
  void shouldSaveMessageWithNormalizedFields() {
    RecordingRepository repository = new RecordingRepository();
    I18nService service = new DefaultI18nService(repository);

    service.saveMessage(new I18nMessageSaveCommand(" tenant_a ", " zh-CN ", " menu.system ",
        " 系统管理 ", null));

    assertEquals("tenant_a", repository.lastSaved.getTenantId());
    assertEquals("zh-CN", repository.lastSaved.getLocale());
    assertEquals("menu.system", repository.lastSaved.getMessageKey());
    assertEquals("系统管理", repository.lastSaved.getMessageValue());
    assertEquals("enabled", repository.lastSaved.getStatus());
  }

  /**
   * 验证国际化词条拒绝非法状态，避免后台和移动端读取到不可识别的词条状态。
   */
  @Test
  void shouldRejectInvalidMessageStatus() {
    I18nService service = new DefaultI18nService(new RecordingRepository());

    BusinessException exception = assertThrows(BusinessException.class,
        () -> service.saveMessage(new I18nMessageSaveCommand("tenant_a", "zh-CN", "menu.system",
            "系统管理", "archived")));

    assertEquals("ZHYC_I18N_MESSAGE_STATUS_UNSUPPORTED", exception.getCode());
    assertEquals("词条状态不支持: archived", exception.getMessage());
  }

  /**
   * 验证词条列表按租户、语言和状态过滤。
   */
  @Test
  void shouldListMessagesByTenantLocaleAndStatus() {
    RecordingRepository repository = new RecordingRepository();
    I18nService service = new DefaultI18nService(repository);

    List<I18nMessageResponse> responses = service.listMessages(" tenant_a ", " zh-CN ", " enabled ");

    assertEquals("tenant_a", repository.lastTenantId);
    assertEquals("zh-CN", repository.lastLocale);
    assertEquals("enabled", repository.lastStatus);
    assertEquals("menu.system", responses.getFirst().messageKey());
  }

  /**
   * 验证解析词条时找不到配置会返回默认值。
   */
  @Test
  void shouldReturnDefaultWhenMessageMissing() {
    RecordingRepository repository = new RecordingRepository();
    repository.resolved = Optional.empty();
    I18nService service = new DefaultI18nService(repository);

    String value = service.resolveMessage("tenant_a", "en-US", "menu.system", "System");

    assertEquals("System", value);
  }

  /**
   * 验证批量解析词条会保留请求顺序，命中时返回词条值，未命中时返回默认文案。
   */
  @Test
  void shouldResolveMessagesInBatchWithDefaultFallback() {
    RecordingRepository repository = new RecordingRepository();
    I18nService service = new DefaultI18nService(repository);
    Map<String, String> defaults = new LinkedHashMap<>();
    defaults.put("menu.system", "System");
    defaults.put("menu.unknown", "Unknown");

    I18nResolveResponse response = service.resolveMessages(new I18nResolveCommand(" tenant_a ", " zh-CN ", defaults));

    assertEquals("tenant_a", repository.lastTenantId);
    assertEquals("zh-CN", repository.lastLocale);
    assertEquals("menu.unknown", repository.lastMessageKey);
    assertEquals("zh-CN", response.locale());
    Map<String, String> values = response.messages();
    assertEquals(List.of("menu.system", "menu.unknown"), values.keySet().stream().toList());
    assertEquals("系统管理", values.get("menu.system"));
    assertEquals("Unknown", values.get("menu.unknown"));
  }

  /**
   * 测试用国际化仓储。
   */
  private static class RecordingRepository implements I18nRepository {

    /** 最近一次保存的词条。 */
    private I18nMessage lastSaved;
    /** 最近一次查询租户。 */
    private String lastTenantId;
    /** 最近一次查询语言。 */
    private String lastLocale;
    /** 最近一次查询状态。 */
    private String lastStatus;
    /** 最近一次查询词条键。 */
    private String lastMessageKey;
    /** 解析结果。 */
    private Optional<I18nMessage> resolved = Optional.of(new I18nMessage(1L, "tenant_a", "zh-CN",
        "menu.system", "系统管理", "enabled", LocalDateTime.now(), LocalDateTime.now()));

    @Override
    public List<I18nMessage> findMessages(String tenantId, String locale, String status) {
      lastTenantId = tenantId;
      lastLocale = locale;
      lastStatus = status;
      return List.of(new I18nMessage(1L, tenantId, locale, "menu.system", "系统管理", status,
          LocalDateTime.now(), LocalDateTime.now()));
    }

    @Override
    public void saveMessage(I18nMessage message) {
      lastSaved = message;
    }

    @Override
    public Optional<I18nMessage> findEnabledMessage(String tenantId, String locale, String messageKey) {
      lastTenantId = tenantId;
      lastLocale = locale;
      lastMessageKey = messageKey;
      if ("menu.system".equals(messageKey)) {
        return resolved;
      }
      return Optional.empty();
    }
  }
}
