/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.common.secret;

/**
 * 密钥解析器契约。
 *
 * <p>系统模块、开放 API 和低代码模块后续都应复用该契约完成密钥值解析，避免各模块自行定义
 * 不一致的密钥读取方式。</p>
 */
public interface SecretResolver {

    /**
     * 根据密钥编码解析密钥值。
     *
     * <p>具体实现负责完成租户隔离、权限校验、密文解密或外部密钥源访问，契约层只约束输入输出。</p>
     *
     * @param code 密钥编码
     * @return 解析后的密钥值
     */
    String resolve(String code);

    /**
     * 根据密钥引用解析密钥值。
     *
     * <p>默认先从引用中提取密钥编码，再交由实现类处理，保证 {@code secret:<secretCode>} 形式稳定可复用。</p>
     *
     * @param reference 密钥引用
     * @return 解析后的密钥值
     */
    default String resolve(SecretReference reference) {
        if (reference == null) {
            throw new IllegalArgumentException("密钥引用不能为空");
        }
        return resolve(reference.getCode());
    }
}
