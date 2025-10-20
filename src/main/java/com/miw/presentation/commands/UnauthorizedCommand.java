package com.miw.presentation.commands;

import com.miw.infrastructure.logger.Logger;
import com.miw.infrastructure.logger.LoggerAware;
import com.miw.presentation.di.HttpSessionAware;
import com.miw.presentation.di.ServletRequestAware;
import com.miw.security.SecurityContext;
import com.miw.security.SecurityManager;
import com.miw.security.model.User;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

/**
 * Comando que se ejecuta cuando un usuario intenta acceder a un recurso
 * sin los permisos necesarios.
 * 
 * Prepara los atributos necesarios para que la vista (unauthorized.jsp)
 * muestre un mensaje de error informativo al usuario.
 */
public class UnauthorizedCommand implements Command, LoggerAware, ServletRequestAware, HttpSessionAware {
    
    private Logger logger;
    private HttpServletRequest request;
    private HttpSession session;
    private String action;
    
    /**
     * Constructor por defecto
     */
    public UnauthorizedCommand() {
    }
    
    /**
     * Constructor con acción
     * @param action El nombre de la acción que fue denegada
     */
    public UnauthorizedCommand(String action) {
        this.action = action;
    }
    
    /**
     * Ejecuta el comando de acceso no autorizado
     * Prepara información para mostrar en la vista de error
     * 
     * La vista solo necesita mostrar los atributos, toda la lógica está aquí
     */
    @Override
    public void execute() {
        logger.debug("Executing UnauthorizedCommand for action: " + action);
        
        // Marcar que hubo un error de autorización
        request.setAttribute("unauthorized", true);
        
        // Obtener usuario de la sesión
        User user = SecurityContext.getUser(session);
        
        // Preparar mensaje, URL y texto del link según si hay usuario o no
        if (user == null) {
            // Usuario no logueado
            request.setAttribute("errorMessage", "You must be logged in to access this resource.");
            request.setAttribute("linkUrl", "login.jsp");
            request.setAttribute("linkText", "Go to Login");
            logger.info("Unauthorized access (no login) - action: " + action);
        } else {
            // Usuario logueado pero sin permisos
            request.setAttribute("errorMessage", "You don't have permission to access this resource.");
            request.setAttribute("linkUrl", "index.jsp");
            request.setAttribute("linkText", "Back to Main Menu");
            logger.info("Unauthorized access (insufficient permissions) - user: " + user.getUsername() + 
                       ", action: " + action);
        }
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
    
    /**
     * Setter para la acción (permite configurarla después de la construcción)
     * @param action El nombre de la acción
     */
    public void setAction(String action) {
        this.action = action;
    }
}

