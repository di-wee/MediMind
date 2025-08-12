package nus.iss.backend.util;

import org.springframework.stereotype.Component;

/**
 * utility class for sanitizing log inputs to prevent log injection attacks.
 * provides methods to clean user input before logging to prevent
 * malicious characters from being injected into log files.
 */
@Component
public class LogSanitizer {

    /**
     * sanitize input for logging to prevent log injection.
     * removes CR and LF characters and other potentially dangerous characters.
     * 
     * @param input input string to sanitize
     * @return sanitized string safe for logging, or null if input is null
     */
    public static String sanitizeForLog(String input) {
        if (input == null) {
            return null;
        }
        
        
        // using a safer approach to avoid regex issues
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
     * @param input input string to sanitize
     * @param maxLength maximum length allowed (default 1000 if not specified)
     * @return sanitized string safe for logging
     */
    public static String sanitizeForLog(String input, int maxLength) {
        if (input == null) {
            return null;
        }
        
        String sanitized = sanitizeForLog(input);
        
        // truncate if too long to prevent log flooding
        if (sanitized.length() > maxLength) {
            sanitized = sanitized.substring(0, maxLength) + "...[truncated]";
        }
        
        return sanitized;
    }

    /**
     * sanitize multiple inputs for logging.
     * 
     * @param inputs array of input strings to sanitize
     * @return array of sanitized strings
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
     * sanitize object for logging by converting to string first.
     * 
     * @param obj the object to sanitize
     * @return sanitized string representation
     */
    public static String sanitizeForLog(Object obj) {
        if (obj == null) {
            return null;
        }
        
        return sanitizeForLog(obj.toString());
    }
}
