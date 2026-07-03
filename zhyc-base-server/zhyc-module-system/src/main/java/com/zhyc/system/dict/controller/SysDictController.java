/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.dict.controller;

import com.zhyc.common.api.ApiResult;
import com.zhyc.common.exception.BusinessException;
import com.zhyc.system.dict.service.SysDictItemSaveCommand;
import com.zhyc.system.dict.service.SysDictItemResponse;
import com.zhyc.system.dict.service.SysDictService;
import com.zhyc.system.dict.service.SysDictTypeSaveCommand;
import com.zhyc.system.dict.service.SysDictTypeResponse;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;

/**
 * 系统字典管理接口。
 */
@RestController
@RequestMapping("/system/dicts")
public class SysDictController {

    private static final String ERROR_TYPE_SAVE_REQUEST_REQUIRED = "ZHYC_SYSTEM_DICT_TYPE_SAVE_REQUEST_REQUIRED";
    private static final String ERROR_ITEM_SAVE_REQUEST_REQUIRED = "ZHYC_SYSTEM_DICT_ITEM_SAVE_REQUEST_REQUIRED";

    /** 系统字典业务服务。 */
    private final SysDictService dictService;

    /**
     * 创建系统字典管理接口。
     *
     * @param dictService 系统字典业务服务
     */
    public SysDictController(SysDictService dictService) {
        this.dictService = Objects.requireNonNull(dictService, "系统字典业务服务不能为空");
    }

    /**
     * 查询租户字典类型列表。
     *
     * @param tenantId 租户业务编码
     * @return 字典类型列表
     */
    @RequiresPermissions("system:dict:query")
    @GetMapping("/types")
    public ApiResult<List<SysDictTypeResponse>> listTypes(@RequestParam("tenantId") String tenantId) {
        return ApiResult.ok(dictService.listTypes(tenantId));
    }

    @RequiresPermissions("system:dict:create")
    @PostMapping("/types")
    public ApiResult<Void> createType(@RequestBody SysDictTypeSaveRequest request) {
        SysDictTypeSaveRequest requiredRequest = requireTypeRequest(request);
        dictService.saveType(toTypeCommand(null, requiredRequest));
        return ApiResult.ok(null);
    }

    @RequiresPermissions("system:dict:update")
    @PutMapping("/types/{typeId}")
    public ApiResult<Void> updateType(@PathVariable("typeId") Long typeId,
                                      @RequestBody SysDictTypeSaveRequest request) {
        SysDictTypeSaveRequest requiredRequest = requireTypeRequest(request);
        dictService.saveType(toTypeCommand(typeId, requiredRequest));
        return ApiResult.ok(null);
    }

    @RequiresPermissions("system:dict:delete")
    @DeleteMapping("/types/{typeId}")
    public ApiResult<Void> deleteType(@PathVariable("typeId") Long typeId, @RequestParam("tenantId") String tenantId) {
        dictService.deleteType(tenantId, typeId);
        return ApiResult.ok(null);
    }

    /**
     * 查询租户指定字典编码下的字典项。
     *
     * @param tenantId 租户业务编码
     * @param dictCode 字典编码
     * @return 字典项列表
     */
    @RequiresPermissions("system:dict:query")
    @GetMapping("/items")
    public ApiResult<List<SysDictItemResponse>> listItems(@RequestParam("tenantId") String tenantId,
                                                          @RequestParam("dictCode") String dictCode) {
        return ApiResult.ok(dictService.listItems(tenantId, dictCode));
    }

    @RequiresPermissions("system:dict:item:create")
    @PostMapping("/items")
    public ApiResult<Void> createItem(@RequestBody SysDictItemSaveRequest request) {
        SysDictItemSaveRequest requiredRequest = requireItemRequest(request);
        dictService.saveItem(toItemCommand(null, requiredRequest));
        return ApiResult.ok(null);
    }

    @RequiresPermissions("system:dict:item:update")
    @PutMapping("/items/{itemId}")
    public ApiResult<Void> updateItem(@PathVariable("itemId") Long itemId,
                                      @RequestBody SysDictItemSaveRequest request) {
        SysDictItemSaveRequest requiredRequest = requireItemRequest(request);
        dictService.saveItem(toItemCommand(itemId, requiredRequest));
        return ApiResult.ok(null);
    }

    @RequiresPermissions("system:dict:item:delete")
    @DeleteMapping("/items/{itemId}")
    public ApiResult<Void> deleteItem(@PathVariable("itemId") Long itemId, @RequestParam("tenantId") String tenantId) {
        dictService.deleteItem(tenantId, itemId);
        return ApiResult.ok(null);
    }

    private SysDictTypeSaveRequest requireTypeRequest(SysDictTypeSaveRequest request) {
        if (request == null) {
            throw new BusinessException(ERROR_TYPE_SAVE_REQUEST_REQUIRED, "字典类型保存请求不能为空");
        }
        return request;
    }

    private SysDictItemSaveRequest requireItemRequest(SysDictItemSaveRequest request) {
        if (request == null) {
            throw new BusinessException(ERROR_ITEM_SAVE_REQUEST_REQUIRED, "字典项保存请求不能为空");
        }
        return request;
    }

    private SysDictTypeSaveCommand toTypeCommand(Long typeId, SysDictTypeSaveRequest request) {
        return new SysDictTypeSaveCommand(typeId, request.getTenantId(), request.getDictCode(), request.getDictName(),
                request.getSystemFlag(), request.getStatus());
    }

    private SysDictItemSaveCommand toItemCommand(Long itemId, SysDictItemSaveRequest request) {
        return new SysDictItemSaveCommand(itemId, request.getTenantId(), request.getDictCode(), request.getItemLabel(),
                request.getItemValue(), request.getItemColor(), request.getSortOrder(), request.getStatus());
    }
}
