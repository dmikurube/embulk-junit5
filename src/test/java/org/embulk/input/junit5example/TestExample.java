package org.embulk.input.junit5example;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class TestExample {
    @Test
    public void testExample() {
        assertEquals(0, 1 - 1);
    }
}
