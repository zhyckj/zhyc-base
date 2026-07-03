/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.post.controller;

import com.zhyc.common.api.ApiResult;
import com.zhyc.common.exception.BusinessException;
import com.zhyc.system.post.service.SysPostResponse;
import com.zhyc.system.post.service.SysPostSaveCommand;
import com.zhyc.system.post.service.SysPostService;
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
 * 系统岗位管理接口。
 */
@RestController
@RequestMapping("/system/posts")
public class SysPostController {

    /** 岗位保存请求缺失错误码。 */
    private static final String ERROR_SAVE_REQUEST_REQUIRED = "ZHYC_SYSTEM_POST_SAVE_REQUEST_REQUIRED";
    /** 岗位状态请求缺失错误码。 */
    private static final String ERROR_STATUS_REQUEST_REQUIRED = "ZHYC_SYSTEM_POST_STATUS_REQUEST_REQUIRED";

    /** 系统岗位业务服务。 */
    private final SysPostService postService;

    /**
     * 创建系统岗位管理接口。
     *
     * @param postService 系统岗位业务服务
     */
    public SysPostController(SysPostService postService) {
        this.postService = Objects.requireNonNull(postService, "系统岗位业务服务不能为空");
    }

    /**
     * 查询租户内岗位列表。
     *
     * @param tenantId 租户业务编码
     * @param orgId 所属组织主键，为空时查询租户内全部岗位
     * @return 岗位响应列表
     */
    @RequiresPermissions("system:post:query")
    @GetMapping
    public ApiResult<List<SysPostResponse>> listPosts(@RequestParam("tenantId") String tenantId,
                                                      @RequestParam(value = "orgId", required = false) Long orgId) {
        return ApiResult.ok(postService.listPosts(tenantId, orgId));
    }

    @RequiresPermissions("system:post:create")
    @PostMapping
    public ApiResult<Void> createPost(@RequestBody SysPostSaveRequest request) {
        SysPostSaveRequest requiredRequest = requireSaveRequest(request);
        postService.savePost(toSaveCommand(null, requiredRequest));
        return ApiResult.ok(null);
    }

    @RequiresPermissions("system:post:update")
    @PutMapping("/{postId}")
    public ApiResult<Void> updatePost(@PathVariable("postId") Long postId, @RequestBody SysPostSaveRequest request) {
        SysPostSaveRequest requiredRequest = requireSaveRequest(request);
        postService.savePost(toSaveCommand(postId, requiredRequest));
        return ApiResult.ok(null);
    }

    @RequiresPermissions("system:post:update-status")
    @PutMapping("/{postId}/status")
    public ApiResult<Void> updateStatus(@PathVariable("postId") Long postId,
                                        @RequestBody SysPostStatusRequest request) {
        if (request == null) {
            throw new BusinessException(ERROR_STATUS_REQUEST_REQUIRED, "岗位状态请求不能为空");
        }
        postService.updateStatus(request.getTenantId(), postId, request.getStatus());
        return ApiResult.ok(null);
    }

    @RequiresPermissions("system:post:delete")
    @DeleteMapping("/{postId}")
    public ApiResult<Void> deletePost(@PathVariable("postId") Long postId,
                                      @RequestParam("tenantId") String tenantId) {
        postService.deletePost(tenantId, postId);
        return ApiResult.ok(null);
    }

    private SysPostSaveRequest requireSaveRequest(SysPostSaveRequest request) {
        if (request == null) {
            throw new BusinessException(ERROR_SAVE_REQUEST_REQUIRED, "岗位保存请求不能为空");
        }
        return request;
    }

    private SysPostSaveCommand toSaveCommand(Long postId, SysPostSaveRequest request) {
        return new SysPostSaveCommand(postId, request.getTenantId(), request.getOrgId(), request.getPostCode(),
                request.getPostName(), request.getSortOrder(), request.getStatus());
    }
}
