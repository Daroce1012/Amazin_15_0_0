package com.miw.presentation.controller;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.miw.infrastructure.logger.LoggerAware;
import com.miw.presentation.commands.Command;
import com.miw.presentation.commands.UnauthorizedCommand;
import com.miw.presentation.di.HttpSessionAware;
import com.miw.presentation.di.ServletContextAware;
import com.miw.presentation.di.ServletRequestAware;
import com.miw.security.SecureCommandProxy;
import com.miw.security.SecurityContext;
import com.miw.security.model.User;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(value = "/Controller")
public class ControllerServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	protected Logger logger = LogManager.getLogger(getClass());

	Command command = null;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		// We execute the received command
		String action = req.getParameter("action");
		if (action != null) {
			logger.debug("Executing action " + action);

			command = ControllerConfigurationManager.getInstance().getCommand(action);

			// We execute the command and redirect
			if (command != null) {
				
				// 1. Crear comando de acceso no autorizado para este action
				UnauthorizedCommand unauthorizedCommand = new UnauthorizedCommand(action);
				injectDependencies(unauthorizedCommand, req);
				
				// 2. Envolver el comando en un proxy de seguridad
				// Este proxy verificará permisos antes de ejecutar el comando real
				Command secureCommand = new SecureCommandProxy(
					command, 
					action, 
					req.getSession(),
					unauthorizedCommand
				);
				
				// 3. Inyectar dependencias estándar del framework (con interfaces)
				injectDependencies(command, req);
				
				// 4. Inyectar información del usuario (POJO style - sin interfaces)
				injectUserInformation(command, req);

				// 5. Popular parámetros de la petición
				populateParameters(req, resp);

				// 6. Ejecutar el comando (a través del proxy de seguridad)
				secureCommand.execute();

				// 7. Determinar el forward correcto
				String forward;
				if (req.getAttribute("unauthorized") != null && (Boolean)req.getAttribute("unauthorized")) {
					// Si hubo un error de autorización, ir a la página de error
					forward = "unauthorized.jsp";
				} else {
					// Si todo fue bien, ir a la página de éxito configurada
					forward = ControllerConfigurationManager.getInstance().getForward(action);
				}
				
				// 8. Forward a la vista correspondiente
				RequestDispatcher dispatcher = req.getRequestDispatcher(forward);
				dispatcher.forward(req, resp);
			}
		}

	}
	
	/**
	 * Inyecta dependencias estándar del framework en un comando
	 * Usa el patrón de inyección basado en interfaces
	 * 
	 * @param cmd El comando donde inyectar
	 * @param req La petición HTTP
	 */
	private void injectDependencies(Command cmd, HttpServletRequest req) {
		if (cmd instanceof ServletRequestAware) {
			logger.debug("Injecting request in command");
			((ServletRequestAware) cmd).setServletRequest(req);
		}
		if (cmd instanceof HttpSessionAware) {
			logger.debug("Injecting session in command");
			((HttpSessionAware) cmd).setHttpSession(req.getSession());
		}
		if (cmd instanceof ServletContextAware) {
			logger.debug("Injecting context in command");
			((ServletContextAware) cmd).setServletContext(req.getServletContext());
		}
		if (cmd instanceof LoggerAware) {
			logger.debug("Injecting logger in command");
			((LoggerAware) cmd).setLogger(new com.miw.infrastructure.logger.LogManager(cmd.getClass()));
		}
	}
	
	/**
	 * Inyecta información del usuario de forma POJO (sin interfaces)
	 * Busca un método setUser(User) mediante reflexión
	 * Si el comando no tiene este método, simplemente no hace nada
	 * 
	 * Esto permite que los comandos reciban información del usuario
	 * sin tener que implementar ninguna interfaz del framework
	 * 
	 * @param cmd El comando donde inyectar
	 * @param req La petición HTTP
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
					logger.debug("Injecting user information in command (POJO style): " + user.getUsername());
					setUserMethod.invoke(cmd, user);
				}
			}
		} catch (NoSuchMethodException e) {
			// El comando no tiene setUser() → OK, no pasa nada
			// La inyección de usuario es opcional
		} catch (IllegalAccessException | InvocationTargetException e) {
			logger.error("Error injecting user information", e);
		}
	}

	/**
	 * Popula automáticamente los parámetros de la petición HTTP en el comando
	 * Busca setters que correspondan con los nombres de los parámetros
	 * 
	 * Por ejemplo: si hay un parámetro "username", busca setUsername(String)
	 * 
	 * @param req La petición HTTP
	 * @param resp La respuesta HTTP
	 */
	private void populateParameters(HttpServletRequest req, HttpServletResponse resp) {
		for (String s : req.getParameterMap().keySet()) {
			try {
				Method m = command.getClass().getMethod("set" + s.substring(0, 1).toUpperCase() + s.substring(1),
						String.class);
				if (m != null) {
					logger.debug("Found expected parameter " + "set" + s.substring(0, 1).toUpperCase() + s.substring(1)
							+ ", populating the value");
					try {
						m.invoke(command, req.getParameter(s));
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					} catch (IllegalArgumentException e) {
						e.printStackTrace();
					} catch (InvocationTargetException e) {
						e.printStackTrace();
					}
				}
			} catch (NoSuchMethodException e) {
				// No existe el setter, ignorar
			} catch (SecurityException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		this.doGet(req, resp);
	}
}
