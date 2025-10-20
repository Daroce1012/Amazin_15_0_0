package com.miw.security;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.miw.security.model.User;

/**
 * Gestor centralizado de seguridad (Patrón Singleton)
 * 
 * Responsabilidades:
 * - Cargar configuración de usuarios y autorizaciones desde archivos .properties
 * - Autenticar usuarios
 * - Verificar autorizaciones (usuario + acción)
 */
public class SecurityManager {
    
    private static SecurityManager instance = null;
    protected Logger logger = LogManager.getLogger(getClass());
    
    private Properties usersConfig;           // users.properties: username=role
    private Properties authorizationConfig;   // authorization.properties: action=roles
    private Map<String, User> users;          // Mapa en memoria de usuarios
    
    /**
     * Constructor privado (Singleton)
     * Carga la configuración al instanciarse
     */
    private SecurityManager() {
        loadConfiguration();
    }
    
    /**
     * Obtiene la instancia única del SecurityManager (Singleton)
     * Thread-safe mediante synchronized
     */
    public static synchronized SecurityManager getInstance() {
        if (instance == null) {
            instance = new SecurityManager();
        }
        return instance;
    }
    
    /**
     * Carga la configuración desde los archivos .properties
     */
    private void loadConfiguration() {
        // Cargar usuarios y roles (users.properties)
        usersConfig = new Properties();
        try {
            InputStream file = getClass().getClassLoader().getResourceAsStream("users.properties");
            if (file != null) {
                usersConfig.load(file);
                logger.info("Users configuration loaded successfully");
            } else {
                logger.warn("users.properties not found - no users configured");
            }
        } catch (IOException e) {
            logger.error("Error loading users.properties", e);
        }
        
        // Cargar autorizaciones (authorization.properties)
        authorizationConfig = new Properties();
        try {
            InputStream file = getClass().getClassLoader().getResourceAsStream("authorization.properties");
            if (file != null) {
                authorizationConfig.load(file);
                logger.info("Authorization configuration loaded successfully");
            } else {
                logger.warn("authorization.properties not found - no authorization rules configured");
            }
        } catch (IOException e) {
            logger.error("Error loading authorization.properties", e);
        }
        
        // Construir mapa de usuarios en memoria
        users = new HashMap<>();
        for (String username : usersConfig.stringPropertyNames()) {
            String role = usersConfig.getProperty(username);
            users.put(username, new User(username, role));
            logger.debug("User configured: " + username + " -> " + role);
        }
        
        logger.info("Security configuration loaded: " + users.size() + " users, " + 
                   authorizationConfig.size() + " authorization rules");
    }
    
    /**
     * Autentica un usuario (sin contraseña en esta versión simplificada)
     * 
     * @param username El nombre de usuario
     * @return El objeto User si existe, null si no se encuentra
     */
    public User authenticate(String username) {
        User user = users.get(username);
        if (user != null) {
            logger.info("User authenticated successfully: " + username);
        } else {
            logger.warn("Authentication failed - user not found: " + username);
        }
        return user;
    }
    
    /**
     * Verifica si un usuario tiene permiso para ejecutar una acción
     * 
     * @param user El usuario (puede ser null)
     * @param action El nombre de la acción (ej: "ShowBooksAction")
     * @return true si está autorizado, false en caso contrario
     */
    public boolean isAuthorized(User user, String action) {
        // Obtener roles permitidos para esta acción
        String allowedRoles = authorizationConfig.getProperty(action);
        
        // Si no hay configuración para esta acción, es pública (acceso sin autenticación)
        if (allowedRoles == null || allowedRoles.trim().isEmpty()) {
            logger.debug("No authorization config for action: " + action + " - PUBLIC ACCESS (no authentication required)");
            return true;
        }
        
        // Si la acción SÍ requiere roles específicos, verificar que haya usuario logueado
        if (user == null) {
            logger.debug("Authorization denied - action " + action + " requires authentication but no user logged in");
            return false;
        }
        
        // Parsear la lista de roles permitidos
        List<String> rolesList = Arrays.stream(allowedRoles.split(","))
                .map(String::trim)
                .collect(Collectors.toList());
        
        // Verificar si el rol del usuario está en la lista
        boolean authorized = rolesList.contains(user.getRole());
        
        logger.debug("Authorization check: user=" + user.getUsername() + 
                    ", role=" + user.getRole() + 
                    ", action=" + action + 
                    ", allowed_roles=" + rolesList + 
                    ", result=" + (authorized ? "GRANTED" : "DENIED"));
        
        return authorized;
    }
    
    /**
     * Obtiene los roles permitidos para una acción
     * Útil para mostrar mensajes de error informativos
     * 
     * @param action El nombre de la acción
     * @return Lista de roles permitidos (vacía si no hay restricciones)
     */
    public List<String> getAllowedRoles(String action) {
        String allowedRoles = authorizationConfig.getProperty(action);
        if (allowedRoles == null || allowedRoles.trim().isEmpty()) {
            return Arrays.asList();
        }
        return Arrays.stream(allowedRoles.split(","))
                .map(String::trim)
                .collect(Collectors.toList());
    }
}

