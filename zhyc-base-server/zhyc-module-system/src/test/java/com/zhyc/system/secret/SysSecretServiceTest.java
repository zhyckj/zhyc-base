/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.secret;

import com.zhyc.common.exception.BusinessException;
import com.zhyc.common.tenant.TenantContext;
import com.zhyc.system.secret.repository.SysSecretRepository;
import com.zhyc.system.secret.service.SystemSecretCipherService;
import com.zhyc.system.secret.service.SystemSecretResolver;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 系统密钥中心业务服务测试。
 */
class SysSecretServiceTest {

    /**
     * 验证保存密钥时会裁剪字段、立即加密并保留掩码。
     *
     * @throws Exception 反射创建服务或实体失败时抛出
     */
    @Test
    void shouldSaveSecretWithEncryptedCipherAndMaskedValue() throws Exception {
        RecordingRepositoryHandler handler = new RecordingRepositoryHandler();
        Object repository = newRepositoryProxy(handler);
        Object service = newService(repository);
        LocalDateTime expireAt = LocalDateTime.of(2099, 12, 31, 23, 59, 59);

        invoke(service, "saveSecret", newSaveCommand(null, " tenant_a ", " db-main ", " 主数据库密码 ",
                " db_password ", " super-secret-1234 ", " enabled ", expireAt));

        assertEquals("tenant_a", getField(handler.insertedSecret, "tenantId"));
        assertEquals("db-main", getField(handler.insertedSecret, "secretCode"));
        assertEquals("主数据库密码", getField(handler.insertedSecret, "secretName"));
        assertEquals("db_password", getField(handler.insertedSecret, "secretKind"));
        assertEquals("enabled", getField(handler.insertedSecret, "status"));
        assertEquals(expireAt, getField(handler.insertedSecret, "expireAt"));
        assertNotNull(getField(handler.insertedSecret, "secretCipher"));
        assertNotNull(getField(handler.insertedSecret, "secretMask"));
        assertFalse("super-secret-1234".equals(getField(handler.insertedSecret, "secretCipher")));
        assertFalse("super-secret-1234".equals(getField(handler.insertedSecret, "secretMask")));
        assertEquals(1, handler.insertCount);
    }

    /**
     * 验证同一租户内密钥编码重复时会被拒绝。
     *
     * @throws Exception 反射创建服务或实体失败时抛出
     */
    @Test
    void shouldRejectDuplicateSecretCodeWithinTenant() throws Exception {
        RecordingRepositoryHandler handler = new RecordingRepositoryHandler();
        handler.findByTenantIdAndSecretCodeResult = Optional.of(newSecret(9L, "tenant_a", "db-main",
                "旧密钥", "db_password", "cipher-old", "mask-old", "enabled",
                null, null, LocalDateTime.now(), LocalDateTime.now()));
        Object repository = newRepositoryProxy(handler);
        Object service = newService(repository);

        InvocationTargetException exception = assertThrows(InvocationTargetException.class,
                () -> invoke(service, "saveSecret", newSaveCommand(null, "tenant_a", "db-main", "主数据库密码",
                        "db_password", "super-secret-1234", "enabled", null)));

        assertTrue(exception.getCause() instanceof BusinessException);
        BusinessException businessException = (BusinessException) exception.getCause();
        assertEquals("ZHYC_SYSTEM_SECRET_CODE_DUPLICATE", businessException.getCode());
        assertTrue(businessException.getMessage().contains("db-main"));
        assertEquals(0, handler.insertCount);
    }

