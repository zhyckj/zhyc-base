/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.common.api;

import java.util.List;

/**
 * 通用分页响应对象。
 *
 * <p>用于后台管理端、移动端和开放 API 返回统一分页结构，字段固定为
 * {@code total/pageNo/pageSize/records}。</p>
 *
 * @param <T> 分页记录类型
 */
public class PageResult<T> {

    /** 单页最大记录数，避免调用方传入过大的分页参数。 */
    private static final int MAX_PAGE_SIZE = 100;

    /** 总记录数，不能小于 0。 */
    private final long total;
    /** 当前页码，从 1 开始。 */
    private final int pageNo;
    /** 每页记录数，范围为 1 到 100。 */
    private final int pageSize;
    /** 当前页记录，只读副本。 */
    private final List<T> records;

    /**
     * 创建分页响应对象。
     *
     * @param total 总记录数
     * @param pageNo 当前页码
     * @param pageSize 每页记录数
     * @param records 当前页记录
     */
    private PageResult(long total, int pageNo, int pageSize, List<T> records) {
        this.total = Math.max(total, 0);
        this.pageNo = Math.max(pageNo, 1);
        this.pageSize = Math.min(Math.max(pageSize, 1), MAX_PAGE_SIZE);
        this.records = records == null ? List.of() : List.copyOf(records);
    }

    /**
     * 创建分页响应对象。
     *
     * @param total 总记录数
     * @param pageNo 当前页码
     * @param pageSize 每页记录数
     * @param records 当前页记录
     * @param <T> 分页记录类型
     * @return 通用分页响应对象
     */
    public static <T> PageResult<T> of(long total, int pageNo, int pageSize, List<T> records) {
        return new PageResult<>(total, pageNo, pageSize, records);
    }

    /**
     * 返回总记录数。
     *
     * @return 总记录数
     */
    public long getTotal() {
        return total;
    }

    /**
     * 返回当前页码。
     *
     * @return 当前页码
     */
    public int getPageNo() {
        return pageNo;
    }

    /**
     * 返回每页记录数。
     *
     * @return 每页记录数
     */
    public int getPageSize() {
        return pageSize;
    }

    /**
     * 返回当前页记录。
     *
     * @return 当前页记录只读副本
     */
    public List<T> getRecords() {
        return records;
    }
}
