/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.platform.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.zhyc.platform.security.PlatformSecurityProtectionFilter;
import com.zhyc.system.securityprotection.domain.SysSecurityPolicy;
import com.zhyc.system.securityprotection.service.SecurityEventRecordCommand;
import com.zhyc.system.securityprotection.service.SecurityEventResponse;
import com.zhyc.system.securityprotection.service.SecurityIpBlockCommand;
import com.zhyc.system.securityprotection.service.SecurityOverviewResponse;
import com.zhyc.system.securityprotection.service.SecurityRankResponse;
import com.zhyc.system.securityprotection.service.SysSecurityProtectionService;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.core.Ordered;

/**
 * 平台运行时安全防护配置测试。
 */
class PlatformSecurityProtectionConfigTest {

    /** 应用上下文启动器。 */
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(PlatformSecurityProtectionConfig.class)
            .withBean(SysSecurityProtectionService.class, RecordingSecurityProtectionService::new);

    /**
     * 验证默认启用运行时安全防护过滤器。
     */
    @Test
    void shouldRegisterPlatformSecurityProtectionFilterByDefault() {
        contextRunner.run(context -> {
            FilterRegistrationBean<?> registrationBean =
                    context.getBean("platformSecurityProtectionFilterRegistration", FilterRegistrationBean.class);

            assertEquals(1, registrationBean.getUrlPatterns().size());
            assertEquals("/*", registrationBean.getUrlPatterns().iterator().next());
            assertEquals(Ordered.HIGHEST_PRECEDENCE + 30, registrationBean.getOrder());
            assertInstanceOf(PlatformSecurityProtectionFilter.class, registrationBean.getFilter());
        });
    }

    /**
     * 验证通过配置可以关闭运行时安全防护过滤器。
     */
    @Test
    void shouldDisablePlatformSecurityProtectionFilterByProperty() {
        contextRunner.withPropertyValues("zhyc.security.protection.enabled=false")
                .run(context -> assertFalse(context.containsBean("platformSecurityProtectionFilterRegistration")));
    }

    /**
     * 验证命中封禁 IP 时直接拒绝访问并记录安全事件。
     *
     * @throws Exception 过滤器执行异常
     */
    @Test
    void shouldRejectBlockedIpBeforeBusinessChain() throws Exception {
        RecordingSecurityProtectionService service = new RecordingSecurityProtectionService();
        service.blocked = true;
        PlatformSecurityProtectionFilter filter = new PlatformSecurityProtectionFilter(service);
        org.springframework.mock.web.MockHttpServletRequest request =
                new org.springframework.mock.web.MockHttpServletRequest("GET", "/system/users");
        request.addHeader("X-ZHYC-Tenant-Id", "zhyc-platform");
        request.setRemoteAddr("113.240.198.194");
        org.springframework.mock.web.MockHttpServletResponse response =
                new org.springframework.mock.web.MockHttpServletResponse();
        RecordingFilterChain chain = new RecordingFilterChain();

        filter.doFilter(request, response, chain);

        assertEquals(403, response.getStatus());
        assertFalse(chain.called);
        assertEquals(1, service.events.size());
        assertEquals("ip_block", service.events.get(0).getEventType());
    }

    /**
     * 测试用安全防护服务。
     */
    private static final class RecordingSecurityProtectionService implements SysSecurityProtectionService {

        /** 是否命中封禁。 */
        private boolean blocked;
        /** 已记录事件。 */
        private final List<SecurityEventRecordCommand> events = new ArrayList<>();

        @Override
        public SecurityOverviewResponse overview(String tenantId, LocalDateTime now) {
            return null;
        }

        @Override
        public List<SysSecurityPolicy> listPolicies(String tenantId) {
            return List.of();
        }

        @Override
        public void savePolicy(SysSecurityPolicy policy) {
        }

        @Override
        public void recordEvent(SecurityEventRecordCommand command) {
            events.add(command);
        }

        @Override
        public List<SecurityEventResponse> recentEvents(String tenantId, int limit) {
            return List.of();
        }

        @Override
        public List<SecurityRankResponse> topSourceIps(String tenantId, LocalDateTime now, int limit) {
            return List.of();
        }

        @Override
        public List<SecurityRankResponse> topRequestPaths(String tenantId, LocalDateTime now, int limit) {
            return List.of();
        }

        @Override
        public void blockIp(SecurityIpBlockCommand command) {
        }

        @Override
        public void unblockIp(String tenantId, String ipValue) {
        }

        @Override
        public boolean isIpBlocked(String tenantId, String ipValue, LocalDateTime now) {
            return blocked;
        }
    }

    /**
     * 测试用过滤器链。
     */
    private static final class RecordingFilterChain implements jakarta.servlet.FilterChain {

        /** 是否进入后续过滤器链。 */
        private boolean called;

        @Override
        public void doFilter(jakarta.servlet.ServletRequest request, jakarta.servlet.ServletResponse response) {
            called = true;
        }
    }
}
