package com.cadiscatola.api.wrapper.exceptions;

public class InternalException extends Exception {
	private static final long serialVersionUID = 1L;

	public InternalException() {
		super();
	}
	
	public InternalException(String msg) {
		super(msg);
	}
}
