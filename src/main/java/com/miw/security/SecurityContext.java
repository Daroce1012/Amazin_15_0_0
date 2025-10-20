package com.miw.security;

import com.miw.security.model.User;
import jakarta.servlet.http.HttpSession;

/**
 * Clase de utilidad para gestionar el usuario logueado en la sesión HTTP
 * Proporciona una API limpia para guardar/recuperar el usuario
 */
public class SecurityContext {
    
    // Clave bajo la cual se guarda el usuario en la sesión
    private static final String USER_SESSION_KEY = "LOGGED_USER";
    
    /**
     * Almacena el usuario en la sesión (login)
     * @param session La sesión HTTP
     * @param user El usuario a almacenar
     */
    public static void setUser(HttpSession session, User user) {
        if (session != null) {
            session.setAttribute(USER_SESSION_KEY, user);
        }
    }
    
    /**
     * Obtiene el usuario de la sesión
     * @param session La sesión HTTP
     * @return El usuario logueado, o null si no hay nadie logueado
     */
    public static User getUser(HttpSession session) {
        if (session != null) {
            return (User) session.getAttribute(USER_SESSION_KEY);
        }
        return null;
    }
    
    /**
     * Elimina el usuario de la sesión (logout)
     * @param session La sesión HTTP
     */
    public static void clearUser(HttpSession session) {
        if (session != null) {
            session.removeAttribute(USER_SESSION_KEY);
        }
    }
    
    /**
     * Verifica si hay un usuario logueado
     * @param session La sesión HTTP
     * @return true si hay un usuario logueado, false en caso contrario
     */
    public static boolean isAuthenticated(HttpSession session) {
        return getUser(session) != null;
    }
}

