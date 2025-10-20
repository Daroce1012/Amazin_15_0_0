package com.miw.presentation.commands;

import com.miw.infrastructure.logger.Logger;
import com.miw.infrastructure.logger.LoggerAware;
import com.miw.presentation.di.HttpSessionAware;
import com.miw.presentation.di.ServletRequestAware;
import com.miw.security.SecurityContext;
import com.miw.security.model.User;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

/**
 * Comando para gestionar el logout (cierre de sesión)
 * 
 * Flujo:
 * 1. Obtiene el usuario actual de la sesión (opcional, para logging)
 * 2. Elimina el usuario de la sesión con SecurityContext
 * 3. Prepara atributos para la vista
 */
public class LogoutCommand implements Command, LoggerAware, ServletRequestAware, HttpSessionAware {
    
    private Logger logger;
    private HttpServletRequest request;
    private HttpSession session;
    
    /**
     * Ejecuta el proceso de logout
     */
    @Override
    public void execute() {
        logger.debug("Executing LogoutCommand");
        
        // Obtener usuario actual (antes de eliminarlo) para logging
        User currentUser = SecurityContext.getUser(session);
        String username = (currentUser != null) ? currentUser.getUsername() : "unknown";
        
        // Eliminar usuario de la sesión
        SecurityContext.clearUser(session);
        
        // Preparar atributos para la vista
        request.setAttribute("logoutSuccess", true);
        
        logger.info("User logged out successfully: " + username);
    }
    
    @Override
    public void setLogger(Logger logger) {
        this.logger = logger;
    }
    
    @Override
    public void setServletRequest(HttpServletRequest request) {
        this.request = request;
    }
    
    @Override
    public void setHttpSession(HttpSession session) {
        this.session = session;
    }
}

