package com.cadiscatola.api.model;

import java.util.Map;

import com.cadiscatola.api.utils.exceptions.SharedSpaceDoesNotExistException;
import com.cadiscatola.api.wrapper.exceptions.InternalException;

public class SharedSpace {
	private String name = null;
	private User owner = null;
	
	public SharedSpace() { }
	public SharedSpace(String name, User owner) {
		this.name = name;
		this.owner = owner;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public User getOwner() {
		return owner;
	}
	
	public void setOwner(User owner) {
		this.owner = owner;
	}
	
	/** Restituisce una mappa M<S, K> tale che:
	 * 		- M.get(S) è True se l'utente dal nickname S ha permessi di scrittura sul repository, False altrimenti
	 * 
	 * 
	 * @return
	 * @throws SharedSpaceDoesNotExistException
	 * @throws InternalException
	 */
	public Map<String, Boolean> getCollaborators() throws SharedSpaceDoesNotExistException, InternalException {
		return null;
	}
	
	/** Aggiunge un collaboratore allo Sharespace
	 * 
	 * @param user
	 * @param canWrite True se l'utente ha permessi di scrittura, False altrimenti 
	 * @throws SharedSpaceDoesNotExistException
	 * @throws InternalException
	 */
	public void addCollaborator(User user, Boolean canWrite) throws SharedSpaceDoesNotExistException, InternalException {	}
	
	/** Rimuove un collaboratore dallo Sharespace.
	 * NON RIMUOVE LA COPIA LOCALE DELL'UTENTE.
	 * 
	 * @param user
	 * @throws SharedSpaceDoesNotExistException
	 * @throws InternalException
	 */
	public void removeCollaborator(User user) throws SharedSpaceDoesNotExistException, InternalException { }

	@Override
	public String toString() {
		return name;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SharedSpace other = (SharedSpace) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (owner == null) {
			if (other.owner != null)
				return false;
		} else if (!owner.equals(other.owner))
			return false;
		return true;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((owner == null) ? 0 : owner.hashCode());
		return result;
	}
}
