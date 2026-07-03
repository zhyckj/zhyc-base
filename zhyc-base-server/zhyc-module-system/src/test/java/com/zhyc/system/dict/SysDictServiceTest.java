/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.dict;

import com.zhyc.system.dict.domain.SysDictItem;
import com.zhyc.system.dict.domain.SysDictType;
import com.zhyc.system.dict.repository.SysDictRepository;
import com.zhyc.system.dict.service.DefaultSysDictService;
import com.zhyc.system.dict.service.SysDictItemResponse;
import com.zhyc.system.dict.service.SysDictService;
import com.zhyc.system.dict.service.SysDictTypeResponse;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 系统字典业务服务测试。
 */
class SysDictServiceTest {

    /**
     * 验证系统字典服务按租户查询字典类型列表。
     */
    @Test
    void shouldListTenantDictTypes() {
        RecordingDictRepository repository = new RecordingDictRepository();
        SysDictService service = new DefaultSysDictService(repository);

        List<SysDictTypeResponse> types = service.listTypes(" tenant_a ");

        assertEquals("tenant_a", repository.lastTenantId);
        assertEquals(1, types.size());
        assertEquals("user_status", types.get(0).getDictCode());
        assertEquals("用户状态", types.get(0).getDictName());
    }

    /**
     * 验证系统字典服务按租户和字典编码查询字典项。
     */
    @Test
    void shouldListTenantDictItemsByCode() {
        RecordingDictRepository repository = new RecordingDictRepository();
        SysDictService service = new DefaultSysDictService(repository);

        List<SysDictItemResponse> items = service.listItems(" tenant_a ", " user_status ");

        assertEquals("tenant_a", repository.lastTenantId);
        assertEquals("user_status", repository.lastDictCode);
        assertEquals(2, items.size());
        assertEquals("enabled", items.get(0).getItemValue());
    }

    /**
     * 测试用系统字典仓储。
     */
    private static class RecordingDictRepository implements SysDictRepository {

        /** 最近一次查询的租户业务编码。 */
        private String lastTenantId;
        /** 最近一次查询的字典编码。 */
        private String lastDictCode;

        @Override
        public List<SysDictType> findTypesByTenantId(String tenantId) {
            lastTenantId = tenantId;
            return List.of(new SysDictType(1L, tenantId, "user_status", "用户状态",
                    false, "enabled", LocalDateTime.now(), LocalDateTime.now()));
        }

        @Override
        public void insertType(SysDictType type) {
            throw new AssertionError("字典查询测试不应新增字典类型");
        }

        @Override
        public void updateType(SysDictType type) {
            throw new AssertionError("字典查询测试不应更新字典类型");
        }

        @Override
        public void deleteTypeByTenantIdAndId(String tenantId, Long typeId) {
            throw new AssertionError("字典查询测试不应删除字典类型");
        }

        @Override
        public List<SysDictItem> findItemsByTenantIdAndDictCode(String tenantId, String dictCode) {
            lastTenantId = tenantId;
            lastDictCode = dictCode;
            return List.of(
                    new SysDictItem(1L, tenantId, dictCode, "启用", "enabled", "green",
                            1, "enabled", LocalDateTime.now(), LocalDateTime.now()),
                    new SysDictItem(2L, tenantId, dictCode, "停用", "disabled", "red",
                            2, "enabled", LocalDateTime.now(), LocalDateTime.now()));
        }

        @Override
        public void insertItem(SysDictItem item) {
            throw new AssertionError("字典查询测试不应新增字典项");
        }

        @Override
        public void updateItem(SysDictItem item) {
            throw new AssertionError("字典查询测试不应更新字典项");
        }

        @Override
        public void deleteItemByTenantIdAndId(String tenantId, Long itemId) {
            throw new AssertionError("字典查询测试不应删除字典项");
        }
    }
}
