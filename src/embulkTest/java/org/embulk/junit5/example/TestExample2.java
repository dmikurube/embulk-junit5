package org.embulk.junit5.example;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.embulk.junit5.api.EmbulkPluginTest;

public class TestExample2 {
    @EmbulkPluginTest
    public void testExample2_1() {
        assertEquals(0, 1 - 1);
    }

    @EmbulkPluginTest
    public void testExample2_2() {
        assertEquals(0, 1 - 1);
    }
}
