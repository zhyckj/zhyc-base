/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.signature.mapper;

import com.zhyc.openapi.signature.domain.OpenApiSignaturePolicy;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.SelectProvider;

import java.util.List;

/**
 * 开放 API 签名策略 MyBatis Mapper。
 */
@Mapper
public interface OpenApiSignaturePolicyMapper {

    /**
     * 查询租户指定应用的签名策略列表。
     *
     * @param tenantId 租户业务编码
     * @param appCode 开发者应用编码
     * @return 签名策略列表
     */
    @SelectProvider(type = OpenApiSignaturePolicySqlProvider.class, method = "selectByTenantIdAndAppCode")
    List<OpenApiSignaturePolicy> selectByTenantIdAndAppCode(@Param("tenantId") String tenantId,
                                                            @Param("appCode") String appCode);

    /**
     * 保存或更新签名策略。
     *
     * @param policy 签名策略领域对象
     */
    @InsertProvider(type = OpenApiSignaturePolicySqlProvider.class, method = "upsert")
    void upsert(OpenApiSignaturePolicy policy);
}