    /**
     * 验证轮换密钥会更新轮换时间并写入新密文。
     *
     * @throws Exception 反射创建服务或实体失败时抛出
     */
    @Test
    void shouldRotateSecretAndUpdateLastRotatedAt() throws Exception {
        RecordingRepositoryHandler handler = new RecordingRepositoryHandler();
        handler.findByTenantIdAndIdResult = Optional.of(newSecret(11L, "tenant_a", "db-main",
                "主数据库密码", "db_password", "cipher-old", "mask-old", "enabled",
                LocalDateTime.of(2099, 12, 31, 23, 59, 59), null,
                LocalDateTime.of(2026, 6, 27, 10, 0), LocalDateTime.of(2026, 6, 27, 10, 0)));
        Object repository = newRepositoryProxy(handler);
        Object service = newService(repository);
        LocalDateTime before = LocalDateTime.now();

        invoke(service, "rotateSecret", "tenant_a", 11L, "  rotated-secret-2027  ",
                LocalDateTime.of(2099, 12, 31, 23, 59, 59));

        assertEquals(1, handler.updateCount);
        assertEquals(11L, getField(handler.updatedSecret, "id"));
        assertEquals("tenant_a", getField(handler.updatedSecret, "tenantId"));
        assertEquals("db-main", getField(handler.updatedSecret, "secretCode"));
        assertEquals("db_password", getField(handler.updatedSecret, "secretKind"));
        assertEquals("enabled", getField(handler.updatedSecret, "status"));
        assertNotNull(getField(handler.updatedSecret, "secretCipher"));
        assertNotNull(getField(handler.updatedSecret, "secretMask"));
        assertNotNull(getField(handler.updatedSecret, "lastRotatedAt"));
        assertTrue(((LocalDateTime) getField(handler.updatedSecret, "lastRotatedAt")).isAfter(before.minusSeconds(1)));
        assertFalse("cipher-old".equals(getField(handler.updatedSecret, "secretCipher")));
    }

    /**
     * 验证查询选项默认只返回启用的数据源兼容密钥，并保留密钥引用，不暴露密钥值。
     *
     * @throws Exception 反射创建服务或实体失败时抛出
     */
    @Test
    void shouldListDefaultSelectableOptionsWithoutCipher() throws Exception {
        RecordingRepositoryHandler handler = new RecordingRepositoryHandler();
        handler.findSelectableSecretsResult = List.of(
                newSecret(21L, "tenant_a", "db-main", "主数据库密码", "db_password", "cipher-a", "mask-a",
                        "enabled", null, null, LocalDateTime.of(2026, 6, 27, 9, 0),
                        LocalDateTime.of(2026, 6, 27, 9, 0)));
        Object repository = newRepositoryProxy(handler);
        Object service = newService(repository);

        List<?> options = (List<?>) invoke(service, "listOptions", "tenant_a", null, "enabled");

        assertEquals("tenant_a", handler.lastTenantId);
        assertEquals("enabled", handler.lastStatus);
        assertEquals(1, options.size());
        Object first = options.get(0);
        assertEquals("secret:db-main", invokeGetter(first, "getSecretRef"));
        assertEquals("enabled", invokeGetter(first, "getStatus"));
        assertEquals("db_password", invokeGetter(first, "getSecretKind"));
    }

    /**
     * 验证指定 API 密钥类型时会把过滤条件传入仓储，供 AI 供应商选择 API 密钥。
     *
     * @throws Exception 反射创建服务或实体失败时抛出
     */
    @Test
    void shouldListApiSecretOptionsWhenSecretKindSpecified() throws Exception {
        RecordingRepositoryHandler handler = new RecordingRepositoryHandler();
        handler.findSelectableSecretsResult = List.of(
                newSecret(22L, "tenant_a", "deepseek", "DeepSeek API Key", "api_secret", "cipher-b", "mask-b",
                        "enabled", null, null, LocalDateTime.of(2026, 7, 1, 9, 0),
                        LocalDateTime.of(2026, 7, 1, 9, 0)));
        Object repository = newRepositoryProxy(handler);
        Object service = newService(repository);

        List<?> options = (List<?>) invoke(service, "listOptions", "tenant_a", " api_secret ", null);

        assertEquals("tenant_a", handler.lastTenantId);
        assertEquals("api_secret", handler.lastSecretKind);
        assertEquals("enabled", handler.lastStatus);
        assertEquals(1, options.size());
        Object first = options.get(0);
        assertEquals("secret:deepseek", invokeGetter(first, "getSecretRef"));
        assertEquals("api_secret", invokeGetter(first, "getSecretKind"));
    }

