/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.common.audit;

/**
 * 审计事件基础数据。
 *
 * <p>用于在业务模块和审计实现之间传递最小审计信息，不绑定具体存储或消息中间件。</p>
 */
public class AuditEvent {

    /** 事件所属租户 ID。 */
    private String tenantId;
    /** 触发事件的用户 ID。 */
    private String userId;
    /** 操作类型或动作名称。 */
    private String operation;
    /** 被操作的资源标识或资源类型。 */
    private String resource;
    /** 操作是否成功。 */
    private boolean success;
    /** 事件发生时间戳，单位为毫秒。 */
    private long timestamp;
    /** 操作结果或失败原因说明。 */
    private String message;

    /**
     * 创建空审计事件，供序列化框架或手动赋值场景使用。
     */
    public AuditEvent() {
    }

    /**
     * 创建完整审计事件。
     *
     * @param tenantId 事件所属租户 ID
     * @param userId 触发事件的用户 ID
     * @param operation 操作类型或动作名称
     * @param resource 被操作的资源标识或资源类型
     * @param success 操作是否成功
     * @param timestamp 事件发生时间戳，单位为毫秒
     * @param message 操作结果或失败原因说明
     */
    public AuditEvent(String tenantId, String userId, String operation, String resource,
                      boolean success, long timestamp, String message) {
        this.tenantId = tenantId;
        this.userId = userId;
        this.operation = operation;
        this.resource = resource;
        this.success = success;
        this.timestamp = timestamp;
        this.message = message;
    }

    /**
     * 返回事件所属租户 ID。
     *
     * @return 租户 ID
     */
    public String getTenantId() {
        return tenantId;
    }

    /**
     * 设置事件所属租户 ID。
     *
     * @param tenantId 租户 ID
     */
    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    /**
     * 返回触发事件的用户 ID。
     *
     * @return 用户 ID
     */
    public String getUserId() {
        return userId;
    }

    /**
     * 设置触发事件的用户 ID。
     *
     * @param userId 用户 ID
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * 返回操作类型或动作名称。
     *
     * @return 操作名称
     */
    public String getOperation() {
        return operation;
    }

    /**
     * 设置操作类型或动作名称。
     *
     * @param operation 操作名称
     */
    public void setOperation(String operation) {
        this.operation = operation;
    }

    /**
     * 返回被操作的资源标识或资源类型。
     *
     * @return 资源标识或资源类型
     */
    public String getResource() {
        return resource;
    }

    /**
     * 设置被操作的资源标识或资源类型。
     *
     * @param resource 资源标识或资源类型
     */
    public void setResource(String resource) {
        this.resource = resource;
    }

    /**
     * 返回操作是否成功。
     *
     * @return 成功返回 {@code true}，失败返回 {@code false}
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * 设置操作是否成功。
     *
     * @param success 成功标记
     */
    public void setSuccess(boolean success) {
        this.success = success;
    }

    /**
     * 返回事件发生时间戳。
     *
     * @return 毫秒时间戳
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * 设置事件发生时间戳。
     *
     * @param timestamp 毫秒时间戳
     */
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * 返回操作结果或失败原因说明。
     *
     * @return 结果说明
     */
    public String getMessage() {
        return message;
    }

    /**
     * 设置操作结果或失败原因说明。
     *
     * @param message 结果说明
     */
    public void setMessage(String message) {
        this.message = message;
    }
}
