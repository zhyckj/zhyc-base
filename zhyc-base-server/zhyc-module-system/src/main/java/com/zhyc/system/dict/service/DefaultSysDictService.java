/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.dict.service;

import com.zhyc.common.cache.ZhycCacheNames;
import com.zhyc.system.dict.domain.SysDictItem;
import com.zhyc.system.dict.domain.SysDictType;
import com.zhyc.system.dict.repository.SysDictRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

/**
 * 默认系统字典业务服务实现。
 */
@Service
public class DefaultSysDictService implements SysDictService {

    /** 系统字典仓储。 */
    private final SysDictRepository dictRepository;

    /**
     * 创建默认系统字典业务服务。
     *
     * @param dictRepository 系统字典仓储
     */
    public DefaultSysDictService(SysDictRepository dictRepository) {
        this.dictRepository = Objects.requireNonNull(dictRepository, "系统字典仓储不能为空");
    }

    @Override
    @Cacheable(cacheNames = ZhycCacheNames.SYS_DICT_TYPES,
            key = "#tenantId == null ? '' : #tenantId.trim()")
    public List<SysDictTypeResponse> listTypes(String tenantId) {
        String requiredTenantId = requireText(tenantId, "租户业务编码不能为空");
        return dictRepository.findTypesByTenantId(requiredTenantId).stream()
                .map(this::toTypeResponse)
                .toList();
    }

    @Override
    @Cacheable(cacheNames = ZhycCacheNames.SYS_DICT_ITEMS,
            key = "(#tenantId == null ? '' : #tenantId.trim()) + ':' + (#dictCode == null ? '' : #dictCode.trim())")
    public List<SysDictItemResponse> listItems(String tenantId, String dictCode) {
        String requiredTenantId = requireText(tenantId, "租户业务编码不能为空");
        String requiredDictCode = requireText(dictCode, "字典编码不能为空");
        return dictRepository.findItemsByTenantIdAndDictCode(requiredTenantId, requiredDictCode).stream()
                .map(this::toItemResponse)
                .toList();
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = {ZhycCacheNames.SYS_DICT_TYPES, ZhycCacheNames.SYS_DICT_ITEMS}, allEntries = true)
    public void saveType(SysDictTypeSaveCommand command) {
        Objects.requireNonNull(command, "字典类型保存命令不能为空");
        SysDictType type = new SysDictType();
        type.setId(command.getTypeId());
        type.setTenantId(requireText(command.getTenantId(), "租户业务编码不能为空"));
        type.setDictCode(requireText(command.getDictCode(), "字典编码不能为空"));
        type.setDictName(requireText(command.getDictName(), "字典名称不能为空"));
        type.setSystemFlag(Boolean.TRUE.equals(command.getSystemFlag()));
        type.setStatus(normalizeStatus(command.getStatus()));
        if (command.getTypeId() == null) {
            dictRepository.insertType(type);
            return;
        }
        dictRepository.updateType(type);
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = {ZhycCacheNames.SYS_DICT_TYPES, ZhycCacheNames.SYS_DICT_ITEMS}, allEntries = true)
    public void deleteType(String tenantId, Long typeId) {
        dictRepository.deleteTypeByTenantIdAndId(requireText(tenantId, "租户业务编码不能为空"),
                requirePositive(typeId, "字典类型主键不能为空"));
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = ZhycCacheNames.SYS_DICT_ITEMS, allEntries = true)
    public void saveItem(SysDictItemSaveCommand command) {
        Objects.requireNonNull(command, "字典项保存命令不能为空");
        SysDictItem item = new SysDictItem();
        item.setId(command.getItemId());
        item.setTenantId(requireText(command.getTenantId(), "租户业务编码不能为空"));
        item.setDictCode(requireText(command.getDictCode(), "字典编码不能为空"));
        item.setItemLabel(requireText(command.getItemLabel(), "字典项标签不能为空"));
        item.setItemValue(requireText(command.getItemValue(), "字典项值不能为空"));
        item.setItemColor(trimToNull(command.getItemColor()));
        item.setSortOrder(command.getSortOrder() == null ? 0 : command.getSortOrder());
        item.setStatus(normalizeStatus(command.getStatus()));
        if (command.getItemId() == null) {
            dictRepository.insertItem(item);
            return;
        }
        dictRepository.updateItem(item);
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = ZhycCacheNames.SYS_DICT_ITEMS, allEntries = true)
    public void deleteItem(String tenantId, Long itemId) {
        dictRepository.deleteItemByTenantIdAndId(requireText(tenantId, "租户业务编码不能为空"),
                requirePositive(itemId, "字典项主键不能为空"));
    }

    private SysDictTypeResponse toTypeResponse(SysDictType type) {
        return new SysDictTypeResponse(type.getId(), type.getTenantId(), type.getDictCode(), type.getDictName(),
                type.isSystemFlag(), type.getStatus());
    }

    private SysDictItemResponse toItemResponse(SysDictItem item) {
        return new SysDictItemResponse(item.getId(), item.getTenantId(), item.getDictCode(), item.getItemLabel(),
                item.getItemValue(), item.getItemColor(), item.getSortOrder(), item.getStatus());
    }

    private String requireText(String value, String message) {
        if (value == null || value.trim().isEmpty()) {
            throw com.zhyc.system.support.SystemServiceValidation.businessFailure(message);
        }
        return value.trim();
    }

    private String normalizeStatus(String status) {
        String normalizedStatus = requireText(status, "状态不能为空").toLowerCase();
        if (!"enabled".equals(normalizedStatus) && !"disabled".equals(normalizedStatus)) {
            throw com.zhyc.system.support.SystemServiceValidation.businessFailure("状态仅支持 enabled 或 disabled");
        }
        return normalizedStatus;
    }

    private Long requirePositive(Long value, String message) {
        if (value == null || value <= 0) {
            throw com.zhyc.system.support.SystemServiceValidation.businessFailure(message);
        }
        return value;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