    /**
     * 验证运行期密钥解析器会按当前租户读取启用密钥并解密真实密钥值。
     *
     * @throws Exception 反射创建实体失败时抛出
     */
    @Test
    void shouldResolveEnabledSecretByCurrentTenantContext() throws Exception {
        RecordingRepositoryHandler handler = new RecordingRepositoryHandler();
        SystemSecretCipherService cipherService = new SystemSecretCipherService();
        handler.findByTenantIdAndSecretCodeResult = Optional.of(newSecret(31L, "tenant_a", "db-main",
                "主数据库密码", "db_password", cipherService.encrypt("mysql-password"), "my****rd",
                "enabled", LocalDateTime.of(2099, 12, 31, 23, 59, 59), null,
                LocalDateTime.of(2026, 6, 29, 10, 0), LocalDateTime.of(2026, 6, 29, 10, 0)));
        Object repository = newRepositoryProxy(handler);
        SystemSecretResolver resolver = new SystemSecretResolver((SysSecretRepository) repository, cipherService);

        TenantContext.setTenantId("tenant_a");
        try {
            String plaintext = resolver.resolve("db-main");

            assertEquals("mysql-password", plaintext);
            assertEquals("tenant_a", handler.lastTenantId);
            assertEquals("db-main", handler.lastSecretCode);
        } finally {
            TenantContext.clear();
        }
    }

    /**
     * 验证运行期解析密钥时必须具备租户上下文，避免跨租户读取密钥。
     */
    @Test
    void shouldRejectResolvingSecretWithoutTenantContext() {
        SystemSecretResolver resolver = new SystemSecretResolver(new com.zhyc.system.secret.repository.SysSecretRepository() {
            @Override
            public List<com.zhyc.system.secret.domain.SysSecret> findByTenantId(String tenantId) {
                return List.of();
            }

            @Override
            public Optional<com.zhyc.system.secret.domain.SysSecret> findByTenantIdAndId(String tenantId, Long secretId) {
                return Optional.empty();
            }

            @Override
            public Optional<com.zhyc.system.secret.domain.SysSecret> findByTenantIdAndSecretCode(String tenantId,
                                                                                                  String secretCode) {
                return Optional.empty();
            }

            @Override
            public List<com.zhyc.system.secret.domain.SysSecret> findSelectableSecrets(String tenantId,
                                                                                       String secretKind,
                                                                                       String status) {
                return List.of();
            }

            @Override
            public void insert(com.zhyc.system.secret.domain.SysSecret secret) {
            }

            @Override
            public void update(com.zhyc.system.secret.domain.SysSecret secret) {
            }

            @Override
            public void deleteByTenantIdAndId(String tenantId, Long secretId) {
            }
        }, new SystemSecretCipherService());

        BusinessException exception = assertThrows(BusinessException.class, () -> resolver.resolve("db-main"));

        assertEquals("ZHYC_SYSTEM_SECRET_TENANT_CONTEXT_REQUIRED", exception.getCode());
        assertEquals("解析密钥前必须绑定租户上下文", exception.getMessage());
    }

    /**
     * 创建系统密钥服务实例。
     *
     * @param repository 系统密钥仓储代理
     * @return 系统密钥服务实例
     * @throws Exception 反射创建失败时抛出
     */
    private static Object newService(Object repository) throws Exception {
        Class<?> serviceClass = Class.forName("com.zhyc.system.secret.service.DefaultSysSecretService");
        Class<?> repositoryInterface = Class.forName("com.zhyc.system.secret.repository.SysSecretRepository");
        return serviceClass.getConstructor(repositoryInterface).newInstance(repository);
    }

    /**
     * 创建系统密钥仓储代理。
     *
     * @param handler 代理处理器
     * @return 系统密钥仓储代理
     * @throws Exception 反射创建失败时抛出
     */
    private static Object newRepositoryProxy(RecordingRepositoryHandler handler) throws Exception {
        Class<?> repositoryInterface = Class.forName("com.zhyc.system.secret.repository.SysSecretRepository");
        return Proxy.newProxyInstance(SysSecretServiceTest.class.getClassLoader(),
                new Class<?>[]{repositoryInterface}, handler);
    }

