package com.miw.security;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.miw.presentation.commands.Command;
import com.miw.security.model.User;

import jakarta.servlet.http.HttpSession;

/**
 * Proxy de seguridad que controla el acceso a los comandos (Patrón Proxy)
 * 
 * Este proxy intercepta la ejecución de comandos y verifica que el usuario
 * logueado tenga permisos para ejecutar la acción solicitada.
 * 
 * Si el usuario está autorizado, ejecuta el comando real.
 * Si no está autorizado, ejecuta un comando alternativo que muestra error.
 * 
 * Ventajas:
 * - Control de acceso centralizado y transparente
 * - Los comandos reales no necesitan código de seguridad
 * - Fácil de mantener y testear
 */
public class SecureCommandProxy implements Command {
    
    protected Logger logger = LogManager.getLogger(getClass());
    
    private Command targetCommand;        // El comando real que queremos ejecutar
    private String action;                // El nombre de la acción (ej: "ShowBooksAction")
    private HttpSession session;          // Para obtener el usuario logueado
    private Command unauthorizedCommand;  // Comando alternativo si no hay permisos
    
    /**
     * Constructor del proxy
     * 
     * @param targetCommand El comando real a ejecutar si hay autorización
     * @param action El nombre de la acción (para verificar permisos)
     * @param session La sesión HTTP (para obtener el usuario logueado)
     * @param unauthorizedCommand El comando a ejecutar si no hay autorización
     */
    public SecureCommandProxy(Command targetCommand, String action, 
                             HttpSession session, Command unauthorizedCommand) {
        this.targetCommand = targetCommand;
        this.action = action;
        this.session = session;
        this.unauthorizedCommand = unauthorizedCommand;
    }
    
    /**
     * Ejecuta el comando con verificación de seguridad
     * 
     * Flujo:
     * 1. Obtiene el usuario de la sesión
     * 2. Verifica si tiene permisos para la acción
     * 3. Si está autorizado → ejecuta el comando real
     * 4. Si no está autorizado → ejecuta el comando de error
     */
    @Override
    public void execute() {
        // 1. Obtener usuario de la sesión
        User user = SecurityContext.getUser(session);
        
        // 2. Obtener el gestor de seguridad
        SecurityManager securityManager = SecurityManager.getInstance();
        
        // 3. Verificar autorización
        if (securityManager.isAuthorized(user, action)) {
            // ✅ AUTORIZADO: Ejecutar el comando real
            logger.info("Access GRANTED to [" + action + "] for user: " + 
                       (user != null ? user.getUsername() + " (" + user.getRole() + ")" : "anonymous"));
            targetCommand.execute();
        } else {
            // ❌ DENEGADO: Ejecutar comando de error
            logger.warn("Access DENIED to [" + action + "] for user: " + 
                       (user != null ? user.getUsername() + " (" + user.getRole() + ")" : "anonymous"));
            unauthorizedCommand.execute();
        }
    }
}

