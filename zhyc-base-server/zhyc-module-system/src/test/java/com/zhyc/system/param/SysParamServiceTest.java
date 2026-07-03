/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.param;

import com.zhyc.common.exception.BusinessException;
import com.zhyc.system.param.domain.SysParam;
import com.zhyc.system.param.repository.SysParamRepository;
import com.zhyc.system.param.service.DefaultSysParamService;
import com.zhyc.system.param.service.SysParamResponse;
import com.zhyc.system.param.service.SysParamSaveCommand;
import com.zhyc.system.param.service.SysParamService;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 系统参数业务服务测试。
 */
class SysParamServiceTest {

    /**
     * 验证系统参数服务按租户查询参数列表。
     */
    @Test
    void shouldListTenantParams() {
        RecordingParamRepository repository = new RecordingParamRepository();
        SysParamService service = new DefaultSysParamService(repository);

        List<SysParamResponse> params = service.listParams(" tenant_a ");

        assertEquals("tenant_a", repository.lastTenantId);
        assertEquals(1, params.size());
        assertEquals("platform.name", params.get(0).getParamKey());
        assertEquals("快速开发平台", params.get(0).getParamValue());
    }

    /**
     * 验证系统参数服务按租户和参数键查询单个参数。
     */
    @Test
    void shouldFindTenantParamByKey() {
        RecordingParamRepository repository = new RecordingParamRepository();
        SysParamService service = new DefaultSysParamService(repository);

        Optional<SysParamResponse> param = service.findByKey(" tenant_a ", " platform.name ");

        assertTrue(param.isPresent());
        assertEquals("tenant_a", repository.lastTenantId);
        assertEquals("platform.name", repository.lastParamKey);
    }

    /**
     * 验证系统参数保存会裁剪租户与参数键，并保留系统标记和可编辑标记。
     */
    @Test
    void shouldSaveTenantParam() {
        RecordingParamRepository repository = new RecordingParamRepository();
        SysParamService service = new DefaultSysParamService(repository);

        service.save(new SysParamSaveCommand(" tenant_a ", " platform.name ", "快速开发平台",
                "string", true, false));

        assertEquals("tenant_a", repository.lastSaved.getTenantId());
        assertEquals("platform.name", repository.lastSaved.getParamKey());
        assertEquals("快速开发平台", repository.lastSaved.getParamValue());
        assertEquals("string", repository.lastSaved.getValueType());
        assertTrue(repository.lastSaved.isSystemFlag());
    }

    /**
     * 验证系统参数值类型只能使用配置解析约定的基础类型。
     */
    @Test
    void shouldRejectUnsupportedValueType() {
        RecordingParamRepository repository = new RecordingParamRepository();
        SysParamService service = new DefaultSysParamService(repository);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.save(new SysParamSaveCommand("tenant_a", "platform.name", "快速开发平台",
                        "xml", true, false)));

        assertEquals("ZHYC_SYS_PARAM_VALUE_TYPE_UNSUPPORTED", exception.getCode());
        assertEquals("参数值类型不支持: xml", exception.getMessage());
    }

    /**
     * 测试用系统参数仓储。
     */
    private static class RecordingParamRepository implements SysParamRepository {

        /** 最近一次查询的租户业务编码。 */
        private String lastTenantId;
        /** 最近一次查询的参数键。 */
        private String lastParamKey;
        /** 最近一次保存的系统参数。 */
        private SysParam lastSaved;

        @Override
        public List<SysParam> findByTenantId(String tenantId) {
            lastTenantId = tenantId;
            return List.of(new SysParam(1L, tenantId, "platform.name", "快速开发平台",
                    "string", false, true, LocalDateTime.now(), LocalDateTime.now()));
        }

        @Override
        public Optional<SysParam> findByTenantIdAndParamKey(String tenantId, String paramKey) {
            lastTenantId = tenantId;
            lastParamKey = paramKey;
            return Optional.of(new SysParam(1L, tenantId, paramKey, "快速开发平台",
                    "string", false, true, LocalDateTime.now(), LocalDateTime.now()));
        }

        @Override
        public void save(SysParam param) {
            lastSaved = param;
        }
    }
}
