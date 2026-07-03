/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

import { existsSync, readFileSync } from 'node:fs';
import { fileURLToPath } from 'node:url';
import { resolve } from 'node:path';

const rootDir = resolve(fileURLToPath(new URL('..', import.meta.url)));

const requiredSnippets = [
  ['zhyc-module-purchase/src/main/java/com/zhyc/purchase/request/repository/PurRequestRepository.java', 'void save(PurRequest purRequest);'],
  ['zhyc-module-purchase/src/main/java/com/zhyc/purchase/request/repository/PurRequestRepository.java', 'void updateSubmitted'],
  ['zhyc-module-purchase/src/main/java/com/zhyc/purchase/request/repository/PurRequestRepository.java', 'long countByTenantIdAndProcessStatus'],
  ['zhyc-module-purchase/src/main/java/com/zhyc/purchase/request/repository/PurRequestRepository.java', 'List<PurRequest> findPageByTenantIdAndProcessStatus'],
  ['zhyc-module-purchase/src/main/java/com/zhyc/purchase/request/repository/PurRequestRepository.java', 'void updateProcessStatus'],
  ['zhyc-module-purchase/src/main/java/com/zhyc/purchase/request/repository/MyBatisPurRequestRepository.java', 'public void save(PurRequest purRequest)'],
  ['zhyc-module-purchase/src/main/java/com/zhyc/purchase/request/repository/MyBatisPurRequestRepository.java', 'public void updateSubmitted'],
  ['zhyc-module-purchase/src/main/java/com/zhyc/purchase/request/repository/MyBatisPurRequestRepository.java', 'public long countByTenantIdAndProcessStatus'],
  ['zhyc-module-purchase/src/main/java/com/zhyc/purchase/request/repository/MyBatisPurRequestRepository.java', 'public List<PurRequest> findPageByTenantIdAndProcessStatus'],
  ['zhyc-module-purchase/src/main/java/com/zhyc/purchase/request/repository/MyBatisPurRequestRepository.java', 'public void updateProcessStatus'],
  ['zhyc-module-purchase/src/main/java/com/zhyc/purchase/request/controller/PurRequestAdminController.java', 'ERROR_CREATE_REQUEST_REQUIRED'],
  ['zhyc-module-purchase/src/main/java/com/zhyc/purchase/request/controller/PurRequestAdminController.java', 'ZHYC_PURCHASE_REQUEST_CREATE_REQUEST_REQUIRED'],
  ['zhyc-module-purchase/src/main/java/com/zhyc/purchase/request/controller/PurRequestAdminController.java', 'new BusinessException(ERROR_CREATE_REQUEST_REQUIRED, "采购申请创建请求不能为空")'],
  ['zhyc-module-purchase/src/main/java/com/zhyc/purchase/request/controller/PurRequestAdminController.java', '采购申请创建请求不能为空'],
  ['zhyc-module-purchase/src/main/java/com/zhyc/purchase/order/controller/PurOrderAdminController.java', 'ERROR_CREATE_REQUEST_REQUIRED'],
  ['zhyc-module-purchase/src/main/java/com/zhyc/purchase/order/controller/PurOrderAdminController.java', 'ZHYC_PURCHASE_ORDER_CREATE_REQUEST_REQUIRED'],
  ['zhyc-module-purchase/src/main/java/com/zhyc/purchase/order/controller/PurOrderAdminController.java', 'new BusinessException(ERROR_CREATE_REQUEST_REQUIRED, "采购订单创建请求不能为空")'],
  ['zhyc-module-purchase/src/main/java/com/zhyc/purchase/order/controller/PurOrderAdminController.java', '采购订单创建请求不能为空'],
  ['zhyc-module-purchase/src/main/java/com/zhyc/purchase/order/controller/PurOrderOpenApiController.java', 'ZHYC_PURCHASE_ORDER_OPENAPI_GATEWAY_CONTEXT_REQUIRED'],
  ['zhyc-module-purchase/src/main/java/com/zhyc/purchase/order/controller/PurOrderOpenApiController.java', '@RequestMapping("/openapi/v1/purchase/orders")'],
  ['zhyc-module-purchase/src/main/java/com/zhyc/purchase/order/controller/PurOrderOpenApiController.java', '@GetMapping("/{orderNo}")'],
  ['zhyc-module-purchase/src/main/java/com/zhyc/purchase/order/controller/PurOrderOpenApiController.java', '开放 API 网关上下文不能为空'],
  ['zhyc-module-purchase/src/test/java/com/zhyc/purchase/request/PurRequestAdminControllerContractTest.java', 'shouldRejectNullCreateCommand'],
  ['zhyc-module-purchase/src/test/java/com/zhyc/purchase/request/PurRequestAdminControllerContractTest.java', 'ZHYC_PURCHASE_REQUEST_CREATE_REQUEST_REQUIRED'],
  ['zhyc-module-purchase/src/test/java/com/zhyc/purchase/request/PurRequestAdminControllerContractTest.java', '采购申请创建请求不能为空'],
  ['zhyc-module-purchase/src/test/java/com/zhyc/purchase/order/PurOrderAdminControllerContractTest.java', 'shouldRejectNullCreateCommand'],
  ['zhyc-module-purchase/src/test/java/com/zhyc/purchase/order/PurOrderAdminControllerContractTest.java', 'ZHYC_PURCHASE_ORDER_CREATE_REQUEST_REQUIRED'],
  ['zhyc-module-purchase/src/test/java/com/zhyc/purchase/order/PurOrderAdminControllerContractTest.java', '采购订单创建请求不能为空'],
  ['zhyc-module-purchase/src/test/java/com/zhyc/purchase/order/PurOrderOpenApiControllerContractTest.java', 'shouldExposeOpenApiOrderDetailRoute'],
  ['zhyc-module-purchase/src/test/java/com/zhyc/purchase/order/PurOrderOpenApiControllerContractTest.java', 'shouldRejectOpenApiOrderCallWithoutGatewayContext'],
  ['zhyc-module-purchase/src/main/java/com/zhyc/purchase/request/mapper/PurRequestSqlProvider.java', "AND process_status = 'DRAFT'"],
  ['zhyc-module-purchase/src/main/java/com/zhyc/purchase/request/mapper/PurRequestSqlProvider.java', "AND process_status = 'APPROVING'"],
  ['zhyc-module-purchase/src/test/java/com/zhyc/purchase/request/PurRequestSqlProviderTest.java', "AND process_status = 'DRAFT'"],
  ['zhyc-module-purchase/src/test/java/com/zhyc/purchase/request/PurRequestSqlProviderTest.java', "AND process_status = 'APPROVING'"],
  ['zhyc-module-purchase/src/main/resources/db/V1__purchase_core.sql', 'INSERT INTO openapi_catalog'],
  ['zhyc-module-purchase/src/main/resources/db/V1__purchase_core.sql', 'INSERT INTO openapi_version'],
  ['zhyc-module-purchase/src/main/resources/db/V1__purchase_core.sql', "'purchase-request-status'"],
  ['zhyc-module-purchase/src/main/resources/db/V1__purchase_core.sql', "'http://zhyc-platform-app/openapi/v1/purchase/requests/{requestNo}/status'"],
  ['zhyc-module-purchase/src/main/resources/db/V1__purchase_core.sql', "'purchase-order-detail'"],
  ['zhyc-module-purchase/src/main/resources/db/V1__purchase_core.sql', "'http://zhyc-platform-app/openapi/v1/purchase/orders/{orderNo}'"],
  ['zhyc-module-purchase/src/test/java/com/zhyc/purchase/request/PurRequestSchemaTest.java', 'shouldRegisterPurRequestStatusOpenApiRoute'],
  ['zhyc-module-purchase/src/test/java/com/zhyc/purchase/order/PurOrderSchemaTest.java', 'shouldRegisterPurOrderDetailOpenApiRoute'],
];

