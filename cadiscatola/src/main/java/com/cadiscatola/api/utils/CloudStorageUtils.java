package com.cadiscatola.api.utils;

import java.util.ArrayList;
import java.util.Map;

import org.apache.commons.lang3.tuple.ImmutablePair;

import com.cadiscatola.api.model.SharedSpace;
import com.cadiscatola.api.model.User;
import com.cadiscatola.api.utils.exceptions.PasswordMismatchException;
import com.cadiscatola.api.utils.exceptions.SharedSpaceAlreadyExistsException;
import com.cadiscatola.api.utils.exceptions.SharedSpaceDoesNotExistException;
import com.cadiscatola.api.utils.exceptions.UserAlreadyExistsException;
import com.cadiscatola.api.utils.exceptions.UserDoesNotExistException;
import com.cadiscatola.api.wrapper.ServerUtils;
import com.cadiscatola.api.wrapper.exceptions.InternalException;
import com.cadiscatola.api.wrapper.exceptions.RepositoryDoesNotExistException;

public class CloudStorageUtils {
	private CloudStorageUtils() { }
	
	/**Imposta l'indirizzo del server cloud
	 * ATTENZIONE: La mancata inizializzazione delle coordinate del server
	 * non garantisce il funzionamento delle api
	 * 
	 * @param ip
	 */
	public static void setCloudServerIp(String ip) {
		setCloudServerFullCoords(ip, 8080, "admin", "admin");
	}
	
	/**Imposta le coordinate del server cloud
	 * ATTENZIONE: La mancata inizializzazione delle coordinate del server
	 * non garantisce il funzionamento delle api
	 * 
	 * @param ip
	 * @param port
	 * @param adminUsername
	 * @param adminPassword
	 */
	public static void setCloudServerFullCoords(String ip, int port, String adminUsername, String adminPassword) {
		ServerUtils.setServerCoords(ip, port, adminUsername, adminPassword);
	}
	
	/**Imposta l?URL del server cloud
	 * ATTENZIONE: La mancata inizializzazione delle coordinate del server
	 * non garantisce il funzionamento delle api
	 * 
	 * @param URL
	 */
	public static void setCloudServerURL(String URL) {
		ServerUtils.setServerURL(URL);
	}
	
	/**	Crea un utente. La password deve essere in chiaro (viene applicato l'hash durante la creazione).
	 * 
	 * @param username Nome utente
	 * @param password Password 
	 * @return Un oggetto {@link com.cadiscatola.model.User#User}
	 * @throws UserAlreadyExistsException 
	 * 
	 */
	public static User createUser(String username, String password) throws InternalException, UserAlreadyExistsException {
		boolean status = false;
		try {
			status = ServerUtils.createUser(username, password);
		} catch (com.cadiscatola.api.wrapper.exceptions.UserAlreadyExistsException e) {
			throw new UserAlreadyExistsException(username);
		}
		User newUser = null;
		
		if(status)
			newUser = new User(username, password);
		
		return newUser;
	}
	
	/** Elimina un utente. 
	 * 
	 * 
	 * @param user Nickname dell'utente da eliminare.
	 * @return
	 * @throws InternalException
	 * @throws UserDoesNotExistException 
	 */
	public static boolean deleteUser(User user) throws InternalException, UserDoesNotExistException {
		try {
			return ServerUtils.deleteUser(user.getName());
		} catch (com.cadiscatola.api.wrapper.exceptions.UserDoesNotExistException e) {
			throw new UserDoesNotExistException(user.getName());
		}
	}
	
	/**	Modifica la password di un utente.
	 * 
	 * @param user
	 * @param newPassword
	 * @return
	 * @throws UserDoesNotExistException
	 * @throws PasswordMismatchException
	 * @throws InternalException
	 */
	public static boolean modifyPassword(User user, String newPassword) throws UserDoesNotExistException, PasswordMismatchException, InternalException {
		boolean status = false;
		try {
			status = ServerUtils.modifyPassword(user.getName(), user.getPassword(), newPassword);
		} catch (com.cadiscatola.api.wrapper.exceptions.UserDoesNotExistException e) {
			throw new UserDoesNotExistException(user.getName());
		} catch (com.cadiscatola.api.wrapper.exceptions.PasswordMismatchException e) {
			throw new PasswordMismatchException(user.getName());
		}
		
		return status;
	}
	
