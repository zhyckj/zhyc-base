/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.signature.repository;

import com.zhyc.openapi.signature.domain.OpenApiSignaturePolicy;
import com.zhyc.openapi.signature.mapper.OpenApiSignaturePolicyMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;

/**
 * 基于 MyBatis 的开放 API 签名策略仓储实现。
 */
@Repository
public class MyBatisOpenApiSignaturePolicyRepository implements OpenApiSignaturePolicyRepository {

    /** 签名策略 Mapper。 */
    private final OpenApiSignaturePolicyMapper signaturePolicyMapper;

    /**
     * 创建签名策略仓储实现。
     *
     * @param signaturePolicyMapper 签名策略 Mapper
     */
    public MyBatisOpenApiSignaturePolicyRepository(OpenApiSignaturePolicyMapper signaturePolicyMapper) {
        this.signaturePolicyMapper = Objects.requireNonNull(signaturePolicyMapper, "签名策略 Mapper 不能为空");
    }

    @Override
    public List<OpenApiSignaturePolicy> findByTenantIdAndAppCode(String tenantId, String appCode) {
        return signaturePolicyMapper.selectByTenantIdAndAppCode(tenantId, appCode);
    }

    @Override
    public void save(OpenApiSignaturePolicy policy) {
        signaturePolicyMapper.upsert(policy);
    }
}
