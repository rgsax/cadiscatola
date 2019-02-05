package com.cadiscatola.api.utils;

import java.util.Map;

import com.cadiscatola.api.model.SharedSpace;
import com.cadiscatola.api.model.User;
import com.cadiscatola.api.utils.exceptions.SharedSpaceDoesNotExistException;
import com.cadiscatola.api.wrapper.exceptions.InternalException;

public class SharedSpaceProxy extends SharedSpace{
	private Map<String, Boolean> collaborators = null;
	
	public SharedSpaceProxy() {
		super();
	}
	
	public SharedSpaceProxy(String name, User owner) {
		super(name, owner);
	}
	
	@Override
	public void addCollaborator(User user, Boolean canWrite) throws SharedSpaceDoesNotExistException, InternalException {
		if(collaborators == null)
			collaborators = CloudStorageUtils.getSharedSPaceCollaborators(this);
		collaborators.put(user.getName(), canWrite);
	}
	
	//TODO: versione di GetCollaborators che "forza un'aggiornamento"
	
	@Override
	public Map<String, Boolean> getCollaborators() throws SharedSpaceDoesNotExistException, InternalException {
		if(collaborators == null)
			collaborators = CloudStorageUtils.getSharedSPaceCollaborators(this);
		return collaborators;
	}
	
	@Override
	public void removeCollaborator(User user) throws SharedSpaceDoesNotExistException, InternalException {
		if(collaborators == null)
			collaborators = CloudStorageUtils.getSharedSPaceCollaborators(this);
		
		collaborators.remove(user.getName());
	}
}