    /**
     * 反射调用服务方法。
     *
     * @param service 服务实例
     * @param methodName 方法名
     * @param arguments 方法参数
     * @return 方法返回值
     * @throws Exception 调用失败时抛出
     */
    private static Object invoke(Object service, String methodName, Object... arguments) throws Exception {
        Method method = findMethod(service.getClass(), methodName, arguments.length);
        return method.invoke(service, arguments);
    }

    /**
     * 反射查找指定参数个数的方法。
     *
     * @param targetClass 目标类型
     * @param methodName 方法名
     * @param argumentCount 参数个数
     * @return 方法对象
     */
    private static Method findMethod(Class<?> targetClass, String methodName, int argumentCount) {
        return Arrays.stream(targetClass.getMethods())
                .filter(candidate -> candidate.getName().equals(methodName))
                .filter(candidate -> candidate.getParameterCount() == argumentCount)
                .findFirst()
                .orElseThrow(() -> new AssertionError("缺少服务方法: " + methodName));
    }

    /**
     * 创建保存命令。
     *
     * @param id 密钥主键
     * @param tenantId 租户业务编码
     * @param secretCode 密钥编码
     * @param secretName 密钥名称
     * @param secretKind 密钥类型
     * @param secretPlaintext 明文密钥
     * @param status 密钥状态
     * @param expireAt 到期时间
     * @return 保存命令
     * @throws Exception 反射创建失败时抛出
     */
    private static Object newSaveCommand(Long id, String tenantId, String secretCode, String secretName,
                                         String secretKind, String secretPlaintext, String status,
                                         LocalDateTime expireAt) throws Exception {
        Class<?> commandClass = Class.forName("com.zhyc.system.secret.service.SysSecretSaveCommand");
        return commandClass.getConstructor(Long.class, String.class, String.class, String.class, String.class,
                        String.class, String.class, LocalDateTime.class)
                .newInstance(id, tenantId, secretCode, secretName, secretKind, secretPlaintext, status, expireAt);
    }

    /**
     * 读取对象字段值。
     *
     * @param target 目标对象
     * @param fieldName 字段名
     * @return 字段值
     */
    private static Object getField(Object target, String fieldName) {
        try {
            Method method = target.getClass().getMethod("get" + Character.toUpperCase(fieldName.charAt(0))
                    + fieldName.substring(1));
            return method.invoke(target);
        } catch (ReflectiveOperationException exception) {
            throw new AssertionError("读取字段失败: " + fieldName, exception);
        }
    }

    /**
     * 读取对象 getter 返回值。
     *
     * @param target 目标对象
     * @param methodName getter 方法名
     * @return 方法返回值
     */
    private static Object invokeGetter(Object target, String methodName) {
        try {
            Method method = target.getClass().getMethod(methodName);
            return method.invoke(target);
        } catch (ReflectiveOperationException exception) {
            throw new AssertionError("调用 getter 失败: " + methodName, exception);
        }
    }

    /**
     * 创建系统密钥领域对象。
     *
     * @param id 主键
     * @param tenantId 租户业务编码
     * @param secretCode 密钥编码
     * @param secretName 密钥名称
     * @param secretKind 密钥类型
     * @param secretCipher 密文
     * @param secretMask 掩码
     * @param status 状态
     * @param expireAt 到期时间
     * @param lastRotatedAt 最近轮换时间
     * @param createdAt 创建时间
     * @param updatedAt 更新时间
     * @return 系统密钥领域对象
     * @throws Exception 反射创建失败时抛出
     */
    private static Object newSecret(Long id, String tenantId, String secretCode, String secretName,
                                    String secretKind, String secretCipher, String secretMask, String status,
                                    LocalDateTime expireAt, LocalDateTime lastRotatedAt,
                                    LocalDateTime createdAt, LocalDateTime updatedAt) throws Exception {
        Class<?> secretClass = Class.forName("com.zhyc.system.secret.domain.SysSecret");
        Object secret = secretClass.getConstructor().newInstance();
        setField(secret, "id", id);
        setField(secret, "tenantId", tenantId);
        setField(secret, "secretCode", secretCode);
        setField(secret, "secretName", secretName);
        setField(secret, "secretKind", secretKind);
        setField(secret, "secretCipher", secretCipher);
        setField(secret, "secretMask", secretMask);
        setField(secret, "status", status);
        setField(secret, "expireAt", expireAt);
        setField(secret, "lastRotatedAt", lastRotatedAt);
        setField(secret, "createdAt", createdAt);
        setField(secret, "updatedAt", updatedAt);
        return secret;
    }

