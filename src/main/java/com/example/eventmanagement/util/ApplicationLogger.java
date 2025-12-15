package com.example.eventmanagement.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ApplicationLogger {

    /**
     * Get a logger instance for the given class
     */
    public Logger getLogger(Class<?> clazz) {
        return LoggerFactory.getLogger(clazz);
    }

    /**
     * Log error with exception details
     */
    public void logError(Logger logger, String message, Exception e) {
        logger.error("ERROR: {} - Exception: {}", message, e.getMessage(), e);
    }

    /**
     * Log trace for tracking operations
     */
    public void logTrace(Logger logger, String operation, String entity, Object id) {
        logger.trace("TRACE: {} operation on {} with ID: {}", operation, entity, id);
    }

    /**
     * Log info messages
     */
    public void logInfo(Logger logger, String message, Object... params) {
        logger.info("INFO: {}", message, params);
    }

    /**
     * Log debug messages
     */
    public void logDebug(Logger logger, String message, Object... params) {
        logger.debug("DEBUG: {}", message, params);
    }
}
