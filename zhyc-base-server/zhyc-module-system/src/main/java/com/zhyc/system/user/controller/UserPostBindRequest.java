/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.user.controller;

import java.util.List;

/**
 * 用户岗位绑定请求。
 */
public class UserPostBindRequest {

    /** 岗位绑定项列表。 */
    private List<UserPostBindItemRequest> posts;

    /**
     * 返回岗位绑定项列表。
     *
     * @return 岗位绑定项列表
     */
    public List<UserPostBindItemRequest> getPosts() {
        return posts;
    }

    /**
     * 设置岗位绑定项列表。
     *
     * @param posts 岗位绑定项列表
     */
    public void setPosts(List<UserPostBindItemRequest> posts) {
        this.posts = posts;
    }
}