const forbiddenSnippets = [
  ['zhyc-module-purchase/src/main/java/com/zhyc/purchase/request/repository/PurRequestRepository.java', 'UnsupportedOperationException'],
  ['zhyc-module-purchase/src/main/java/com/zhyc/purchase/request/repository/PurRequestRepository.java', '当前仓储未实现'],
  ['zhyc-module-purchase/src/main/java/com/zhyc/purchase/request/repository/PurRequestRepository.java', 'default void save'],
  ['zhyc-module-purchase/src/main/java/com/zhyc/purchase/request/repository/PurRequestRepository.java', 'default long countByTenantIdAndProcessStatus'],
  ['zhyc-module-purchase/src/main/java/com/zhyc/purchase/request/controller/PurRequestAdminController.java', 'throw new IllegalArgumentException("采购申请创建请求不能为空")'],
  ['zhyc-module-purchase/src/main/java/com/zhyc/purchase/order/controller/PurOrderAdminController.java', 'throw new IllegalArgumentException("采购订单创建请求不能为空")'],
  ['zhyc-module-purchase/src/main/java/com/zhyc/purchase/request/mapper/PurRequestSqlProvider.java', "process_instance_id = #{processInstanceId},\n            process_status = #{processStatus},\n            submitted_at = #{submittedAt},\n            updated_at = CURRENT_TIMESTAMP\n        WHERE tenant_id = #{tenantId}\n          AND request_no = #{requestNo}\n          AND process_status = 'APPROVING'"],
  ['zhyc-module-purchase/src/main/java/com/zhyc/purchase/request/mapper/PurRequestSqlProvider.java', "public String selectByTenantIdAndRequestNo() {\n    return \"\"\"\n        SELECT id,\n               tenant_id AS tenantId,\n               request_no AS requestNo,\n               request_title AS requestTitle,\n               applicant_id AS applicantId,\n               org_id AS orgId,\n               total_amount AS totalAmount,\n               request_reason AS requestReason,\n               process_status AS processStatus,\n               process_instance_id AS processInstanceId,\n               submitted_at AS submittedAt,\n               created_at AS createdAt,\n               updated_at AS updatedAt\n        FROM pur_request\n        WHERE tenant_id = #{tenantId}\n          AND request_no = #{requestNo}\n          AND process_status = 'DRAFT'"],
];

const missingSnippets = requiredSnippets.filter(([file, snippet]) => {
  const path = resolve(rootDir, file);
  return !existsSync(path) || !readFileSync(path, 'utf8').includes(snippet);
});

const forbiddenHits = forbiddenSnippets.filter(([file, snippet]) => {
  const path = resolve(rootDir, file);
  return existsSync(path) && readFileSync(path, 'utf8').includes(snippet);
});

if (missingSnippets.length > 0 || forbiddenHits.length > 0) {
  console.error('采购首期契约校验失败。');
  for (const [file, snippet] of missingSnippets) {
    console.error(`缺少关键内容: ${file} -> ${snippet}`);
  }
  for (const [file, snippet] of forbiddenHits) {
    console.error(`存在过期内容: ${file} -> ${snippet}`);
  }
  process.exit(1);
}

console.log('采购首期契约校验通过。');
