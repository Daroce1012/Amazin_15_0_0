package com.miw.infrastructure.logging;

public class LogManager implements Logger {
    private final org.apache.logging.log4j.Logger logger;
    
    public LogManager(Class<?> clazz) {
        this.logger = org.apache.logging.log4j.LogManager.getLogger(clazz);
    }
    
    @Override
    public void debug(String message) {
        logger.debug(message);
    }
    
    @Override
    public void info(String message) {
        logger.info(message);
    }
    
    @Override
    public void warn(String message) {
        logger.warn(message);
    }
    
    @Override
    public void error(String message) {
        logger.error(message);
    }
}

