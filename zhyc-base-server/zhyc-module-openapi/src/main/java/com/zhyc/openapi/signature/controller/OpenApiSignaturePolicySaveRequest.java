/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.signature.controller;

/**
 * 开放 API 签名策略保存请求。
 */
public class OpenApiSignaturePolicySaveRequest {

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

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getAppCode() {
        return appCode;
    }

    public void setAppCode(String appCode) {
        this.appCode = appCode;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    public int getTimestampToleranceSeconds() {
        return timestampToleranceSeconds;
    }

    public void setTimestampToleranceSeconds(int timestampToleranceSeconds) {
        this.timestampToleranceSeconds = timestampToleranceSeconds;
    }

    public int getNonceTtlSeconds() {
        return nonceTtlSeconds;
    }

    public void setNonceTtlSeconds(int nonceTtlSeconds) {
        this.nonceTtlSeconds = nonceTtlSeconds;
    }

    public int getRequireBodyHash() {
        return requireBodyHash;
    }

    public void setRequireBodyHash(int requireBodyHash) {
        this.requireBodyHash = requireBodyHash;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
