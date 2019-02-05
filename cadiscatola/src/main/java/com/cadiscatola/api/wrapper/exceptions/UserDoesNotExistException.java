package com.cadiscatola.api.wrapper.exceptions;

public class UserDoesNotExistException extends Exception {
	private static final long serialVersionUID = 1L;
	
	protected String user;
	
	public String getRepositoryName() {
		return user;
	}

	public void setRepositoryName(String user) {
		this.user = user;
	}

	public UserDoesNotExistException() { 
		super(); 
	}
	
	public UserDoesNotExistException(String user) {
		super();
		this.user = user;
	}
	
	@Override
	public String getMessage() {
		return "user " + user + " does not exists";
	}
	
}