	public static User getUser(String username, String password) throws PasswordMismatchException, UserDoesNotExistException, InternalException {
		User user = null;
			
		try {
			if(ServerUtils.authenticateUser(username, password))
				user = new User(username, password);
		} catch (com.cadiscatola.api.wrapper.exceptions.UserDoesNotExistException e) {
			throw new UserDoesNotExistException(username);
		} catch (com.cadiscatola.api.wrapper.exceptions.PasswordMismatchException e) {
			throw new PasswordMismatchException(username);
		}
		
		
		return user;
	}
	
	
	/** Crea un nuovo Sharespace e gli associa un proprietario.
	 * 
	 * @param name
	 * @param owner
	 * @return
	 * @throws InternalException
	 * @throws SharedSpaceAlreadyExistsException
	 */
	public static SharedSpace createSharedSpace(String name, User owner) throws InternalException, SharedSpaceAlreadyExistsException {
		boolean status = false;
		SharedSpace sharedSpace = new SharedSpaceProxy(name, owner);
		try {
			status = ServerUtils.createRepository(Utils.getSharedSpaceName(sharedSpace), owner.getName());
		} catch(com.cadiscatola.api.wrapper.exceptions.RepositoryAlreadyExistsException e) {
			throw new SharedSpaceAlreadyExistsException(name, owner.getName());
		}
		
		if(!status)
			return null;
		
		return sharedSpace;
	}
	
	/** Elimina uno Sharespace (MA NON LA COPIA LOCALE!)
	 * 
	 * @param sharedSpace
	 * @return
	 * @throws InternalException
	 * @throws SharedSpaceDoesNotExistException
	 */
	public static boolean deleteSharedSpace(SharedSpace sharedSpace) throws InternalException, SharedSpaceDoesNotExistException {
		boolean status = false;
		try {
			String owner = sharedSpace.getOwner().getName();
			status = ServerUtils.deleteRepository(Utils.getSharedSpaceName(sharedSpace), owner);
		} catch (com.cadiscatola.api.wrapper.exceptions.RepositoryDoesNotExistException e) {
			throw new SharedSpaceDoesNotExistException(sharedSpace.getName());
		}
		
		return status;
	}
	
	
	/** Consente all'utente di utilizzare lo Sharespace in sola lettura.
	 * 
	 * @param user
	 * @param sharedSpace
	 * @return
	 * @throws InternalException
	 * @throws UserDoesNotExistException
	 * @throws SharedSpaceDoesNotExistException
	 */
	public static boolean setReadOnlyUser(User user, SharedSpace sharedSpace) throws InternalException, UserDoesNotExistException, SharedSpaceDoesNotExistException {
		boolean status = false;
		try {
			status = ServerUtils.setReadForUser(Utils.getSharedSpaceName(sharedSpace), user.getName());
		} catch (com.cadiscatola.api.wrapper.exceptions.UserDoesNotExistException e) {
			throw new UserDoesNotExistException(user.getName());
		} catch (com.cadiscatola.api.wrapper.exceptions.RepositoryDoesNotExistException e) {
			throw new SharedSpaceDoesNotExistException(sharedSpace.getName());
		}
		
		if(!status)
			return false;
		
		sharedSpace.addCollaborator(user, false);
		return true;
	}
	
	/** Consente all'utente di modificare lo Sharespace.
	 * 
	 * @param user
	 * @param sharedSpace
	 * @return
	 * @throws UserDoesNotExistException
	 * @throws SharedSpaceDoesNotExistException
	 * @throws InternalException
	 */
	public static boolean setReadWriteUser(User user, SharedSpace sharedSpace) throws UserDoesNotExistException, SharedSpaceDoesNotExistException, InternalException {
		boolean status = false;
		try {
			status = ServerUtils.setReadWriteForUser(Utils.getSharedSpaceName(sharedSpace), user.getName());
		} catch (com.cadiscatola.api.wrapper.exceptions.UserDoesNotExistException e) {
			throw new UserDoesNotExistException(user.getName());
		} catch (com.cadiscatola.api.wrapper.exceptions.RepositoryDoesNotExistException e) {
			throw new SharedSpaceDoesNotExistException(sharedSpace.getName());
		}
		
		if(!status)
			return false;
		
		sharedSpace.addCollaborator(user, true);
		return true;
	}
	
