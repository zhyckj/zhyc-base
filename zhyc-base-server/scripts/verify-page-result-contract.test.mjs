/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

import assert from 'node:assert/strict';
import { mkdirSync, mkdtempSync, writeFileSync } from 'node:fs';
import { tmpdir } from 'node:os';
import { dirname, join, resolve } from 'node:path';
import { spawnSync } from 'node:child_process';

const scriptPath = resolve(process.cwd(), 'scripts/verify-page-result-contract.mjs');

const failedRoot = mkdtempSync(join(tmpdir(), 'zhyc-page-result-fail-'));
writeFile(failedRoot, 'zhyc-common/src/main/java/com/zhyc/common/api/PageResult.java', `
package com.zhyc.common.api;

/**
 * 分页响应。
 */
public class PageResult<T> {
}
`);
writeFile(failedRoot, 'zhyc-module-file/src/main/java/com/zhyc/file/object/service/FileObjectService.java', `
package com.zhyc.file.object.service;

/**
 * 文件对象业务服务。
 */
public interface FileObjectService {
  FileObjectPageResponse listFiles(FileObjectQuery query);
}
`);
writeFile(failedRoot, 'zhyc-module-file/src/main/java/com/zhyc/file/object/service/FileObjectPageResponse.java', `
package com.zhyc.file.object.service;

/**
 * 文件对象分页响应。
 */
public record FileObjectPageResponse(long total) {
}
`);
writeFile(failedRoot, 'zhyc-module-message/src/main/java/com/zhyc/message/inbox/service/MsgMessageService.java', `
package com.zhyc.message.inbox.service;

/**
 * 消息业务服务。
 */
public interface MsgMessageService {
  MsgMessagePageResponse listMessages(MsgMessageQuery query);
}
`);
writeFile(failedRoot, 'zhyc-module-message/src/main/java/com/zhyc/message/inbox/service/MsgMessagePageResponse.java', `
package com.zhyc.message.inbox.service;

/**
 * 消息分页响应。
 */
public record MsgMessagePageResponse(long total) {
}
`);
writeFile(failedRoot, 'zhyc-module-purchase/src/main/java/com/zhyc/purchase/request/service/PurRequestCommandService.java', `
package com.zhyc.purchase.request.service;

/**
 * 采购申请命令服务。
 */
public interface PurRequestCommandService {
  PurRequestPageResponse list(PurRequestQuery query);
}
`);
writeFile(failedRoot, 'zhyc-module-purchase/src/main/java/com/zhyc/purchase/request/service/PurRequestPageResponse.java', `
package com.zhyc.purchase.request.service;

/**
 * 采购申请分页响应。
 */
public class PurRequestPageResponse {
}
`);
writeFile(failedRoot, 'zhyc-module-purchase/src/main/java/com/zhyc/purchase/order/service/PurOrderCommandService.java', `
package com.zhyc.purchase.order.service;

/**
 * 采购订单命令服务。
 */
public interface PurOrderCommandService {
  PurOrderPageResponse list(PurOrderQuery query);
}
`);
writeFile(failedRoot, 'zhyc-module-purchase/src/main/java/com/zhyc/purchase/order/service/PurOrderPageResponse.java', `
package com.zhyc.purchase.order.service;

/**
 * 采购订单分页响应。
 */
public class PurOrderPageResponse {
}
`);

const failedResult = spawnSync('node', [scriptPath, failedRoot], {
  cwd: process.cwd(),
  encoding: 'utf8',
});

assert.notEqual(failedResult.status, 0, '分页契约缺失或模块私有分页响应必须触发门禁失败');
assert.match(failedResult.stderr, /PageResult/, '应报告公共分页契约不完整');
assert.match(failedResult.stderr, /FileObjectPageResponse/, '应报告文件模块私有分页响应残留');
assert.match(failedResult.stderr, /MsgMessagePageResponse/, '应报告消息模块私有分页响应残留');
assert.match(failedResult.stderr, /PurRequestPageResponse/, '应报告采购申请私有分页响应残留');
assert.match(failedResult.stderr, /PurOrderPageResponse/, '应报告采购订单私有分页响应残留');

