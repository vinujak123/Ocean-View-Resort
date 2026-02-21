package com.oceanview.resort.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for manual validation (replacement for Jakarta validation)
 */
public class ValidationUtil {

    /**
     * Validate an object and return list of error messages
     */
    public static List<String> validate(Object obj) {
        List<String> errors = new ArrayList<>();

        if (obj == null) {
            errors.add("Object cannot be null");
            return errors;
        }

        // Add validation logic as needed for specific types
        return errors;
    }

    /**
     * Check if string is blank (null or empty after trim)
     */
    public static boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }

    /**
     * Check if string is not blank
     */
    public static boolean isNotBlank(String str) {
        return !isBlank(str);
    }

    /**
     * Validate required string field
     */
    public static void validateRequired(String value, String fieldName, List<String> errors) {
        if (isBlank(value)) {
            errors.add(fieldName + " is required");
        }
    }

    /**
     * Validate required object field
     */
    public static void validateRequired(Object value, String fieldName, List<String> errors) {
        if (value == null) {
            errors.add(fieldName + " is required");
        }
    }
}
