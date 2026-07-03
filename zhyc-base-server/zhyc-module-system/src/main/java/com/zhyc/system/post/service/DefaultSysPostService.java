/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.post.service;

import com.zhyc.system.post.domain.SysPost;
import com.zhyc.system.post.repository.SysPostRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * 默认系统岗位业务服务实现。
 */
@Service
public class DefaultSysPostService implements SysPostService {

    /** 系统岗位仓储。 */
    private final SysPostRepository postRepository;

    /**
     * 创建默认系统岗位业务服务。
     *
     * @param postRepository 系统岗位仓储
     */
    public DefaultSysPostService(SysPostRepository postRepository) {
        this.postRepository = Objects.requireNonNull(postRepository, "系统岗位仓储不能为空");
    }

    @Override
    public List<SysPostResponse> listPosts(String tenantId, Long orgId) {
        String requiredTenantId = requireText(tenantId, "租户业务编码不能为空");
        return postRepository.findByTenantIdAndOrgId(requiredTenantId, orgId).stream()
                .sorted(Comparator.comparing(SysPost::getSortOrder, Comparator.nullsLast(Integer::compareTo))
                        .thenComparing(SysPost::getId, Comparator.nullsLast(Long::compareTo)))
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public void savePost(SysPostSaveCommand command) {
        Objects.requireNonNull(command, "岗位保存命令不能为空");
        String requiredTenantId = requireText(command.getTenantId(), "租户业务编码不能为空");
        SysPost post = new SysPost();
        post.setId(command.getPostId());
        post.setTenantId(requiredTenantId);
        post.setOrgId(command.getOrgId());
        post.setPostCode(requireText(command.getPostCode(), "岗位编码不能为空"));
        post.setPostName(requireText(command.getPostName(), "岗位名称不能为空"));
        post.setSortOrder(command.getSortOrder() == null ? 0 : command.getSortOrder());
        post.setStatus(normalizeStatus(command.getStatus()));
        if (command.getPostId() == null) {
            postRepository.insert(post);
            return;
        }
        Long requiredPostId = requirePositive(command.getPostId(), "岗位主键不能为空");
        validateTenantPost(requiredTenantId, requiredPostId);
        postRepository.update(post);
    }

    @Override
    @Transactional
    public void updateStatus(String tenantId, Long postId, String status) {
        String requiredTenantId = requireText(tenantId, "租户业务编码不能为空");
        Long requiredPostId = requirePositive(postId, "岗位主键不能为空");
        validateTenantPost(requiredTenantId, requiredPostId);
        postRepository.updateStatus(requiredTenantId, requiredPostId, normalizeStatus(status));
    }

    @Override
    @Transactional
    public void deletePost(String tenantId, Long postId) {
        String requiredTenantId = requireText(tenantId, "租户业务编码不能为空");
        Long requiredPostId = requirePositive(postId, "岗位主键不能为空");
        validateTenantPost(requiredTenantId, requiredPostId);
        postRepository.deleteByTenantIdAndId(requiredTenantId, requiredPostId);
    }

    private SysPostResponse toResponse(SysPost post) {
        return new SysPostResponse(post.getId(), post.getTenantId(), post.getOrgId(), post.getPostCode(),
                post.getPostName(), post.getSortOrder(), post.getStatus());
    }

    private String requireText(String value, String message) {
        if (value == null || value.trim().isEmpty()) {
            throw com.zhyc.system.support.SystemServiceValidation.businessFailure(message);
        }
        return value.trim();
    }

    private void validateTenantPost(String tenantId, Long postId) {
        boolean exists = postRepository.findByTenantIdAndOrgId(tenantId, null).stream()
                .map(SysPost::getId)
                .filter(Objects::nonNull)
                .anyMatch(postId::equals);
        if (!exists) {
            throw com.zhyc.system.support.SystemServiceValidation.businessFailure("岗位主键不属于当前租户：" + postId);
        }
    }

    private String normalizeStatus(String status) {
        String normalizedStatus = requireText(status, "岗位状态不能为空").toLowerCase();
        if (!"enabled".equals(normalizedStatus) && !"disabled".equals(normalizedStatus)) {
            throw com.zhyc.system.support.SystemServiceValidation.businessFailure("岗位状态仅支持 enabled 或 disabled");
        }
        return normalizedStatus;
    }

    private Long requirePositive(Long value, String message) {
        if (value == null || value <= 0) {
            throw com.zhyc.system.support.SystemServiceValidation.businessFailure(message);
        }
        return value;
    }
}
