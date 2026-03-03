package com.oceanview.resort.util;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ValidationUtilTest {

    @Test
    public void testValidateEmail_Valid() {
        List<String> errors = new ArrayList<>();
        ValidationUtil.validateEmail("test@example.com", "Email", errors);
        assertTrue(errors.isEmpty(), "Should have no errors for valid email");
    }

    @Test
    public void testValidateEmail_Invalid() {
        List<String> errors = new ArrayList<>();
        ValidationUtil.validateEmail("invalid-email", "Email", errors);
        assertFalse(errors.isEmpty(), "Should have errors for invalid email");
        assertEquals("Email is invalid", errors.get(0));
    }

    @Test
    public void testValidateEmail_Empty() {
        List<String> errors = new ArrayList<>();
        ValidationUtil.validateEmail("", "Email", errors);
        assertFalse(errors.isEmpty(), "Should have errors for empty email");
        assertEquals("Email is required", errors.get(0));
    }
}
