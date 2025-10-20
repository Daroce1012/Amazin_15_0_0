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
 * Comando para gestionar el login de usuarios
 * 
 * Flujo:
 * 1. Recibe el username del formulario (via setUsername por populateParameters)
 * 2. Autentica el usuario con SecurityManager
 * 3. Si existe, guarda el usuario en la sesión con SecurityContext
 * 4. Prepara atributos para la vista (success/error)
 */
public class LoginCommand implements Command, LoggerAware, ServletRequestAware, HttpSessionAware {
    
    private Logger logger;
    private HttpServletRequest request;
    private HttpSession session;
    private String username;
    
    /**
     * Getter para username
     */
    public String getUsername() {
        return username;
    }
    
    /**
     * Setter para username
     * Será llamado automáticamente por populateParameters() del controlador
     * cuando el formulario envíe el parámetro "username"
     */
    public void setUsername(String username) {
        this.username = username;
    }
    
    /**
     * Ejecuta el proceso de login
     */
    @Override
    public void execute() {
        logger.debug("Executing LoginCommand for user: " + username);
        
        // Solo procesar si se envió un username (es decir, si se envió el formulario)
        // Si username es null, es la primera carga de la página (GET sin parámetros)
        if (username != null) {
            if (!username.trim().isEmpty()) {
                // Autenticar con SecurityManager
                SecurityManager securityManager = SecurityManager.getInstance();
                User user = securityManager.authenticate(username);
                
                if (user != null) {
                    // ✅ Usuario encontrado - Guardar en sesión
                    SecurityContext.setUser(session, user);
                    
                    // Preparar atributos para la vista
                    request.setAttribute("loginSuccess", true);
                    request.setAttribute("user", user);
                    
                    logger.info("User logged in successfully: " + username + " (role: " + user.getRole() + ")");
                } else {
                    // ❌ Usuario no encontrado
                    request.setAttribute("loginSuccess", false);
                    request.setAttribute("errorMessage", "User not found");
                    
                    logger.warn("Login failed - user not found: " + username);
                }
            } else {
                // ❌ Username vacío (formulario enviado pero vacío)
                request.setAttribute("loginSuccess", false);
                request.setAttribute("errorMessage", "You must provide a username");
                
                logger.warn("Login failed - empty username provided");
            }
        }
        // Si username es null, no establecemos nada (primera carga de la página)
        // Simplemente muestra el formulario sin mensajes de error
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

