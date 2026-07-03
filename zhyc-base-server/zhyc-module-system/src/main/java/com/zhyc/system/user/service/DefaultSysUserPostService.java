/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.user.service;

import com.zhyc.system.post.domain.SysPost;
import com.zhyc.system.post.repository.SysPostRepository;
import com.zhyc.system.user.domain.SysUserPost;
import com.zhyc.system.user.repository.SysUserPostRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 默认系统用户岗位业务服务实现。
 */
@Service
public class DefaultSysUserPostService implements SysUserPostService {

    /** 系统用户岗位仓储。 */
    private final SysUserPostRepository userPostRepository;
    /** 系统岗位仓储，用于校验岗位是否属于当前租户。 */
    private final SysPostRepository postRepository;

    /**
     * 创建默认系统用户岗位业务服务。
     *
     * @param userPostRepository 系统用户岗位仓储
     * @param postRepository 系统岗位仓储
     */
    public DefaultSysUserPostService(SysUserPostRepository userPostRepository, SysPostRepository postRepository) {
        this.userPostRepository = Objects.requireNonNull(userPostRepository, "系统用户岗位仓储不能为空");
        this.postRepository = Objects.requireNonNull(postRepository, "系统岗位仓储不能为空");
    }

    @Override
    public List<SysUserPostResponse> listUserPosts(String tenantId, Long userId) {
        String requiredTenantId = requireText(tenantId, "租户业务编码不能为空");
        Long requiredUserId = requirePositive(userId, "用户主键不能为空");
        return userPostRepository.findByTenantIdAndUserId(requiredTenantId, requiredUserId).stream()
                .sorted(Comparator.comparing(SysUserPost::isPrimaryFlag).reversed()
                        .thenComparing(SysUserPost::getPostId, Comparator.nullsLast(Long::compareTo)))
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public void bindUserPosts(SysUserPostBindCommand command) {
        Objects.requireNonNull(command, "用户岗位绑定命令不能为空");
        String requiredTenantId = requireText(command.getTenantId(), "租户业务编码不能为空");
        Long requiredUserId = requirePositive(command.getUserId(), "用户主键不能为空");
        Map<Long, SysUserPost> bindings = new LinkedHashMap<>();
        boolean primaryAssigned = false;
        if (command.getPosts() != null) {
            for (SysUserPostBindItem item : command.getPosts()) {
                if (item == null || item.getPostId() == null || item.getPostId() <= 0
                        || bindings.containsKey(item.getPostId())) {
                    continue;
                }
                boolean primaryFlag = item.isPrimaryFlag() && !primaryAssigned;
                if (primaryFlag) {
                    primaryAssigned = true;
                }
                bindings.put(item.getPostId(), new SysUserPost(null, requiredTenantId, requiredUserId,
                        item.getPostId(), null, null, primaryFlag, null));
            }
        }
        validateTenantPosts(requiredTenantId, bindings.keySet());
        userPostRepository.replaceUserPosts(requiredTenantId, requiredUserId, List.copyOf(bindings.values()));
    }

    /**
     * 校验待绑定岗位均属于当前租户。
     *
     * <p>必须在替换用户岗位绑定前完成，避免非法岗位导致旧绑定被提前清空。</p>
     *
     * @param tenantId 租户业务编码
     * @param postIds 待绑定岗位主键集合
     */
    private void validateTenantPosts(String tenantId, Set<Long> postIds) {
        if (postIds.isEmpty()) {
            return;
        }
        Set<Long> tenantPostIds = postRepository.findByTenantIdAndOrgId(tenantId, null).stream()
                .map(SysPost::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        for (Long postId : postIds) {
            if (!tenantPostIds.contains(postId)) {
                throw com.zhyc.system.support.SystemServiceValidation.businessFailure("岗位主键不属于当前租户：" + postId);
            }
        }
    }

    private SysUserPostResponse toResponse(SysUserPost userPost) {
        return new SysUserPostResponse(userPost.getPostId(), userPost.getPostCode(), userPost.getPostName(),
                userPost.isPrimaryFlag());
    }

    private String requireText(String value, String message) {
        if (value == null || value.trim().isEmpty()) {
            throw com.zhyc.system.support.SystemServiceValidation.businessFailure(message);
        }
        return value.trim();
    }

    private Long requirePositive(Long value, String message) {
        if (value == null || value <= 0) {
            throw com.zhyc.system.support.SystemServiceValidation.businessFailure(message);
        }
        return value;
    }
}
