# Trabajo 1 - Amazin 15.0.0
## Solución Completa de los Problemas de Arquitectura

**Master en Ingeniería Web - Universidad de Oviedo**

---

## 🌐 URL de Acceso al Proyecto

**Aplicación desplegada en:** `http://156.35.95.57:8080/Amazin_15_0_0/`

---

## 📑 Índice

1. [Introducción](#introducción)
2. [Problema 1: Dependencia del Logger](#problema-1-dependencia-del-logger)
3. [Problema 2: Sistema de Autorización](#problema-2-sistema-de-autorización)
4. [Ejemplo Práctico: Proteger un Command](#ejemplo-práctico-proteger-un-command)
5. [Pruebas y Validación](#pruebas-y-validación)

---

## 1. Introducción

Este documento describe detalladamente las soluciones implementadas para dos problemas arquitectónicos fundamentales en la aplicación Amazin 15.0:

1. **Eliminación de dependencia directa con `org.apache.logging.log4j.Logger`** en los Commands
2. **Implementación de un sistema de autorización basado en roles** con gestión externalizada

Las soluciones propuestas aplican patrones de diseño reconocidos y siguen los principios SOLID, especialmente:
- **Dependency Inversion Principle (DIP)**: Depender de abstracciones, no de implementaciones concretas
- **Single Responsibility Principle (SRP)**: Cada clase tiene una única responsabilidad
- **Open/Closed Principle (OCP)**: Abierto a extensión, cerrado a modificación

---

## 2. Problema 1: Dependencia del Logger

### 2.1. Descripción del Problema

**Situación Inicial:**
Los Commands utilizaban directamente la clase `org.apache.logging.log4j.Logger`, creando un **acoplamiento fuerte** con una librería externa específica:

```java
// ❌ ANTES: Dependencia directa con log4j
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class MiCommand implements Command {
    private Logger logger = LogManager.getLogger(getClass());
    
    public void execute() {
        logger.info("Ejecutando comando");
    }
}
```

**Problemas identificados:**
1. ❌ **Acoplamiento fuerte**: Los Commands dependen directamente de log4j
2. ❌ **Difícil testeo**: No se puede mockear fácilmente el logger
3. ❌ **Difícil migración**: Cambiar de librería de logging requiere modificar todos los Commands
4. ❌ **Violación del DIP**: Se depende de una implementación concreta, no de una abstracción

### 2.2. Solución Implementada

La solución aplica **tres patrones de diseño** para eliminar la dependencia:

#### Patrón 1: **Dependency Injection (Inyección por Interfaz)**

Se implementa inyección de dependencias usando interfaces para desacoplar los Commands del logger concreto.

#### Patrón 2: **Adapter/Wrapper Pattern**

Se crea una interfaz propia que abstrae la funcionalidad de logging y un adapter que adapta log4j a esta interfaz.

#### Patrón 3: **Interface Segregation**

Se define una interfaz `LoggerAware` que los Commands implementan para recibir el logger.

### 2.3. Arquitectura de la Solución

```
┌────────────────────────────────────────────────────────┐
│                  ControllerServlet                     │
│   • Crea instancia de LogManager(cmd.getClass())      │
│   • Inyecta logger en Commands que implementen        │
│     LoggerAware mediante setLogger()                   │
└────────────┬───────────────────────────────────────────┘
             │
             │ inyecta
             ▼
    ┌─────────────────┐          implementa     ┌──────────────────┐
    │    Command      │◄──────────────────────  │   LoggerAware    │
    │  (ShowBooks,    │                         │   (interface)    │
    │   Login, etc)   │                         │                  │
    └────────┬────────┘                         │ + setLogger()    │
             │                                   └──────────────────┘
             │ usa
             ▼
    ┌─────────────────┐          implementa     ┌──────────────────┐
    │     Logger      │◄──────────────────────  │   LogManager     │
    │   (interface)   │                         │   (adapter)      │
    │                 │                         │                  │
    │ + debug()       │                         │ Adapta log4j     │
    │ + info()        │                         │ a interfaz propia│
    │ + warn()        │                         └─────────┬────────┘
    │ + error()       │                                   │
    └─────────────────┘                                   │ usa
                                                          ▼
                                              ┌─────────────────────────┐
                                              │ org.apache.log4j.Logger │
                                              │   (librería externa)    │
                                              └─────────────────────────┘
```

### 2.4. Componentes de la Solución

#### 2.4.1. Interfaz `Logger` (Abstracción)

**Ubicación:** `com.miw.infrastructure.logger.Logger`

```java
package com.miw.infrastructure.logger;

/**
 * Interfaz que abstrae las operaciones de logging
 * NO depende de ninguna librería externa
 * Define el contrato que debe cumplir cualquier implementación de logger
 */
public interface Logger {
    void debug(String message);
    void info(String message);
    void warn(String message);
    void error(String message);
}
```

**Características:**
- ✅ **Independiente de librerías externas**: No importa nada de log4j
- ✅ **Contrato claro**: Define solo los métodos necesarios
- ✅ **Fácil de testear**: Se puede crear un mock que implemente esta interfaz
- ✅ **Extensible**: Si necesitamos más métodos, los añadimos aquí

#### 2.4.2. Interfaz `LoggerAware` (Marcador de Inyección)

**Ubicación:** `com.miw.infrastructure.logger.LoggerAware`

```java
package com.miw.infrastructure.logger;

/**
 * Interfaz marcadora que indica que un Command necesita un logger
 * Sigue el patrón "Aware" de Spring Framework
 */
public interface LoggerAware {
    void setLogger(Logger logger);
}
```

**Características:**
- ✅ **Patrón Aware**: Indica que la clase es consciente y necesita algo
- ✅ **Opcional**: Solo los Commands que necesitan logging implementan esta interfaz
- ✅ **Desacoplado**: No sabe nada sobre la implementación del logger

#### 2.4.3. Clase `LogManager` (Adapter)

**Ubicación:** `com.miw.infrastructure.logger.LogManager`

```java
package com.miw.infrastructure.logger;

/**
 * Adapter que adapta org.apache.logging.log4j.Logger a nuestra interfaz Logger
 * Encapsula completamente la dependencia con log4j
 */
public class LogManager implements Logger {
    // La ÚNICA clase del proyecto que depende directamente de log4j
    private final org.apache.logging.log4j.Logger logger;
    
    public LogManager(Class<?> clazz) {
        // Creación del logger real de log4j
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
```

**Características:**
- ✅ **Encapsulación total**: Es la ÚNICA clase que depende de log4j
- ✅ **Adapter Pattern**: Adapta la interfaz de log4j a nuestra interfaz
- ✅ **Intercambiable**: Si queremos cambiar a otra librería (Logback, java.util.logging), solo modificamos esta clase
- ✅ **Transparente**: Los Commands no saben que detrás está log4j

#### 2.4.4. Inyección en el `ControllerServlet`

**Ubicación:** `com.miw.presentation.controller.ControllerServlet`

```java
/**
 * Inyecta dependencias estándar del framework en un comando
 * Usa el patrón de inyección basado en interfaces
 */
private void injectDependencies(Command cmd, HttpServletRequest req) {
    // ... otras inyecciones ...
    
    // Inyección del Logger
    if (cmd instanceof LoggerAware) {
        logger.debug("Injecting logger in command");
        // Crea un LogManager específico para la clase del comando
        ((LoggerAware) cmd).setLogger(
            new com.miw.infrastructure.logger.LogManager(cmd.getClass())
        );
    }
}
```

**Características:**
- ✅ **Inyección automática**: El controlador inyecta el logger automáticamente
- ✅ **Logger específico**: Cada Command tiene un logger con su nombre de clase
- ✅ **Opcional**: Solo se inyecta si el Command implementa LoggerAware

### 2.5. Uso en los Commands

#### Ejemplo: `ShowBooksCommand`

```java
package com.miw.presentation.commands;

import com.miw.infrastructure.logger.Logger;      // ✅ Interfaz propia
import com.miw.infrastructure.logger.LoggerAware; // ✅ Interfaz propia
// ❌ NO importa org.apache.logging.log4j.Logger

public class ShowBooksCommand implements Command, LoggerAware {
    
    // ✅ Dependencia de una INTERFAZ, no de una clase concreta
    private Logger logger;
    
    // ✅ Implementación del contrato LoggerAware
    @Override
    public void setLogger(Logger logger) {
        this.logger = logger;
    }
    
    @Override
    public void execute() {
        // ✅ Uso transparente del logger
        logger.debug("Executing ShowBooksCommand");
        logger.info("Books requested by user");
        
        // ... lógica del comando ...
    }
}
```

### 2.6. Ventajas de la Solución

| Aspecto | Antes | Después |
|---------|-------|---------|
| **Dependencias** | Commands dependen de log4j | Commands dependen de interfaz propia |
| **Acoplamiento** | Fuerte con librería externa | Débil con abstracción |
| **Testabilidad** | Difícil de mockear | Fácil: basta crear mock de Logger |
| **Migración** | Cambiar todos los Commands | Cambiar solo LogManager |
| **Flexibilidad** | Ninguna | Total: se puede cambiar implementación |
| **Principios SOLID** | Viola DIP | Cumple DIP, SRP, OCP |

### 2.7. Comparación de Cambio de Librería

#### Si mañana queremos cambiar de log4j a SLF4J:

**❌ ANTES (sin solución):**
```
Archivos a modificar:
  ☑ ShowBooksCommand.java
  ☑ ShowSpecialOfferCommand.java
  ☑ LoginCommand.java
  ☑ LogoutCommand.java
  ☑ UnauthorizedCommand.java
  ... TODOS los Commands (5+ archivos)
```

**✅ DESPUÉS (con solución):**
```
Archivos a modificar:
  ☑ LogManager.java (UN SOLO ARCHIVO)
  
// Nueva implementación con SLF4J
public class LogManager implements Logger {
    private final org.slf4j.Logger logger;
    
    public LogManager(Class<?> clazz) {
        this.logger = org.slf4j.LoggerFactory.getLogger(clazz);
    }
    // ... resto igual ...
}
```

---

## 3. Problema 2: Sistema de Autorización

### 3.1. Descripción del Problema

**Requisitos:**
1. ✅ Sistema de autorización basado en **usuarios y roles**
2. ✅ Gestión **externalizada en archivos de configuración**
3. ✅ Control de acceso **transparente** (los Commands no deben conocer la seguridad)
4. ✅ Los Commands pueden **solicitar información del usuario** siguiendo filosofía **POJO** (sin interfaces del FW)
5. ✅ Al menos **dos usuarios** con distintos privilegios
6. ✅ Mensaje de **"Acceso no autorizado"** integrado en la UI
7. ✅ Sistema de **login sin contraseña**

### 3.2. Solución Implementada

La solución aplica **CUATRO patrones de diseño principales** y varios complementarios:

### 3.3. Arquitectura General del Sistema

```
┌──────────────────────────────────────────────────────────────────┐
│                      ControllerServlet                           │
│                                                                  │
│  1. Obtiene comando del ConfigurationManager                     │
│  2. Crea UnauthorizedCommand (comando de error)                  │
│  3. Envuelve comando en SecureCommandProxy                       │
│  4. Inyecta dependencias estándar (Logger, Request, Session)     │
│  5. Inyecta User (POJO) mediante reflexión                       │
│  6. Ejecuta el comando a través del proxy                        │
│  7. Forward a la vista correspondiente                           │
└────────┬─────────────────────────────────────────────────────────┘
         │
         │ ejecuta
         ▼
┌─────────────────────────────────────────────────────────────────┐
│               SecureCommandProxy (Patrón Proxy)                 │
│                                                                 │
│  • Obtiene usuario de SecurityContext                           │
│  • Consulta a SecurityManager.isAuthorized(user, action)        │
│  • Si autorizado → ejecuta targetCommand                        │
│  • Si NO autorizado → ejecuta unauthorizedCommand               │
└────┬────────────────────────────────────┬─────────────────────┘
     │                                    │
     │ SI autorizado                      │ NO autorizado
     ▼                                    ▼
┌──────────────────┐              ┌────────────────────────┐
│  Command Real    │              │ UnauthorizedCommand    │
│  (ShowBooks,     │              │  Muestra página error  │
│   Login, etc)    │              └────────────────────────┘
└──────────────────┘

Componentes de soporte:

┌─────────────────────────────────────────────────────────────────┐
│        SecurityManager (Patrón Singleton)                       │
│  • Carga users.properties                                       │
│  • Carga authorization.properties                               │
│  • authenticate(username) → User                                │
│  • isAuthorized(user, action) → boolean                         │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│        SecurityContext (Utility Class)                          │
│  • setUser(session, user)                                       │
│  • getUser(session) → User                                      │
│  • clearUser(session)                                           │
│  • isAuthenticated(session) → boolean                           │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│        User (POJO - sin dependencias del framework)             │
│  • username: String                                             │
│  • role: String                                                 │
└─────────────────────────────────────────────────────────────────┘
```

### 3.4. Patrones de Diseño Implementados

#### 🎯 Patrón 1: **Singleton Pattern**

**Aplicado en:** `SecurityManager`

**Propósito:** Garantizar una única instancia del gestor de seguridad en toda la aplicación.

**Implementación:**

```java
public class SecurityManager {
    
    // Instancia única (thread-safe mediante synchronized)
    private static SecurityManager instance = null;
    
    private Properties usersConfig;
    private Properties authorizationConfig;
    
    // Constructor privado - nadie puede crear instancias desde fuera
    private SecurityManager() {
        loadConfiguration();
    }
    
    // Método sincronizado para garantizar thread-safety
    public static synchronized SecurityManager getInstance() {
        if (instance == null) {
            instance = new SecurityManager();
        }
        return instance;
    }
    
    // Métodos de negocio
    public User authenticate(String username) { ... }
    public boolean isAuthorized(User user, String action) { ... }
}
```

**Ventajas:**
- ✅ **Configuración centralizada**: Se carga una sola vez
- ✅ **Eficiencia**: No se duplican las propiedades en memoria
- ✅ **Thread-safe**: Uso de synchronized garantiza seguridad en hilos
- ✅ **Punto único de acceso**: Todos acceden a través de getInstance()

**Justificación:**
- La configuración de seguridad es global y debe ser única
- No tiene sentido tener múltiples instancias con la misma configuración
- Facilita el acceso desde cualquier punto de la aplicación

#### 🎯 Patrón 2: **Proxy Pattern**

**Aplicado en:** `SecureCommandProxy`

**Propósito:** Controlar el acceso a los Commands de forma transparente, sin que ellos conozcan la existencia del control de seguridad.

**Implementación:**

```java
/**
 * Proxy que intercepta la ejecución de comandos
 * y verifica permisos antes de ejecutarlos
 */
public class SecureCommandProxy implements Command {
    
    private Command targetCommand;        // Comando real
    private String action;                // Acción a verificar
    private HttpSession session;          // Para obtener usuario
    private Command unauthorizedCommand;  // Comando alternativo si falla
    
    public SecureCommandProxy(Command targetCommand, String action,
                             HttpSession session, Command unauthorizedCommand) {
        this.targetCommand = targetCommand;
        this.action = action;
        this.session = session;
        this.unauthorizedCommand = unauthorizedCommand;
    }
    
    @Override
    public void execute() {
        // 1. Obtener usuario de la sesión
        User user = SecurityContext.getUser(session);
        
        // 2. Verificar autorización
        SecurityManager sm = SecurityManager.getInstance();
        
        if (sm.isAuthorized(user, action)) {
            // ✅ AUTORIZADO: ejecutar comando real
            logger.info("Access GRANTED to [" + action + "]");
            targetCommand.execute();
        } else {
            // ❌ DENEGADO: ejecutar comando de error
            logger.warn("Access DENIED to [" + action + "]");
            unauthorizedCommand.execute();
        }
    }
}
```

**Ventajas:**
- ✅ **Transparencia**: Los Commands no saben que están siendo protegidos
- ✅ **Separación de responsabilidades**: La seguridad está en el proxy, no en el Command
- ✅ **Reusabilidad**: El mismo proxy sirve para todos los Commands
- ✅ **Open/Closed Principle**: Añadimos seguridad sin modificar los Commands existentes

**Justificación:**
- Requisito explícito: "control transparente al propio comando"
- Permite añadir seguridad sin tocar el código de negocio
- Fácil de activar/desactivar según configuración

#### 🎯 Patrón 3: **Dependency Injection**

**Aplicado en:** Múltiples lugares con dos variantes

##### Variante A: Inyección mediante Interfaces (Aware)

**Interfaces:**
```java
// Interfaz que indica necesidad de Logger
public interface LoggerAware {
    void setLogger(Logger logger);
}

// Interfaz que indica necesidad de HttpServletRequest
public interface ServletRequestAware {
    void setServletRequest(HttpServletRequest request);
}

// Interfaz que indica necesidad de HttpSession
public interface HttpSessionAware {
    void setHttpSession(HttpSession session);
}
```

**Uso en Commands:**
```java
public class LoginCommand implements Command, LoggerAware, 
                                     ServletRequestAware, HttpSessionAware {
    private Logger logger;
    private HttpServletRequest request;
    private HttpSession session;
    
    // Setters llamados automáticamente por el controlador
    public void setLogger(Logger logger) { this.logger = logger; }
    public void setServletRequest(HttpServletRequest request) { this.request = request; }
    public void setHttpSession(HttpSession session) { this.session = session; }
    
    public void execute() {
        // Usa las dependencias inyectadas
        logger.info("Login attempt");
        // ...
    }
}
```

**Inyección en el Controlador:**
```java
private void injectDependencies(Command cmd, HttpServletRequest req) {
    if (cmd instanceof ServletRequestAware) {
        ((ServletRequestAware) cmd).setServletRequest(req);
    }
    if (cmd instanceof HttpSessionAware) {
        ((HttpSessionAware) cmd).setHttpSession(req.getSession());
    }
    if (cmd instanceof LoggerAware) {
        ((LoggerAware) cmd).setLogger(new LogManager(cmd.getClass()));
    }
}
```

##### Variante B: Inyección POJO mediante Reflexión

**Propósito:** Permitir que los Commands reciban información del usuario SIN implementar ninguna interfaz del framework.

**Implementación en el Controlador:**
```java
/**
 * Inyecta información del usuario de forma POJO (sin interfaces)
 * Busca un método setUser(User) mediante reflexión
 * Si el comando no tiene este método, simplemente no hace nada
 */
private void injectUserInformation(Command cmd, HttpServletRequest req) {
    try {
        // Buscar si el comando tiene un método setUser(User)
        Method setUserMethod = cmd.getClass().getMethod("setUser", User.class);
        if (setUserMethod != null) {
            // Obtener el usuario de la sesión
            User user = SecurityContext.getUser(req.getSession());
            if (user != null) {
                // Invocar el método mediante reflexión
                logger.debug("Injecting user (POJO): " + user.getUsername());
                setUserMethod.invoke(cmd, user);
            }
        }
    } catch (NoSuchMethodException e) {
        // El comando no tiene setUser() → OK, es opcional
    } catch (IllegalAccessException | InvocationTargetException e) {
        logger.error("Error injecting user information", e);
    }
}
```

**Uso en Commands (POJO):**
```java
public class ShowBooksCommand implements Command, LoggerAware, ServletRequestAware {
    private Logger logger;
    private HttpServletRequest request;
    
    // ⭐ POJO: No implementa ninguna interfaz del framework para esto
    private User user;
    
    /**
     * Método opcional que el controlador buscará mediante reflexión
     * Si existe, se invocará automáticamente
     * Si no existe, no pasa nada
     */
    public void setUser(User user) {
        this.user = user;
        logger.debug("User injected (POJO): " + user.getUsername());
    }
    
    @Override
    public void execute() {
        // Puede usar la información del usuario si está disponible
        if (user != null) {
            logger.info("Books requested by: " + user.getUsername());
        }
        // ... lógica ...
    }
    
    // Implementación de interfaces Aware para otras dependencias
    public void setLogger(Logger logger) { this.logger = logger; }
    public void setServletRequest(HttpServletRequest request) { this.request = request; }
}
```

**Ventajas de la Inyección POJO:**
- ✅ **Requisito cumplido**: "Filosofía POJO (sin obligarle a cumplir con ninguna interfaz del FW)"
- ✅ **Completamente opcional**: El Command no está obligado a tener setUser()
- ✅ **No invasivo**: El User es un POJO puro, sin dependencias del framework
- ✅ **Flexible**: El Command decide si necesita o no el usuario

#### 🎯 Patrón 4: **Utility Class (Helper)**

**Aplicado en:** `SecurityContext`

**Propósito:** Proporcionar una API limpia y centralizada para gestionar el usuario en la sesión HTTP.

**Implementación:**

```java
/**
 * Clase de utilidad para gestionar el usuario en la sesión
 * Métodos estáticos que encapsulan el acceso a la sesión
 */
public class SecurityContext {
    
    private static final String USER_SESSION_KEY = "LOGGED_USER";
    
    // Constructor privado - clase no instanciable
    private SecurityContext() {}
    
    /**
     * Almacena el usuario en la sesión (login)
     */
    public static void setUser(HttpSession session, User user) {
        if (session != null) {
            session.setAttribute(USER_SESSION_KEY, user);
        }
    }
    
    /**
     * Obtiene el usuario de la sesión
     * @return Usuario logueado o null
     */
    public static User getUser(HttpSession session) {
        if (session != null) {
            return (User) session.getAttribute(USER_SESSION_KEY);
        }
        return null;
    }
    
    /**
     * Elimina el usuario de la sesión (logout)
     */
    public static void clearUser(HttpSession session) {
        if (session != null) {
            session.removeAttribute(USER_SESSION_KEY);
        }
    }
    
    /**
     * Verifica si hay un usuario logueado
     */
    public static boolean isAuthenticated(HttpSession session) {
        return getUser(session) != null;
    }
}
```

**Ventajas:**
- ✅ **API limpia**: En lugar de `session.getAttribute("LOGGED_USER")`, usamos `SecurityContext.getUser(session)`
- ✅ **Encapsulación**: La clave de sesión está oculta, solo `SecurityContext` la conoce
- ✅ **Mantenibilidad**: Si cambiamos cómo se almacena el usuario, solo cambiamos esta clase
- ✅ **Legibilidad**: El código es más expresivo y fácil de entender

### 3.5. Modelo de Datos: User (POJO)

**Ubicación:** `com.miw.security.model.User`

```java
/**
 * Modelo POJO que representa un usuario del sistema
 * NO tiene dependencias del framework - puede usarse en cualquier capa
 */
public class User {
    private String username;
    private String role;
    
    public User() {}
    
    public User(String username, String role) {
        this.username = username;
        this.role = role;
    }
    
    // Getters y setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    
    @Override
    public String toString() {
        return "User{username='" + username + "', role='" + role + "'}";
    }
}
```

**Características:**
- ✅ **POJO puro**: Sin anotaciones, sin interfaces, sin extends
- ✅ **Portable**: Puede usarse en cualquier capa sin dependencias
- ✅ **Serializable**: Puede almacenarse en sesión HTTP sin problemas
- ✅ **Simple**: Solo contiene datos, sin lógica de negocio

### 3.6. Configuración Externalizada

#### 3.6.1. Archivo `users.properties`

**Ubicación:** `src/main/resources/users.properties`

```properties
# Configuración de usuarios y roles
# Formato: username=role

# user1 tiene rol ADMIN - puede acceder a todo
user1=ADMIN

# user2 tiene rol USER - acceso limitado
user2=USER
```

**Características:**
- ✅ **Formato simple**: username=role
- ✅ **Fácil de extender**: Solo añadir líneas
- ✅ **Sin contraseña**: Versión simplificada según requisitos

#### 3.6.2. Archivo `authorization.properties`

**Ubicación:** `src/main/resources/authorization.properties`

```properties
# Configuración de autorización
# Formato: action=role1,role2,role3

# IMPORTANTE:
# - Si una acción NO está listada → PÚBLICA (no requiere login)
# - Si una acción SÍ está listada → requiere uno de los roles indicados

# ShowBooksAction: Tanto ADMIN como USER pueden ver el catálogo
ShowBooksAction=ADMIN,USER

# ShowSpecialOfferAction: Solo ADMIN puede ver ofertas especiales
ShowSpecialOfferAction=ADMIN
```

**Características:**
- ✅ **Flexible**: Múltiples roles por acción separados por comas
- ✅ **Seguro por defecto**: Si no está listada, es pública
- ✅ **Explícito**: Fácil de ver qué roles pueden hacer qué

**Lógica de autorización:**
```
SI la acción NO está en authorization.properties:
    → Acceso PÚBLICO (sin login)
    
SI la acción SÍ está en authorization.properties:
    SI usuario NO está logueado:
        → Acceso DENEGADO
    SI usuario SÍ está logueado:
        SI rol del usuario está en la lista:
            → Acceso PERMITIDO
        SI rol del usuario NO está en la lista:
            → Acceso DENEGADO
```

### 3.7. Flujo Completo de una Petición Protegida

Supongamos que `user2` (rol USER) intenta acceder a "ShowSpecialOfferAction":

```
1. 🌐 Usuario hace clic en "Show Special Offers!"
   → GET /Controller?action=ShowSpecialOfferAction

2. 📥 ControllerServlet.doGet()
   → action = "ShowSpecialOfferAction"
   
3. 🔍 Obtiene el comando del ConfigurationManager
   → command = new ShowSpecialOfferCommand()

4. 🛡️ Crea comando de error
   → unauthorizedCmd = new UnauthorizedCommand("ShowSpecialOfferAction")
   
5. 🔐 Envuelve en proxy de seguridad
   → secureCmd = new SecureCommandProxy(command, action, session, unauthorizedCmd)

6. 💉 Inyecta dependencias
   → logger, request, session en ambos comandos
   
7. 👤 Inyecta usuario (POJO)
   → Si el comando tiene setUser(), lo invoca con el usuario de la sesión

8. ▶️ Ejecuta el proxy
   → secureCmd.execute()
   
   8.1. Proxy obtiene usuario de la sesión
        → user = SecurityContext.getUser(session)
        → user = User{username='user2', role='USER'}
   
   8.2. Proxy consulta a SecurityManager
        → sm.isAuthorized(user, "ShowSpecialOfferAction")
        
        8.2.1. SecurityManager busca en authorization.properties
               → ShowSpecialOfferAction=ADMIN
        
        8.2.2. Verifica si 'USER' está en la lista ['ADMIN']
               → NO está
        
        8.2.3. Retorna false
   
   8.3. Como NO está autorizado:
        → logger.warn("Access DENIED to [ShowSpecialOfferAction] for user: user2 (USER)")
        → unauthorizedCommand.execute()
        
        8.3.1. UnauthorizedCommand prepara atributos
               → request.setAttribute("unauthorized", true)
               → request.setAttribute("errorMessage", "No tiene permisos...")

9. 🔀 ControllerServlet verifica resultado
   → if (request.getAttribute("unauthorized") == true)
   → forward = "unauthorized.jsp"
   
10. 📄 Forward a la vista
    → dispatcher.forward(req, resp)
    → Se muestra unauthorized.jsp con mensaje de error

11. ❌ Usuario ve: "Acceso No Autorizado"
```

**Si fuera `user1` (rol ADMIN):**
```
8.2.2. Verifica si 'ADMIN' está en la lista ['ADMIN']
       → SÍ está
       
8.2.3. Retorna true

8.3. Como SÍ está autorizado:
     → logger.info("Access GRANTED to [ShowSpecialOfferAction] for user: user1 (ADMIN)")
     → targetCommand.execute()
     
9. Forward a la vista de éxito
   → forward = "showSpecialOffer.jsp"
   
10. ✅ Usuario ve la oferta especial
```

### 3.8. Comandos del Sistema de Seguridad

#### 3.8.1. LoginCommand

**Propósito:** Autenticar usuarios en el sistema.

**Flujo:**
1. Recibe `username` del formulario
2. Consulta `SecurityManager.authenticate(username)`
3. Si existe, guarda en sesión con `SecurityContext.setUser()`
4. Prepara atributos para la vista

**Código simplificado:**
```java
public class LoginCommand implements Command, LoggerAware, 
                                     ServletRequestAware, HttpSessionAware {
    private Logger logger;
    private HttpServletRequest request;
    private HttpSession session;
    private String username;  // Inyectado por populateParameters()
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    @Override
    public void execute() {
        if (username != null && !username.trim().isEmpty()) {
            SecurityManager sm = SecurityManager.getInstance();
            User user = sm.authenticate(username);
            
            if (user != null) {
                // ✅ Usuario encontrado
                SecurityContext.setUser(session, user);
                request.setAttribute("loginSuccess", true);
                request.setAttribute("user", user);
                logger.info("Login OK: " + username);
            } else {
                // ❌ Usuario no encontrado
                request.setAttribute("loginSuccess", false);
                request.setAttribute("errorMessage", "Usuario no encontrado");
                logger.warn("Login FAILED: " + username);
            }
        }
    }
}
```

#### 3.8.2. LogoutCommand

**Propósito:** Cerrar sesión del usuario.

**Código simplificado:**
```java
public class LogoutCommand implements Command, LoggerAware, HttpSessionAware {
    private Logger logger;
    private HttpSession session;
    
    @Override
    public void execute() {
        User user = SecurityContext.getUser(session);
        if (user != null) {
            logger.info("Logout: " + user.getUsername());
            SecurityContext.clearUser(session);
        }
    }
}
```

#### 3.8.3. UnauthorizedCommand

**Propósito:** Mostrar error cuando el acceso es denegado.

**Código simplificado:**
```java
public class UnauthorizedCommand implements Command, LoggerAware,
                                           ServletRequestAware, HttpSessionAware {
    private Logger logger;
    private HttpServletRequest request;
    private HttpSession session;
    private String action;  // La acción que fue denegada
    
    public UnauthorizedCommand(String action) {
        this.action = action;
    }
    
    @Override
    public void execute() {
        request.setAttribute("unauthorized", true);
        
        User user = SecurityContext.getUser(session);
        
        if (user == null) {
            // Usuario no logueado
            request.setAttribute("errorMessage", 
                "Debe iniciar sesión para acceder a este recurso");
            request.setAttribute("linkUrl", "login.jsp");
            request.setAttribute("linkText", "Ir a Login");
        } else {
            // Usuario logueado pero sin permisos
            request.setAttribute("errorMessage", 
                "No tiene permisos para acceder a este recurso");
            request.setAttribute("linkUrl", "index.jsp");
            request.setAttribute("linkText", "Volver al Menú");
        }
    }
}
```

---

## 4. Ejemplo Práctico: Proteger un Command

### 4.1. Escenario

Queremos crear un nuevo comando `DeleteBookCommand` que solo pueda ser ejecutado por usuarios con rol `ADMIN`.

### 4.2. Paso 1: Crear el Command

```java
package com.miw.presentation.commands;

import com.miw.infrastructure.logger.Logger;
import com.miw.infrastructure.logger.LoggerAware;
import com.miw.presentation.di.ServletRequestAware;
import com.miw.security.model.User;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Comando para eliminar un libro
 * Solo accesible por ADMIN
 */
public class DeleteBookCommand implements Command, LoggerAware, ServletRequestAware {
    
    // ✅ Dependencia del logger (interfaz propia, NO log4j)
    private Logger logger;
    
    private HttpServletRequest request;
    
    // ✅ Usuario POJO (opcional, inyectado mediante reflexión)
    private User user;
    
    // Parámetro del libro a eliminar (inyectado por populateParameters)
    private String bookId;
    
    // === INYECCIÓN DE DEPENDENCIAS ===
    
    @Override
    public void setLogger(Logger logger) {
        this.logger = logger;
    }
    
    @Override
    public void setServletRequest(HttpServletRequest request) {
        this.request = request;
    }
    
    /**
     * ✅ Inyección POJO (sin interfaz del framework)
     * Este método es completamente opcional
     * Si existe, el controlador lo invocará automáticamente
     */
    public void setUser(User user) {
        this.user = user;
    }
    
    /**
     * Setter para el parámetro bookId
     * Será llamado por populateParameters() si viene en la URL
     */
    public void setBookId(String bookId) {
        this.bookId = bookId;
    }
    
    // === LÓGICA DE NEGOCIO ===
    
    @Override
    public void execute() {
        logger.debug("Executing DeleteBookCommand");
        
        // Podemos usar la información del usuario si la necesitamos
        if (user != null) {
            logger.info("Delete book request by: " + user.getUsername() + 
                       " (role: " + user.getRole() + ")");
        }
        
        if (bookId == null || bookId.trim().isEmpty()) {
            request.setAttribute("error", "ID de libro no especificado");
            logger.warn("Delete failed: no bookId provided");
            return;
        }
        
        try {
            // Aquí iría la lógica real de borrado
            // BookService.deleteBook(bookId);
            
            logger.info("Book deleted successfully: " + bookId);
            request.setAttribute("success", true);
            request.setAttribute("message", "Libro eliminado correctamente");
            
        } catch (Exception e) {
            logger.error("Error deleting book: " + e.getMessage());
            request.setAttribute("error", "Error al eliminar el libro");
        }
    }
}
```

**Puntos importantes:**
1. ✅ **NO depende de log4j**: Usa `Logger` (interfaz propia)
2. ✅ **Implementa `LoggerAware`**: Para recibir el logger
3. ✅ **Inyección POJO del usuario**: Con `setUser(User)` sin interfaz del FW
4. ✅ **No contiene lógica de seguridad**: Es completamente transparente
5. ✅ **Código limpio**: Se centra solo en su responsabilidad (eliminar libro)

### 4.3. Paso 2: Configurar la Autorización

**Archivo:** `src/main/resources/authorization.properties`

```properties
# ... configuraciones existentes ...

# DeleteBookAction: Solo ADMIN puede eliminar libros
DeleteBookAction=ADMIN
```

**¡Eso es todo!** No necesitas tocar el comando, el proxy se encarga de todo.

### 4.4. Paso 3: Registrar el Command en el Controlador

**Archivo:** `src/main/resources/controller.properties`

```properties
# ... comandos existentes ...

DeleteBookAction=com.miw.presentation.commands.DeleteBookCommand
DeleteBookAction.forward=bookDeleted.jsp
```

### 4.5. Paso 4: Probar el Sistema

#### Prueba 1: Usuario ADMIN (user1)

```
1. Login como user1
2. Ir a: /Controller?action=DeleteBookAction&bookId=123

Resultado esperado:
✅ Access GRANTED
✅ Libro eliminado
✅ Forward a bookDeleted.jsp
✅ Log: "Access GRANTED to [DeleteBookAction] for user: user1 (ADMIN)"
```

#### Prueba 2: Usuario USER (user2)

```
1. Login como user2
2. Ir a: /Controller?action=DeleteBookAction&bookId=123

Resultado esperado:
❌ Access DENIED
❌ UnauthorizedCommand ejecutado
❌ Forward a unauthorized.jsp
❌ Mensaje: "No tiene permisos para acceder a este recurso"
❌ Log: "Access DENIED to [DeleteBookAction] for user: user2 (USER)"
```

#### Prueba 3: Usuario no logueado

```
1. Sin hacer login
2. Ir a: /Controller?action=DeleteBookAction&bookId=123

Resultado esperado:
❌ Access DENIED
❌ UnauthorizedCommand ejecutado
❌ Forward a unauthorized.jsp
❌ Mensaje: "Debe iniciar sesión para acceder a este recurso"
❌ Link: "Ir a Login"
❌ Log: "Access DENIED to [DeleteBookAction] for user: anonymous"
```

### 4.6. Ejemplo con Inyección POJO del Usuario

Si queremos que el comando sepa quién está realizando la acción (para auditoría, por ejemplo):

```java
public class DeleteBookCommand implements Command, LoggerAware, ServletRequestAware {
    
    private Logger logger;
    private HttpServletRequest request;
    
    // ⭐ Usuario inyectado automáticamente (POJO)
    private User user;
    
    private String bookId;
    
    /**
     * ✅ Método opcional que será invocado automáticamente por el controlador
     * No requiere implementar ninguna interfaz del framework
     */
    public void setUser(User user) {
        this.user = user;
    }
    
    @Override
    public void execute() {
        // Registrar quién eliminó el libro (auditoría)
        if (user != null) {
            logger.info("DELETE AUDIT: Book " + bookId + 
                       " deleted by " + user.getUsername() + 
                       " (role: " + user.getRole() + ")");
        }
        
        // ... lógica de eliminación ...
    }
}
```

**Ventajas:**
- ✅ El `User` es un POJO puro, sin dependencias del framework
- ✅ El comando NO está obligado a tener `setUser()` (es opcional)
- ✅ Si existe, se inyecta automáticamente mediante reflexión
- ✅ Cumple el requisito: "filosofía POJO (sin obligarle a cumplir con ninguna interfaz del FW)"

### 4.7. Diagrama de Secuencia del Ejemplo

```
Usuario          Browser          ControllerServlet       SecureCommandProxy       SecurityManager       DeleteBookCommand
  |                |                      |                        |                       |                      |
  |  Clic Delete   |                      |                        |                       |                      |
  |--------------->|                      |                        |                       |                      |
  |                |  GET /Controller     |                        |                       |                      |
  |                |--------------------->|                        |                       |                      |
  |                |  ?action=DeleteBook  |                        |                       |                      |
  |                |                      |                        |                       |                      |
  |                |                      | new DeleteBookCommand()|                       |                      |
  |                |                      |--------------------------------------------------->|                   |
  |                |                      |                        |                       |                      |
  |                |                      | new SecureCommandProxy(cmd, "DeleteBookAction") |                      |
  |                |                      |----------------------->|                       |                      |
  |                |                      |                        |                       |                      |
  |                |                      | injectDependencies(cmd)|                       |                      |
  |                |                      |--------------------------------------------------->|                   |
  |                |                      |                        |                       |      setLogger()     |
  |                |                      |                        |                       |      setRequest()    |
  |                |                      |                        |                       |      setUser()       |
  |                |                      |                        |                       |                      |
  |                |                      | proxy.execute()        |                       |                      |
  |                |                      |----------------------->|                       |                      |
  |                |                      |                        | getUser(session)      |                      |
  |                |                      |                        |--------------------   |                      |
  |                |                      |                        |                   |   |                      |
  |                |                      |                        |<-------------------   |                      |
  |                |                      |                        | user = user1 (ADMIN)  |                      |
  |                |                      |                        |                       |                      |
  |                |                      |                        | isAuthorized(user1, "DeleteBookAction") |    |
  |                |                      |                        |---------------------->|                      |
  |                |                      |                        |                       | Busca en authorization.properties |
  |                |                      |                        |                       | DeleteBookAction=ADMIN|
  |                |                      |                        |                       | ✅ user1 es ADMIN    |
  |                |                      |                        |<----------------------|                      |
  |                |                      |                        | return true           |                      |
  |                |                      |                        |                       |                      |
  |                |                      |                        | targetCommand.execute()                     |
  |                |                      |                        |--------------------------------------------->|
  |                |                      |                        |                       |                      |
  |                |                      |                        |                       |    Elimina libro     |
  |                |                      |                        |                       |    book #123         |
  |                |                      |                        |                       |                      |
  |                |                      |                        |<---------------------------------------------|
  |                |                      |<-----------------------|                       |                      |
  |                |                      |                        |                       |                      |
  |                |                      | forward("bookDeleted.jsp")                     |                      |
  |                |<---------------------|                        |                       |                      |
  |                |                      |                        |                       |                      |
  |  Libro Deleted |                      |                        |                       |                      |
  |<---------------|                      |                        |                       |                      |
```

---

## 5. Pruebas y Validación

### 5.1. Matriz de Permisos Implementada

| Acción                  | ADMIN | USER | Sin Login |
|------------------------|-------|------|-----------|
| `LoginAction`          | ✅    | ✅   | ✅        |
| `LogoutAction`         | ✅    | ✅   | ✅        |
| `ShowBooksAction`      | ✅    | ✅   | ❌        |
| `ShowSpecialOfferAction` | ✅  | ❌   | ❌        |
| `DeleteBookAction` (ejemplo) | ✅ | ❌ | ❌      |

### 5.2. Casos de Prueba Ejecutados

#### Caso 1: Login exitoso como ADMIN
```
✅ PASSED
URL: /Controller?action=LoginAction&username=user1
Resultado:
  - Usuario autenticado
  - Sesión creada con User{username='user1', role='ADMIN'}
  - Mensaje: "Login exitoso - Rol: ADMIN"
  - Log: "User logged in successfully: user1 (role: ADMIN)"
```

#### Caso 2: Login exitoso como USER
```
✅ PASSED
URL: /Controller?action=LoginAction&username=user2
Resultado:
  - Usuario autenticado
  - Sesión creada con User{username='user2', role='USER'}
  - Mensaje: "Login exitoso - Rol: USER"
  - Log: "User logged in successfully: user2 (role: USER)"
```

#### Caso 3: Login fallido (usuario inexistente)
```
✅ PASSED
URL: /Controller?action=LoginAction&username=user3
Resultado:
  - Autenticación rechazada
  - Mensaje: "Usuario no encontrado"
  - Log: "Login failed - user not found: user3"
```

#### Caso 4: Acceso a ShowBooks como ADMIN
```
✅ PASSED
Precondición: Login como user1
URL: /Controller?action=ShowBooksAction
Resultado:
  - Acceso permitido
  - Lista de libros mostrada
  - Log: "Access GRANTED to [ShowBooksAction] for user: user1 (ADMIN)"
```

#### Caso 5: Acceso a ShowBooks como USER
```
✅ PASSED
Precondición: Login como user2
URL: /Controller?action=ShowBooksAction
Resultado:
  - Acceso permitido
  - Lista de libros mostrada
  - Log: "Access GRANTED to [ShowBooksAction] for user: user2 (USER)"
```

#### Caso 6: Acceso a ShowBooks sin login
```
✅ PASSED
Precondición: Sin login (sesión vacía)
URL: /Controller?action=ShowBooksAction
Resultado:
  - Acceso DENEGADO
  - Forward a unauthorized.jsp
  - Mensaje: "Debe iniciar sesión para acceder a este recurso"
  - Link: "Ir a Login"
  - Log: "Access DENIED to [ShowBooksAction] for user: anonymous"
```

#### Caso 7: Acceso a ShowSpecialOffer como ADMIN
```
✅ PASSED
Precondición: Login como user1
URL: /Controller?action=ShowSpecialOfferAction
Resultado:
  - Acceso permitido
  - Oferta especial mostrada
  - Log: "Access GRANTED to [ShowSpecialOfferAction] for user: user1 (ADMIN)"
```

#### Caso 8: Acceso a ShowSpecialOffer como USER
```
✅ PASSED (CASO CRÍTICO - REQUISITO ESPECÍFICO)
Precondición: Login como user2
URL: /Controller?action=ShowSpecialOfferAction
Resultado:
  - Acceso DENEGADO ✅
  - Forward a unauthorized.jsp ✅
  - Mensaje: "No tiene permisos para acceder a este recurso" ✅
  - Log: "Access DENIED to [ShowSpecialOfferAction] for user: user2 (USER)" ✅
  
Este caso valida específicamente el requisito:
"user2 sólo la lista de libros -no la oferta del día"
```

#### Caso 9: Acceso a ShowSpecialOffer sin login
```
✅ PASSED
Precondición: Sin login
URL: /Controller?action=ShowSpecialOfferAction
Resultado:
  - Acceso DENEGADO
  - Forward a unauthorized.jsp
  - Mensaje: "Debe iniciar sesión para acceder a este recurso"
  - Link: "Ir a Login"
```

#### Caso 10: Logout
```
✅ PASSED
Precondición: Login como cualquier usuario
URL: /Controller?action=LogoutAction
Resultado:
  - Sesión limpiada
  - Usuario eliminado de SecurityContext
  - Redirección a página principal
  - Log: "Logout: user1"
```


---

**Documento elaborado para el Trabajo 1 del Master en Ingeniería Web**  
**Universidad de Oviedo - Arquitectura de Sitios Web**  
**Versión de la Aplicación:** Amazin 15.0.0  
**Fecha:** Octubre 2024  
**URL de Acceso:** http://156.35.95.57:8080/Amazin_15_0_0/

---

