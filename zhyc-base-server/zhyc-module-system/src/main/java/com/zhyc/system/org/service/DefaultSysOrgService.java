/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.org.service;

import com.zhyc.system.org.domain.SysOrg;
import com.zhyc.system.org.repository.SysOrgRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 默认系统组织机构业务服务实现。
 */
@Service
public class DefaultSysOrgService implements SysOrgService {

    /** 系统组织机构仓储。 */
    private final SysOrgRepository orgRepository;

    /**
     * 创建默认系统组织机构业务服务。
     *
     * @param orgRepository 系统组织机构仓储
     */
    public DefaultSysOrgService(SysOrgRepository orgRepository) {
        this.orgRepository = Objects.requireNonNull(orgRepository, "系统组织机构仓储不能为空");
    }

    @Override
    public List<SysOrgTreeNode> listOrgTree(String tenantId) {
        String requiredTenantId = requireText(tenantId, "租户业务编码不能为空");
        List<SysOrg> orgs = orgRepository.findByTenantId(requiredTenantId).stream()
                .sorted(Comparator.comparing(SysOrg::getSortOrder, Comparator.nullsLast(Integer::compareTo))
                        .thenComparing(SysOrg::getId, Comparator.nullsLast(Long::compareTo)))
                .toList();
        Map<Long, SysOrgTreeNode> nodeMap = new LinkedHashMap<>();
        orgs.forEach(org -> nodeMap.put(org.getId(), SysOrgTreeNode.from(org)));

        List<SysOrgTreeNode> roots = new ArrayList<>();
        for (SysOrg org : orgs) {
            SysOrgTreeNode node = nodeMap.get(org.getId());
            SysOrgTreeNode parent = nodeMap.get(org.getParentId());
            if (parent == null) {
                roots.add(node);
                continue;
            }
            parent.addChild(node);
        }
        return roots;
    }

    @Override
    @Transactional
    public void saveOrg(SysOrgSaveCommand command) {
        Objects.requireNonNull(command, "组织保存命令不能为空");
        String requiredTenantId = requireText(command.getTenantId(), "租户业务编码不能为空");
        SysOrg org = new SysOrg();
        org.setId(command.getOrgId());
        org.setTenantId(requiredTenantId);
        org.setParentId(command.getParentId());
        org.setAncestors(resolveAncestors(requiredTenantId, command.getParentId()));
        org.setOrgCode(requireText(command.getOrgCode(), "组织编码不能为空"));
        org.setOrgName(requireText(command.getOrgName(), "组织名称不能为空"));
        org.setLeaderUserId(command.getLeaderUserId());
        org.setSortOrder(command.getSortOrder() == null ? 0 : command.getSortOrder());
        org.setStatus(normalizeStatus(command.getStatus()));
        if (command.getOrgId() == null) {
            orgRepository.insert(org);
            return;
        }
        Long requiredOrgId = requirePositive(command.getOrgId(), "组织主键不能为空");
        validateTenantOrg(requiredTenantId, requiredOrgId);
        orgRepository.update(org);
    }

    @Override
    @Transactional
    public void updateStatus(String tenantId, Long orgId, String status) {
        String requiredTenantId = requireText(tenantId, "租户业务编码不能为空");
        Long requiredOrgId = requirePositive(orgId, "组织主键不能为空");
        validateTenantOrg(requiredTenantId, requiredOrgId);
        orgRepository.updateStatus(requiredTenantId, requiredOrgId, normalizeStatus(status));
    }

    @Override
    @Transactional
    public void deleteOrg(String tenantId, Long orgId) {
        String requiredTenantId = requireText(tenantId, "租户业务编码不能为空");
        Long requiredOrgId = requirePositive(orgId, "组织主键不能为空");
        validateTenantOrg(requiredTenantId, requiredOrgId);
        orgRepository.deleteByTenantIdAndId(requiredTenantId, requiredOrgId);
    }

    private String resolveAncestors(String tenantId, Long parentId) {
        if (parentId == null || parentId <= 0) {
            return "0";
        }
        return orgRepository.findByTenantId(tenantId).stream()
                .filter(org -> parentId.equals(org.getId()))
                .findFirst()
                .map(parent -> parent.getAncestors() + "," + parent.getId())
                .orElse("0");
    }

    private void validateTenantOrg(String tenantId, Long orgId) {
        boolean exists = orgRepository.findByTenantId(tenantId).stream()
                .map(SysOrg::getId)
                .filter(Objects::nonNull)
                .anyMatch(orgId::equals);
        if (!exists) {
            throw com.zhyc.system.support.SystemServiceValidation.businessFailure("组织主键不属于当前租户：" + orgId);
        }
    }

    private String normalizeStatus(String status) {
        String normalizedStatus = requireText(status, "组织状态不能为空").toLowerCase();
        if (!"enabled".equals(normalizedStatus) && !"disabled".equals(normalizedStatus)) {
            throw com.zhyc.system.support.SystemServiceValidation.businessFailure("组织状态仅支持 enabled 或 disabled");
        }
        return normalizedStatus;
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
