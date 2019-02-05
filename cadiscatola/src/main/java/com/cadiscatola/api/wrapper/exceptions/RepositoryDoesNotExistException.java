package com.cadiscatola.api.wrapper.exceptions;

public class RepositoryDoesNotExistException extends Exception {
	private static final long serialVersionUID = 1L;
	
	protected String repositoryName;
	
	public String getRepositoryName() {
		return repositoryName;
	}

	public void setRepositoryName(String repositoryName) {
		this.repositoryName = repositoryName;
	}

	public RepositoryDoesNotExistException() { 
		super(); 
	}
	
	public RepositoryDoesNotExistException(String repositoryName) {
		super();
		this.repositoryName = repositoryName;
	}
	
	@Override
	public String getMessage() {
		return repositoryName + " does not exists";
	}
	
}

