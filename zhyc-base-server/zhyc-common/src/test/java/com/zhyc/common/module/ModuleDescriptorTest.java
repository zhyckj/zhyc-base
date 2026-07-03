/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.common.module;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ModuleDescriptorTest {

    @Test
    void defaultConstructorInitializesListFieldsAsEmptyLists() {
        ModuleDescriptor descriptor = new ModuleDescriptor();

        assertEmptyList(descriptor.getDependencies());
        assertEmptyList(descriptor.getMenus());
        assertEmptyList(descriptor.getPermissions());
        assertEmptyList(descriptor.getDictionaries());
        assertEmptyList(descriptor.getDbScripts());
        assertEmptyList(descriptor.getGeneratorTemplates());
        assertEmptyList(descriptor.getExtensionPoints());
    }

    @Test
    void settersNormalizeNullListsToEmptyLists() {
        ModuleDescriptor descriptor = new ModuleDescriptor();

        descriptor.setDependencies(null);
        descriptor.setMenus(null);
        descriptor.setPermissions(null);
        descriptor.setDictionaries(null);
        descriptor.setDbScripts(null);
        descriptor.setGeneratorTemplates(null);
        descriptor.setExtensionPoints(null);

        assertEmptyList(descriptor.getDependencies());
        assertEmptyList(descriptor.getMenus());
        assertEmptyList(descriptor.getPermissions());
        assertEmptyList(descriptor.getDictionaries());
        assertEmptyList(descriptor.getDbScripts());
        assertEmptyList(descriptor.getGeneratorTemplates());
        assertEmptyList(descriptor.getExtensionPoints());
    }

    @Test
    void constructorCopiesInputLists() {
        List<String> dependencies = new ArrayList<>();
        dependencies.add("system");
        List<String> menus = new ArrayList<>();
        menus.add("menu:system");
        List<String> permissions = new ArrayList<>();
        permissions.add("system:view");
        List<String> dictionaries = new ArrayList<>();
        dictionaries.add("dict:status");
        List<String> dbScripts = new ArrayList<>();
        dbScripts.add("db/V1.sql");
        List<String> generatorTemplates = new ArrayList<>();
        generatorTemplates.add("templates/entity.java");

        ModuleDescriptor descriptor = new ModuleDescriptor("demo", "Demo", "1.0.0",
                dependencies, menus, permissions, dictionaries, dbScripts, generatorTemplates, true);

        dependencies.add("changed");
        menus.add("changed");
        permissions.add("changed");
        dictionaries.add("changed");
        dbScripts.add("changed");
        generatorTemplates.add("changed");

        assertEquals(List.of("system"), descriptor.getDependencies());
        assertEquals(List.of("menu:system"), descriptor.getMenus());
        assertEquals(List.of("system:view"), descriptor.getPermissions());
        assertEquals(List.of("dict:status"), descriptor.getDictionaries());
        assertEquals(List.of("db/V1.sql"), descriptor.getDbScripts());
        assertEquals(List.of("templates/entity.java"), descriptor.getGeneratorTemplates());
    }

    @Test
    void settersCopyInputLists() {
        ModuleDescriptor descriptor = new ModuleDescriptor();
        List<String> dependencies = new ArrayList<>();
        dependencies.add("system");

        descriptor.setDependencies(dependencies);
        dependencies.add("changed");

        assertEquals(List.of("system"), descriptor.getDependencies());
    }

    private static void assertEmptyList(List<String> value) {
        assertNotNull(value);
        assertTrue(value.isEmpty());
    }
}