	/** Rimuove un utente da uno Sharespace, togliendogli ogni permesso.
	 * 
	 * @param user
	 * @param sharedSpace
	 * @return
	 * @throws UserDoesNotExistException
	 * @throws SharedSpaceDoesNotExistException
	 * @throws InternalException
	 */
	public static boolean removeCollaborator(User user, SharedSpace sharedSpace) throws UserDoesNotExistException, SharedSpaceDoesNotExistException, InternalException {
		boolean status = false;
		
		try {
			status = ServerUtils.setHiddenForUser(Utils.getSharedSpaceName(sharedSpace), user.getName());
		} catch (com.cadiscatola.api.wrapper.exceptions.UserDoesNotExistException e) {
			throw new UserDoesNotExistException(user.getName());
		} catch (com.cadiscatola.api.wrapper.exceptions.RepositoryDoesNotExistException e) {
			throw new SharedSpaceDoesNotExistException(sharedSpace.getName());
		}
		
		if(status)
			sharedSpace.removeCollaborator(user);
		
		return status;
	}
	
	/** Restituisce una mappa M tale che:
	 * 	- le chiavi rappresentino gli utenti che hanno accesso allo Sharespace S
	 *  - M(k) è True se l'utente dal nickname k ha diritto di scrittura su S, False altrimenti 
	 * 
	 * @param sharedSpace
	 * @return
	 * @throws SharedSpaceDoesNotExistException
	 * @throws InternalException
	 */
	public static Map<String, Boolean> getSharedSPaceCollaborators(SharedSpace sharedSpace) throws SharedSpaceDoesNotExistException, InternalException {
		Map<String, Boolean> collaborators = null;
		
		try {
			collaborators =  ServerUtils.getCollaborators(Utils.getSharedSpaceName(sharedSpace));
		} catch (com.cadiscatola.api.wrapper.exceptions.RepositoryDoesNotExistException e) {
			throw new SharedSpaceDoesNotExistException(sharedSpace.getName());
		}
		
		return collaborators;
	}
	
	/** Restituisce True se l'utente ha permesso di lettura sullo Sharespace, False altrimenti.
	 * 
	 * @param user
	 * @param sharedSpace
	 * @return
	 * @throws SharedSpaceDoesNotExistException
	 * @throws InternalException
	 */
	public static boolean userCanReadSharedSpace(User user, SharedSpace sharedSpace) throws SharedSpaceDoesNotExistException, InternalException {
		boolean canRead = false;
		
		try {
			canRead = ServerUtils.canReadToReposiory(user.getName(), Utils.getSharedSpaceName(sharedSpace));
		} catch (RepositoryDoesNotExistException e) {
			throw new SharedSpaceDoesNotExistException(sharedSpace.getName());
		}
		
		return canRead;
	}
	/** Restituisce True se l'utente ha permesso di scrittura sullo Sharespace, False altrimenti.
	 * 
	 * @param user
	 * @param sharedSpace
	 * @return
	 * @throws SharedSpaceDoesNotExistException
	 * @throws InternalException
	 */
	public static boolean userCanWriteToSharedSpace(User user, SharedSpace sharedSpace) throws SharedSpaceDoesNotExistException, InternalException {
		boolean canWrite = false;
		
		try {
			canWrite = ServerUtils.canWriteToReposiory(user.getName(), Utils.getSharedSpaceName(sharedSpace));
		} catch (RepositoryDoesNotExistException e) {
			throw new SharedSpaceDoesNotExistException(sharedSpace.getName());
		}
		
		return canWrite;
	}
	
	/** Restituisce una lista degli Sharespace accessibili all'utenti, cioè Sharespace sui l'utente ha peremssi di 
	 * scrittura o di lettura.
	 * 
	 * @param user
	 * @return
	 * @throws InternalException
	 */
	public static ArrayList<ImmutablePair<SharedSpace, Boolean>> getAccessibleSharedSpaces(User user) throws InternalException {
		ArrayList<ImmutablePair<SharedSpace, Boolean>> sharedSpaces = new ArrayList<>();
		
		Map<String, ImmutablePair<String, Boolean>> sharedSpacesValues = ServerUtils.getUserAccessibleRepository(user.getName());
		for(String sharedSpace : sharedSpacesValues.keySet()) {
			String sharedSpaceName = Utils.getRealSharedSpaceName(sharedSpace, sharedSpacesValues.get(sharedSpace).left);
			User ownerUser = new User();
			ownerUser.setName(sharedSpacesValues.get(sharedSpace).left);
			
			sharedSpaces.add(new ImmutablePair<SharedSpace, Boolean>(new SharedSpaceProxy(sharedSpaceName, ownerUser), sharedSpacesValues.get(sharedSpace).right));
		}
		
		return sharedSpaces;
	}
}