    /**
     * 设置对象字段值。
     *
     * @param target 目标对象
     * @param fieldName 字段名
     * @param value 字段值
     */
    private static void setField(Object target, String fieldName, Object value) {
        try {
            Method method = target.getClass().getMethod("set" + Character.toUpperCase(fieldName.charAt(0))
                    + fieldName.substring(1), value == null ? findSetterType(target.getClass(), fieldName) : value.getClass());
            method.invoke(target, value);
        } catch (ReflectiveOperationException exception) {
            throw new AssertionError("设置字段失败: " + fieldName, exception);
        }
    }

    /**
     * 查找 setter 参数类型。
     *
     * @param targetClass 目标类型
     * @param fieldName 字段名
     * @return setter 参数类型
     */
    private static Class<?> findSetterType(Class<?> targetClass, String fieldName) {
        return Arrays.stream(targetClass.getMethods())
                .filter(method -> method.getName().equals("set" + Character.toUpperCase(fieldName.charAt(0))
                        + fieldName.substring(1)))
                .findFirst()
                .map(method -> method.getParameterTypes()[0])
                .orElseThrow(() -> new AssertionError("缺少 setter: " + fieldName));
    }

    /**
     * 记录仓储调用的代理处理器。
     */
    private static class RecordingRepositoryHandler implements InvocationHandler {

        /** 最近一次查询的租户业务编码。 */
        private String lastTenantId;
        /** 最近一次通过 ID 查询的主键。 */
        private Long lastSecretId;
        /** 最近一次查询的密钥编码。 */
        private String lastSecretCode;
        /** 最近一次插入的密钥。 */
        private Object insertedSecret;
        /** 最近一次更新的密钥。 */
        private Object updatedSecret;
        /** 插入次数。 */
        private int insertCount;
        /** 更新次数。 */
        private int updateCount;
        /** 按租户和编码查询的结果。 */
        private Optional<Object> findByTenantIdAndSecretCodeResult = Optional.empty();
        /** 按租户和主键查询的结果。 */
        private Optional<Object> findByTenantIdAndIdResult = Optional.empty();
        /** 可选择密钥查询结果。 */
        private List<Object> findSelectableSecretsResult = List.of();
        /** 最近一次密钥类型过滤条件。 */
        private String lastSecretKind;
        /** 最近一次密钥状态过滤条件。 */
        private String lastStatus;
        /** 按租户查询结果。 */
        private List<Object> findByTenantIdResult = List.of();

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            switch (method.getName()) {
                case "findByTenantIdAndSecretCode" -> {
                    lastTenantId = (String) args[0];
                    lastSecretCode = (String) args[1];
                    return findByTenantIdAndSecretCodeResult;
                }
                case "findByTenantIdAndId" -> {
                    lastTenantId = (String) args[0];
                    lastSecretId = (Long) args[1];
                    return findByTenantIdAndIdResult;
                }
                case "findByTenantId" -> {
                    lastTenantId = (String) args[0];
                    return findByTenantIdResult;
                }
                case "findSelectableSecrets" -> {
                    lastTenantId = (String) args[0];
                    lastSecretKind = (String) args[1];
                    lastStatus = (String) args[2];
                    return findSelectableSecretsResult;
                }
                case "insert" -> {
                    insertedSecret = args[0];
                    insertCount++;
                    return null;
                }
                case "update" -> {
                    updatedSecret = args[0];
                    updateCount++;
                    return null;
                }
                case "deleteByTenantIdAndId" -> {
                    lastTenantId = (String) args[0];
                    lastSecretId = (Long) args[1];
                    return null;
                }
                default -> throw new AssertionError("未处理的仓储方法: " + method.getName());
            }
        }
    }
}
