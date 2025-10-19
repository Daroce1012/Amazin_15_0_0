package com.miw.presentation.commands;

import com.miw.infrastructure.logging.Logger;
import com.miw.infrastructure.logging.LogManager;

public abstract class AbstractCommand implements Command {
    protected Logger logger;
    
    public AbstractCommand() {
        this.logger = new LogManager(this.getClass());
    }
}

