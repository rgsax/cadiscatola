package com.cadiscatola.api.wrapper.exceptions;

public class PasswordMismatchException extends Exception {
	private static final long serialVersionUID = 1L;
	
	protected String user = null;
	
	public PasswordMismatchException(String user) {
		super();
		this.user = user;
	}
	
	@Override
	public String getMessage() {
		return "user " + user + " password mismatch";
	}
}
