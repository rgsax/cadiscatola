package com.cadiscatola.api.utils.exceptions;

public class UserDoesNotExistException extends com.cadiscatola.api.wrapper.exceptions.UserDoesNotExistException {
	private static final long serialVersionUID = 1L;

	public UserDoesNotExistException(String username) {
		super(username);
	}
	
	@Override
	public String getMessage() {
		return "user " + user + " does not exist";
	}
}
