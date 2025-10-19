package com.miw.presentation.commands;

import com.miw.infrastructure.logger.Logger;
import com.miw.infrastructure.logger.LoggerAware;
import com.miw.presentation.book.BookManagerServiceHelper;
import com.miw.presentation.di.ServletRequestAware;

import jakarta.servlet.http.HttpServletRequest;

public class ShowSpecialOfferCommand implements Command, LoggerAware, ServletRequestAware {
	private Logger logger;
	private HttpServletRequest request;
	
	public ShowSpecialOfferCommand( )
	{
	}
	
	public void execute()
	{
		logger.debug("Executing "+this.getClass().getName());
		BookManagerServiceHelper helper = new BookManagerServiceHelper();
		try {
			request.setAttribute("book", helper.getSpecialOffer());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void setServletRequest(HttpServletRequest request) {
		this.request = request;		
	}

	@Override
	public void setLogger(Logger logger) {
		this.logger = logger;
	}
}
