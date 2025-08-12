package nus.iss.backend.util;

import org.springframework.stereotype.Component;

/**
 * Utility class for sanitizing log inputs to prevent log injection attacks.
 * This class provides methods to clean user input before logging to prevent
 * malicious characters from being injected into log files.
 */
@Component
public class LogSanitizer {

    /**
     * Sanitize input for logging to prevent log injection.
     * Removes CR and LF characters and other potentially dangerous characters.
     * 
     * @param input The input string to sanitize
     * @return Sanitized string safe for logging, or null if input is null
     */
    public static String sanitizeForLog(String input) {
        if (input == null) {
            return null;
        }
        
        // Remove CR, LF, and other control characters that could be used for log injection
        // Using a safer approach to avoid regex issues
        return input.replace("\r", "_")
                   .replace("\n", "_")
                   .replace("\t", "_")
                   .replace("\f", "_")
                   .replace("\b", "_");
    }

    /**
     * Sanitize input for logging with additional security measures.
     * Removes control characters and limits length to prevent log flooding.
     * 
     * @param input The input string to sanitize
     * @param maxLength Maximum length allowed (default 1000 if not specified)
     * @return Sanitized string safe for logging
     */
    public static String sanitizeForLog(String input, int maxLength) {
        if (input == null) {
            return null;
        }
        
        String sanitized = sanitizeForLog(input);
        
        // Truncate if too long to prevent log flooding
        if (sanitized.length() > maxLength) {
            sanitized = sanitized.substring(0, maxLength) + "...[truncated]";
        }
        
        return sanitized;
    }

    /**
     * Sanitize multiple inputs for logging.
     * 
     * @param inputs Array of input strings to sanitize
     * @return Array of sanitized strings
     */
    public static String[] sanitizeForLog(String... inputs) {
        if (inputs == null) {
            return null;
        }
        
        String[] sanitized = new String[inputs.length];
        for (int i = 0; i < inputs.length; i++) {
            sanitized[i] = sanitizeForLog(inputs[i]);
        }
        
        return sanitized;
    }

    /**
     * Sanitize object for logging by converting to string first.
     * 
     * @param obj The object to sanitize
     * @return Sanitized string representation
     */
    public static String sanitizeForLog(Object obj) {
        if (obj == null) {
            return null;
        }
        
        return sanitizeForLog(obj.toString());
    }
}
