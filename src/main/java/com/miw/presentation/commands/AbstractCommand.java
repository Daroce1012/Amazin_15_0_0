package com.miw.presentation.commands;

import com.miw.infrastructure.logger.Logger;
import com.miw.infrastructure.logger.LogManager;

public abstract class AbstractCommand implements Command {
    protected Logger logger;
    
    public AbstractCommand() {
        this.logger = new LogManager(this.getClass());
    }
}

