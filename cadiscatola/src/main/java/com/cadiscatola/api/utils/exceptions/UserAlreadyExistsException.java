package com.cadiscatola.api.utils.exceptions;

public class UserAlreadyExistsException extends com.cadiscatola.api.wrapper.exceptions.UserAlreadyExistsException {
	private static final long serialVersionUID = 1L;

	public UserAlreadyExistsException(String user) {
		super(user);
	}
	
	@Override
	public String getMessage() {
		return "user " + user + " already exists";
	}
}
