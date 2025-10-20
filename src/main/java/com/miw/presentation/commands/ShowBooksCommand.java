package com.miw.presentation.commands;

import com.miw.infrastructure.logger.Logger;
import com.miw.infrastructure.logger.LoggerAware;
import com.miw.presentation.book.BookManagerServiceHelper;
import com.miw.presentation.di.HttpSessionAware;
import com.miw.presentation.di.ServletContextAware;
import com.miw.presentation.di.ServletRequestAware;
import com.miw.security.model.User;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

public class ShowBooksCommand implements Command, LoggerAware, HttpSessionAware, ServletRequestAware, ServletContextAware {
	private Logger logger;
	private HttpServletRequest request;
	//private HttpSession session;
	//private ServletContext context;
	
	// ⭐ Usuario POJO - sin necesidad de implementar interfaz del framework
	// Este campo es completamente opcional y se inyecta automáticamente si existe setUser()
	private User user;

	private String myParameter = null;

	public String getMyParameter() {
		return myParameter;
	}

	public void setMyParameter(String myParameter) {
		logger.debug("Setting myParameter to "+myParameter);
		this.myParameter = myParameter;
	}
	
	/**
	 * ⭐ Inyección POJO del usuario - sin implementar ninguna interfaz del FW
	 * El controlador busca este método mediante reflexión y lo invoca si existe
	 * Si no existe, no pasa nada - es completamente opcional
	 */
	public void setUser(User user) {
		this.user = user;
		logger.debug("User information injected (POJO style): " + user.getUsername());
	}

	public void execute() {
		logger.debug("Executing ShowBooksCommand");
		
		// Usar la información del usuario si está disponible
		if (user != null) {
			logger.info("Books requested by: " + user.getUsername() + " (role: " + user.getRole() + ")");
		} else {
			logger.info("Books requested by anonymous user");
		}
		
		BookManagerServiceHelper helper = new BookManagerServiceHelper();
		try {
			request.setAttribute("books", helper.getBooks());
			// Pasar también el usuario a la vista (opcional)
			request.setAttribute("currentUser", user);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void setServletContext(ServletContext context) {
		//this.context = context;
	}

	public void setServletRequest(HttpServletRequest request) {
		this.request = request;
	}

	public void setHttpSession(HttpSession session) {
		//this.session = session;
	}

	@Override
	public void setLogger(Logger logger) {
		this.logger = logger;
	}
}
