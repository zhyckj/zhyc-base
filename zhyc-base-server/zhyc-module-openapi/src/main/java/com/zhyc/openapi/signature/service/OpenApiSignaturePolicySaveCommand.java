/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.signature.service;

/**
 * 开放 API 签名策略保存命令。
 */
public class OpenApiSignaturePolicySaveCommand {

    /** 租户业务编码。 */
    private String tenantId;
    /** 开发者应用编码。 */
    private String appCode;
    /** 签名算法。 */
    private String algorithm;
    /** 客户端时间戳允许偏差秒数。 */
    private int timestampToleranceSeconds;
    /** nonce 防重放有效期秒数。 */
    private int nonceTtlSeconds;
    /** 是否要求请求体参与摘要，1 是 0 否。 */
    private int requireBodyHash;
    /** 签名策略状态。 */
    private String status;

    /**
     * 创建开放 API 签名策略保存命令。
     *
     * @param tenantId 租户业务编码
     * @param appCode 开发者应用编码
     * @param algorithm 签名算法
     * @param timestampToleranceSeconds 客户端时间戳允许偏差秒数
     * @param nonceTtlSeconds nonce 防重放有效期秒数
     * @param requireBodyHash 是否要求请求体参与摘要，1 是 0 否
     * @param status 签名策略状态
     */
    public OpenApiSignaturePolicySaveCommand(String tenantId, String appCode, String algorithm,
                                             int timestampToleranceSeconds, int nonceTtlSeconds,
                                             int requireBodyHash, String status) {
        this.tenantId = tenantId;
        this.appCode = appCode;
        this.algorithm = algorithm;
        this.timestampToleranceSeconds = timestampToleranceSeconds;
        this.nonceTtlSeconds = nonceTtlSeconds;
        this.requireBodyHash = requireBodyHash;
        this.status = status;
    }

    public String getTenantId() {
        return tenantId;
    }

    public String getAppCode() {
        return appCode;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public int getTimestampToleranceSeconds() {
        return timestampToleranceSeconds;
    }

    public int getNonceTtlSeconds() {
        return nonceTtlSeconds;
    }

    public int getRequireBodyHash() {
        return requireBodyHash;
    }

    public String getStatus() {
        return status;
    }
}