const passedRoot = mkdtempSync(join(tmpdir(), 'zhyc-page-result-pass-'));
writeFile(passedRoot, 'zhyc-common/src/main/java/com/zhyc/common/api/PageResult.java', `
package com.zhyc.common.api;

import java.util.List;

/**
 * 通用分页响应对象。
 */
public class PageResult<T> {
  private static final int MAX_PAGE_SIZE = 100;
  private final long total;
  private final int pageNo;
  private final int pageSize;
  private final List<T> records;

  public static <T> PageResult<T> of(long total, int pageNo, int pageSize, List<T> records) {
    return null;
  }

  public List<T> getRecords() {
    return List.copyOf(records);
  }
}
`);
writeFile(passedRoot, 'zhyc-module-file/src/main/java/com/zhyc/file/object/service/FileObjectService.java', `
package com.zhyc.file.object.service;

import com.zhyc.common.api.PageResult;

/**
 * 文件对象业务服务。
 */
public interface FileObjectService {
  PageResult<FileObjectResponse> listFiles(FileObjectQuery query);
}
`);
writeFile(passedRoot, 'zhyc-module-file/src/main/java/com/zhyc/file/object/controller/FileObjectController.java', `
package com.zhyc.file.object.controller;

import com.zhyc.common.api.ApiResult;
import com.zhyc.common.api.PageResult;
import com.zhyc.file.object.service.FileObjectResponse;

/**
 * 文件对象管理接口。
 */
public class FileObjectController {
  public ApiResult<PageResult<FileObjectResponse>> listFiles() {
    return null;
  }
}
`);
writeFile(passedRoot, 'zhyc-module-message/src/main/java/com/zhyc/message/inbox/service/MsgMessageService.java', `
package com.zhyc.message.inbox.service;

import com.zhyc.common.api.PageResult;

/**
 * 消息业务服务。
 */
public interface MsgMessageService {
  PageResult<MsgMessageResponse> listMessages(MsgMessageQuery query);
}
`);
writeFile(passedRoot, 'zhyc-module-message/src/main/java/com/zhyc/message/inbox/controller/MsgMessageController.java', `
package com.zhyc.message.inbox.controller;

import com.zhyc.common.api.ApiResult;
import com.zhyc.common.api.PageResult;
import com.zhyc.message.inbox.service.MsgMessageResponse;

/**
 * 消息管理接口。
 */
public class MsgMessageController {
  public ApiResult<PageResult<MsgMessageResponse>> listMessages() {
    return null;
  }
}
`);
writeFile(passedRoot, 'zhyc-module-purchase/src/main/java/com/zhyc/purchase/request/service/PurRequestCommandService.java', `
package com.zhyc.purchase.request.service;

import com.zhyc.common.api.PageResult;

/**
 * 采购申请命令服务。
 */
public interface PurRequestCommandService {
  PageResult<PurRequestStatusResponse> list(PurRequestQuery query);
}
`);
writeFile(passedRoot, 'zhyc-module-purchase/src/main/java/com/zhyc/purchase/request/controller/PurRequestAdminController.java', `
package com.zhyc.purchase.request.controller;

import com.zhyc.common.api.ApiResult;
import com.zhyc.common.api.PageResult;
import com.zhyc.purchase.request.service.PurRequestStatusResponse;

/**
 * 采购申请后台管理接口。
 */
public class PurRequestAdminController {
  public ApiResult<PageResult<PurRequestStatusResponse>> list() {
    return null;
  }
}
`);
writeFile(passedRoot, 'zhyc-module-purchase/src/main/java/com/zhyc/purchase/order/service/PurOrderCommandService.java', `
package com.zhyc.purchase.order.service;

import com.zhyc.common.api.PageResult;

/**
 * 采购订单命令服务。
 */
public interface PurOrderCommandService {
  PageResult<PurOrderResponse> list(PurOrderQuery query);
}
`);
writeFile(passedRoot, 'zhyc-module-purchase/src/main/java/com/zhyc/purchase/order/controller/PurOrderAdminController.java', `
package com.zhyc.purchase.order.controller;

import com.zhyc.common.api.ApiResult;
import com.zhyc.common.api.PageResult;
import com.zhyc.purchase.order.service.PurOrderResponse;

/**
 * 采购订单后台管理接口。
 */
public class PurOrderAdminController {
  public ApiResult<PageResult<PurOrderResponse>> list() {
    return null;
  }
}
`);

const passedResult = spawnSync('node', [scriptPath, passedRoot], {
  cwd: process.cwd(),
  encoding: 'utf8',
});

assert.equal(passedResult.status, 0, passedResult.stderr || passedResult.stdout);
assert.match(passedResult.stdout, /统一分页响应契约通过/);

/**
 * 写入测试文件。
 *
 * @param root 测试工程根目录
 * @param file 文件相对路径
 * @param content 文件内容
 */
function writeFile(root, file, content) {
  const absolutePath = join(root, file);
  mkdirSync(dirname(absolutePath), { recursive: true });
  writeFileSync(absolutePath, content.trim());
}
