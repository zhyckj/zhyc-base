/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.user.controller;

import com.zhyc.common.api.ApiResult;
import com.zhyc.common.exception.BusinessException;
import com.zhyc.system.user.service.SysUserPostBindCommand;
import com.zhyc.system.user.service.SysUserPostBindItem;
import com.zhyc.system.user.service.SysUserPostResponse;
import com.zhyc.system.user.service.SysUserPostService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;

/**
 * 系统用户岗位绑定接口。
 */
@RestController
@RequestMapping("/system/users/{userId}/posts")
public class SysUserPostController {

    /** 用户岗位绑定请求缺失错误码。 */
    private static final String ERROR_BIND_REQUEST_REQUIRED =
            "ZHYC_SYSTEM_USER_POST_BIND_REQUEST_REQUIRED";

    /** 系统用户岗位业务服务。 */
    private final SysUserPostService userPostService;

    /**
     * 创建系统用户岗位绑定接口。
     *
     * @param userPostService 系统用户岗位业务服务
     */
    public SysUserPostController(SysUserPostService userPostService) {
        this.userPostService = Objects.requireNonNull(userPostService, "系统用户岗位业务服务不能为空");
    }

    /**
     * 查询用户岗位列表。
     *
     * @param userId 用户主键
     * @param tenantId 租户业务编码
     * @return 用户岗位列表
     */
    @RequiresPermissions("system:user:query")
    @GetMapping
    public ApiResult<List<SysUserPostResponse>> listUserPosts(@PathVariable("userId") Long userId,
                                                              @RequestParam("tenantId") String tenantId) {
        return ApiResult.ok(userPostService.listUserPosts(tenantId, userId));
    }

    /**
     * 绑定用户岗位列表。
     *
     * @param userId 用户主键
     * @param tenantId 租户业务编码
     * @param request 用户岗位绑定请求
     * @return 空响应
     */
    @RequiresPermissions("system:user:edit")
    @PutMapping
    public ApiResult<Void> bindUserPosts(@PathVariable("userId") Long userId,
                                         @RequestParam("tenantId") String tenantId,
                                         @RequestBody UserPostBindRequest request) {
        if (request == null) {
            throw new BusinessException(ERROR_BIND_REQUEST_REQUIRED, "用户岗位绑定请求不能为空");
        }
        List<SysUserPostBindItem> posts = request.getPosts() == null
                ? List.of()
                : request.getPosts().stream()
                        .map(item -> new SysUserPostBindItem(item.getPostId(), item.isPrimaryFlag()))
                        .toList();
        userPostService.bindUserPosts(new SysUserPostBindCommand(tenantId, userId, posts));
        return ApiResult.ok(null);
    }
}
