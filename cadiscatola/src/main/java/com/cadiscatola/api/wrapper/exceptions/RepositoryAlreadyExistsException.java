package com.cadiscatola.api.wrapper.exceptions;

public class RepositoryAlreadyExistsException extends Exception {
	private static final long serialVersionUID = 1L;
	
	protected String repositoryName;
	protected String owner;
	
	public String getRepositoryName() {
		return repositoryName;
	}

	public void setRepositoryName(String repositoryName) {
		this.repositoryName = repositoryName;
	}

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public RepositoryAlreadyExistsException() { 
		super(); 
	}
	
	public RepositoryAlreadyExistsException(String repositoryName, String owner) {
		super();
		this.repositoryName = repositoryName;
		this.owner = owner;
	}
	
	@Override
	public String getMessage() {
		return owner + " could not create repository " + repositoryName;
	}
	
}
