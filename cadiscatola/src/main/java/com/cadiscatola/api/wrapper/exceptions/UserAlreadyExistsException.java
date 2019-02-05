package com.cadiscatola.api.wrapper.exceptions;

public class UserAlreadyExistsException extends Exception {
	private static final long serialVersionUID = 1L;
	
	protected String user = null;
	
	public UserAlreadyExistsException(String user) {
		super();
		this.setUser(user);
	}
	
	@Override
	public String getMessage() {
		return "user " + user + " doe not exist";
	}

	/**
	 * @return the user
	 */
	public String getUser() {
		return user;
	}

	/**
	 * @param user the user to set
	 */
	public void setUser(String user) {
		this.user = user;
	}
}
