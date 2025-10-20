# Sistema de Autorización - Amazin 15.0.0

## 📋 Descripción

Sistema completo de autenticación y autorización basado en roles implementado mediante patrones de diseño.

## 🎯 Patrones Implementados

### 1. **Singleton Pattern** 
- `SecurityManager`: Gestor centralizado de seguridad con instancia única.

### 2. **Proxy Pattern**
- `SecureCommandProxy`: Intercepta comandos y verifica permisos de forma transparente.
- Los comandos no conocen la existencia del control de seguridad.

### 3. **Dependency Injection**
- **Con interfaces**: `ServletRequestAware`, `HttpSessionAware`, `LoggerAware`
- **POJO (sin interfaces)**: Inyección de `User` mediante reflexión

### 4. **Utility Class**
- `SecurityContext`: Facilita la gestión de usuarios en sesión HTTP

## 🏗️ Arquitectura

```
┌─────────────────────────────────────────────────────────┐
│                   ControllerServlet                     │
│  1. Obtiene comando                                     │
│  2. Crea SecureCommandProxy                             │
│  3. Inyecta dependencias                                │
│  4. Inyecta User (POJO)                                 │
│  5. Ejecuta a través del proxy                          │
└──────────────────┬──────────────────────────────────────┘
                   │
                   ▼
         ┌─────────────────────┐
         │ SecureCommandProxy  │ ← Patrón Proxy
         │  - Verifica permisos│
         │  - Ejecuta comando  │
         └─────────────────────┘
                   │
          ┌────────┴────────┐
          ▼                 ▼
    ┌──────────┐      ┌─────────────────┐
    │ Command  │      │ UnauthorizedCmd │
    │ (Real)   │      │ (Error)         │
    └──────────┘      └─────────────────┘
```

## 📁 Estructura de Archivos

### Modelo y Seguridad
```
src/main/java/com/miw/security/
├── model/
│   └── User.java                    # POJO del usuario
├── SecurityManager.java             # Singleton - Gestión de seguridad
├── SecurityContext.java             # Utility - Gestión de sesión
└── SecureCommandProxy.java          # Proxy - Control de acceso
```

### Comandos
```
src/main/java/com/miw/presentation/commands/
├── LoginCommand.java                # Autenticación
├── LogoutCommand.java               # Cierre de sesión
└── UnauthorizedCommand.java         # Acceso denegado
```

### Configuración
```
src/main/resources/
├── users.properties                 # username=role
└── authorization.properties         # action=roles
```

### Vistas
```
src/main/webapp/
├── login.jsp                        # Formulario de login
├── unauthorized.jsp                 # Página de error
└── index.html                       # Actualizado con link login
```

## ⚙️ Configuración

### users.properties
```properties
# Usuario → Rol
user1=ADMIN
user2=USER
```

### authorization.properties
```properties
# Acción → Roles permitidos (separados por coma)
ShowBooksAction=ADMIN,USER
ShowSpecialOfferAction=ADMIN
```

## 🧪 Casos de Prueba

### Test 1: Login como ADMIN
1. Ir a: http://localhost:8080/Amazin_15_0_0/index.html
2. Click en "Login"
3. Ingresar: **user1**
4. Resultado: ✅ Login exitoso - Rol: ADMIN

### Test 2: Acceso a Catálogo (user1 - ADMIN)
1. Logueado como user1
2. Click en "Show Catalog"
3. Resultado: ✅ Acceso permitido - Muestra libros

### Test 3: Acceso a Ofertas Especiales (user1 - ADMIN)
1. Logueado como user1
2. Click en "Show Special Offers!"
3. Resultado: ✅ Acceso permitido - Muestra oferta

### Test 4: Login como USER
1. Hacer logout
2. Login con: **user2**
3. Resultado: ✅ Login exitoso - Rol: USER

### Test 5: Acceso a Catálogo (user2 - USER)
1. Logueado como user2
2. Click en "Show Catalog"
3. Resultado: ✅ Acceso permitido - Muestra libros

### Test 6: Acceso a Ofertas Especiales (user2 - USER)
1. Logueado como user2
2. Click en "Show Special Offers!"
3. Resultado: ❌ Acceso DENEGADO
   - Muestra: "Acceso No Autorizado"
   - Indica: "Roles permitidos: ADMIN"

### Test 7: Acceso sin login
1. Hacer logout
2. Intentar acceder directamente a una acción
3. Resultado: ❌ Acceso DENEGADO
   - Muestra: "Debe iniciar sesión"

## 📊 Matriz de Permisos

| Acción               | ADMIN | USER | Sin login |
|---------------------|-------|------|-----------|
| ShowBooksAction     | ✅    | ✅   | ❌        |
| ShowSpecialOffer    | ✅    | ❌   | ❌        |
| LoginAction         | ✅    | ✅   | ✅        |
| LogoutAction        | ✅    | ✅   | ❌        |

## 🔧 Extensión del Sistema

### Agregar nuevo usuario
1. Editar `users.properties`:
```properties
user3=MANAGER
```

### Agregar nuevo rol a una acción
2. Editar `authorization.properties`:
```properties
ShowSpecialOfferAction=ADMIN,MANAGER
```

### Crear comando que use información del usuario (POJO)
```java
public class MiComando implements Command {
    private User user; // POJO - sin interfaces
    
    // Este método es opcional - si existe, se inyecta automáticamente
    public void setUser(User user) {
        this.user = user;
    }
    
    public void execute() {
        if (user != null) {
            logger.info("Ejecutado por: " + user.getUsername());
        }
        // ... lógica del comando
    }
}
```

## 🎨 Características de UI

### Mensajes visuales:
- ✅ **Verde**: Login exitoso, acceso permitido
- ❌ **Rojo**: Error de autenticación, acceso denegado
- ℹ️ **Azul**: Información de ayuda

### Navegación:
- Botón "Login" en la barra de navegación
- Links contextuales según el estado del usuario
- Mensajes informativos con roles permitidos

## 🔐 Seguridad Implementada

1. ✅ **Configuración externalizada** (archivos .properties)
2. ✅ **Control de acceso transparente** (Proxy Pattern)
3. ✅ **Separación de responsabilidades** (cada clase tiene un propósito)
4. ✅ **Inyección POJO** (sin acoplamiento al framework)
5. ✅ **Mensajes informativos** (muestra roles requeridos)
6. ✅ **Gestión de sesión** (HttpSession)

## 📝 Notas Importantes

- **Sin contraseña**: Esta versión simplificada no requiere contraseña
- **Thread-safe**: SecurityManager usa synchronized
- **Opcional**: La inyección de User en comandos es completamente opcional
- **Extensible**: Fácil agregar más usuarios, roles y acciones

## 🚀 Deploy y Ejecución

1. Compilar el proyecto:
```bash
mvn clean package
```

2. Desplegar el WAR en Tomcat

3. Acceder a:
```
http://localhost:8080/Amazin_15_0_0/
```

## 📚 Logs

El sistema registra en log4j:
- Intentos de login (éxito/fallo)
- Verificaciones de autorización
- Accesos permitidos/denegados
- Inyección de dependencias

Revisar logs para auditoría de seguridad.

---

**Desarrollado como parte del Master en Ingeniería Web - Universidad de Oviedo**

