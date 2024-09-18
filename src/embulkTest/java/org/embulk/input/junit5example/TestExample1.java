package org.embulk.input.junit5example;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.embulk.junit5.api.EmbulkPluginTest;

public class TestExample1 {
    @EmbulkPluginTest
    public void testExample1_1() {
        final ClassLoader classLoader = this.getClass().getClassLoader();
        assertEquals("org.embulk.plugin.PluginClassLoader", classLoader.getClass().getName());
    }

    @EmbulkPluginTest
    public void testExample1_2() {
        final ClassLoader classLoader = this.getClass().getClassLoader();
        assertTrue(classLoader instanceof org.embulk.plugin.PluginClassLoader);
    }
}
