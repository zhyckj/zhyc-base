/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.tenantparam;

import com.zhyc.common.exception.BusinessException;
import com.zhyc.system.tenantparam.domain.SysTenantParam;
import com.zhyc.system.tenantparam.repository.SysTenantParamRepository;
import com.zhyc.system.tenantparam.service.DefaultSysTenantParamService;
import com.zhyc.system.tenantparam.service.SysTenantParamResponse;
import com.zhyc.system.tenantparam.service.SysTenantParamSaveCommand;
import com.zhyc.system.tenantparam.service.SysTenantParamService;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 租户参数业务服务测试。
 */
class SysTenantParamServiceTest {

    /**
     * 验证服务会按租户查询租户参数列表并转换可见字段。
     */
    @Test
    void shouldListTenantParamsByTenantId() {
        RecordingTenantParamRepository repository = new RecordingTenantParamRepository();
        SysTenantParamService service = new DefaultSysTenantParamService(repository);

        List<SysTenantParamResponse> params = service.listParams(" tenant_a ");

        assertEquals("tenant_a", repository.lastTenantId);
        assertEquals(1, params.size());
        assertEquals("theme", params.get(0).getParamKey());
        assertTrue(params.get(0).isVisible());
    }

    /**
     * 验证租户参数保存会裁剪租户编码和参数键。
     */
    @Test
    void shouldSaveTenantParam() {
        RecordingTenantParamRepository repository = new RecordingTenantParamRepository();
        SysTenantParamService service = new DefaultSysTenantParamService(repository);

        service.save(new SysTenantParamSaveCommand(" tenant_a ", " theme ", "dark", "string", true));

        assertEquals("tenant_a", repository.savedParam.getTenantId());
        assertEquals("theme", repository.savedParam.getParamKey());
        assertEquals("dark", repository.savedParam.getParamValue());
        assertTrue(repository.savedParam.isVisible());
    }

    /**
     * 验证租户参数值类型只能使用配置解析约定的基础类型。
     */
    @Test
    void shouldRejectUnsupportedValueType() {
        RecordingTenantParamRepository repository = new RecordingTenantParamRepository();
        SysTenantParamService service = new DefaultSysTenantParamService(repository);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.save(new SysTenantParamSaveCommand("tenant_a", "theme", "dark", "xml", true)));

        assertEquals("ZHYC_SYS_TENANT_PARAM_VALUE_TYPE_UNSUPPORTED", exception.getCode());
        assertEquals("参数值类型不支持: xml", exception.getMessage());
    }

    /**
     * 测试用租户参数仓储。
     */
    private static class RecordingTenantParamRepository implements SysTenantParamRepository {

        /** 最近一次查询租户业务编码。 */
        private String lastTenantId;
        /** 最近一次保存参数。 */
        private SysTenantParam savedParam;

        @Override
        public List<SysTenantParam> findByTenantId(String tenantId) {
            lastTenantId = tenantId;
            return List.of(new SysTenantParam(1L, tenantId, "theme", "dark",
                    "string", true, LocalDateTime.now(), LocalDateTime.now()));
        }

        @Override
        public Optional<SysTenantParam> findByTenantIdAndParamKey(String tenantId, String paramKey) {
            return Optional.empty();
        }

        @Override
        public void save(SysTenantParam param) {
            savedParam = param;
        }
    }
}
