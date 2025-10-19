package com.miw.presentation.commands;

import com.miw.presentation.book.BookManagerServiceHelper;
import com.miw.presentation.di.ServletRequestAware;

import jakarta.servlet.http.HttpServletRequest;

public class ShowSpecialOfferCommand extends AbstractCommand implements ServletRequestAware {
	private HttpServletRequest request;
	
	public ShowSpecialOfferCommand( )
	{
	}
	
	@Override
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

	@Override
	public void setServletRequest(HttpServletRequest request) {
		this.request = request;		
	}
	
}
