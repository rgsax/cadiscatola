package com.cadiscatola.api.wrapper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import com.cadiscatola.api.wrapper.exceptions.InternalException;
import com.cadiscatola.api.wrapper.exceptions.PasswordMismatchException;
import com.cadiscatola.api.wrapper.exceptions.RepositoryAlreadyExistsException;
import com.cadiscatola.api.wrapper.exceptions.RepositoryDoesNotExistException;
import com.cadiscatola.api.wrapper.exceptions.UserAlreadyExistsException;
import com.cadiscatola.api.wrapper.exceptions.UserDoesNotExistException;
import com.gitblit.Constants.AccessPermission;
import com.gitblit.Constants.PermissionType;
import com.gitblit.Constants.RegistrantType;
import com.gitblit.models.RegistrantAccessPermission;
import com.gitblit.models.RepositoryModel;
import com.gitblit.models.UserModel;
import com.gitblit.utils.RpcUtils;
import com.gitblit.utils.StringUtils;

public class ServerUtils {
	public static String HostURL = "http://localhost:8080/";
	private static String AdminName = "admin";
	private static char[] AdminPwd = "admin".toCharArray();
	
	private ServerUtils() { }
	
	public static void setServerCoords(String ip, int port, String adminUsername, String adminPassword) {
		HostURL = "http://" + ip + ":" + port + "/";
		AdminName = adminUsername;
		AdminPwd = adminPassword.toCharArray();
	}
	
	public static void setServerURL(String URL) {
		if(!URL.endsWith("/"))
			URL += "/";
		HostURL = URL;
	}
	
	/** Crea un repository (utilizzando le credenziali dell'admin) e gli associa un proprietario
	 * 
	 * @param name Nome delrepository
	 * @param owner Nome del proprietario del repository
	 * @return
	 * @throws RepositoryAlreadyExistsException
	 * @throws InternalException 
	 */
	public static boolean createRepository(String name, String owner) throws RepositoryAlreadyExistsException, InternalException {
		RepositoryModel repo = new BasicRepository(name, "", owner, new Date());
		
		boolean status = false;
		
		try {
			status = RpcUtils.createRepository(repo, HostURL, AdminName, AdminPwd);
		} catch (IOException e) {
			throw new InternalException("gitblit api exception for createRepository");
		}
			
		if(!status) 
			throw new RepositoryAlreadyExistsException(name, owner);
			
		initialCommit(name, owner);
		return status;
	}

	@Deprecated
	/** Aggiunge un proprietario ad un repository 
	 * 
	 * @param owner
	 * @param repo
	 * @return
	 * @throws IOException
	 */
	public static boolean addOwner(String owner, String repo) throws IOException {
		RepositoryModel repository = RpcUtils.getRepositories(HostURL, AdminName, AdminPwd).get(toRepoUrl(repo));
		repository.owners.add(owner);
		return RpcUtils.updateRepository(repository.name, repository, HostURL, AdminName, AdminPwd);
	}
	
	@Deprecated
	/** Rimuovere un proprietario da un repository 
	 * 
	 * @param user
	 * @param repo
	 * @return
	 * @throws IOException
	 */
	public static boolean removeOwner(String user, String repo) throws IOException {
		RepositoryModel repository = RpcUtils.getRepositories(HostURL, AdminName, AdminPwd).get(toRepoUrl(repo));
		repository.removeOwner(user);
		return RpcUtils.updateRepository(repository.name, repository, HostURL, AdminName, AdminPwd);
	}
	
	/**	Elimina un repository 
	 * 
	 * @param name
	 * @param owner
	 * @return
	 * @throws IOException
	 * @throws RepositoryDoesNotExistException
	 * @throws InternalException 
	 */
	public static boolean deleteRepository(String name, String owner) throws RepositoryDoesNotExistException, InternalException  {
		RepositoryModel repo = getRepositoryModel(name);
		if(!repo.owners.contains(owner)) {
			throw new RepositoryDoesNotExistException(name);
		}
		boolean status = false;
		
		try {
			status = RpcUtils.deleteRepository(repo, HostURL, AdminName, AdminPwd);
		} catch (IOException e) {
			throw new InternalException("gitblit api exception for deleteRepository");
		}
		
		return status;
	}
	
	/** Crea un utente dal server Gitblit.
	 * 
	 * @param name
	 * @param pwd
	 * @return
	 * @throws IOException
	 * @throws UserAlreadyExistsException
	 * @throws InternalException 
	 */
	public static boolean createUser(String name, String pwd) throws UserAlreadyExistsException, InternalException {
		UserModel user = new UserModel(name);
		
		try {
			if(RpcUtils.getUsers(HostURL, AdminName, AdminPwd).contains(user))
				throw new UserAlreadyExistsException(name);
		} catch (IOException e) {
			throw new InternalException("gitblit api exception for getUsers in createUser");
		}
		user.password = toPassword(pwd);
		boolean status = false;
		
		try {
			status = RpcUtils.createUser(user, HostURL, AdminName, AdminPwd);
		} catch (IOException e) {
			throw new InternalException("gitblit api exception for createUser");
		}
		
		return status;
	}
	
	/** Elimina un utente dal server Gitblit.
	 * 
	 * @param name
	 * @return
	 * @throws IOException
	 * @throws UserDoesNotExistException
	 * @throws InternalException 
	 */
	public static boolean deleteUser(String name) throws UserDoesNotExistException, InternalException {
		UserModel user = new UserModel(name);
		boolean status = false;
		
		try {
			status = RpcUtils.deleteUser(user, HostURL, AdminName, AdminPwd);
		} catch (IOException e) {
			throw new InternalException("gitblit api exception for deleteUser");
		}
		
		if(!status)
			throw new UserDoesNotExistException(name);
		
		return status;
	}
	
	/**	Modifica la password di un utente (effettua controlli sulla password corrente)
	 * 
	 * @param username
	 * @param oldPassword
	 * @param newPassword
	 * @return
	 * @throws IOException
	 * @throws PasswordMismatchException
	 * @throws UserDoesNotExistException
	 * @throws InternalException 
	 */
	public static boolean modifyPassword(String username, String oldPassword, String newPassword) throws PasswordMismatchException, UserDoesNotExistException, InternalException {
		UserModel user = getUser(username);
		if(!user.password.equals(toPassword(oldPassword))) {
			throw new PasswordMismatchException(username);
		}
		
		boolean status = modifyPassword(user, toPassword(newPassword));
		
		return status;
	}
	
	/**	Modifica la password di un utente (non effettua controlli sulla password corrente).
	 * 
	 * @param user
	 * @param newPassword
	 * @return
	 * @throws InternalException 
	 * @throws IOException
	 */
	private static boolean modifyPassword(UserModel user, String newPassword) throws InternalException {
		user.password = newPassword;
		boolean status = false;
		
		try {
			status = RpcUtils.updateUser(user.username, user, HostURL, AdminName, AdminPwd);
		} catch (IOException e) {
			throw new InternalException("gitblit api exception for modifyPassword");
		}
		
		return status;
	}
	
	/** Associa ad un utente Gitblit i permessi di push, clone su un repository 
	 * 
	 * @param repository
	 * @param collaborator
	 * @return
	 * @throws IOException
	 * @throws RepositoryDoesNotExistException
	 * @throws UserDoesNotExistException
	 */
	public static boolean setReadWriteForUser(String repository, String collaborator) throws InternalException, RepositoryDoesNotExistException, UserDoesNotExistException {
		return setUserPermissionOnRepository(repository, collaborator, Permissions.RW);
	}
	
	/** Associa ad un utente Gitblit i permessi di clone, pull su un repository 
	 * 
	 * @param repository
	 * @param collaborator
	 * @return
	 * @throws IOException
	 * @throws RepositoryDoesNotExistException
	 * @throws UserDoesNotExistException
	 */
	public static boolean setReadForUser(String repository, String collaborator) throws InternalException, RepositoryDoesNotExistException, UserDoesNotExistException {
		return setUserPermissionOnRepository(repository, collaborator, Permissions.R);
	}
	
	/** Rimuove ogni permesso (clone, pull, push) ad un utente Gitblit per un repository 
	 * 
	 * @param repository
	 * @param collaborator
	 * @return
	 * @throws IOException
	 * @throws RepositoryDoesNotExistException
	 * @throws UserDoesNotExistException
	 */
	public static boolean setHiddenForUser(String repository, String collaborator) throws InternalException, RepositoryDoesNotExistException, UserDoesNotExistException {
		return setUserPermissionOnRepository(repository, collaborator, Permissions.HIDDEN);
	}
	
	/** Associa ad un utente Gitblit dei permessi per un determinato repository.
	 * 
	 * @param repository 
	 * @param collaborator
	 * @param accessPermissions array di oggetti che rappresentano dei permessi disponibili per un repository 
	 * @return
	 * @throws IOException
	 * @throws RepositoryDoesNotExistException
	 * @throws UserDoesNotExistException
	 */
	public static boolean setUserPermissionOnRepository(String repository, String collaborator, AccessPermission[] accessPermissions) throws InternalException, RepositoryDoesNotExistException, UserDoesNotExistException {
		RepositoryModel repo = getRepositoryModel(repository);
		UserModel userCollab = getUser(collaborator);
		List<RegistrantAccessPermission> permissions = new ArrayList<>();
		
		for(AccessPermission ap : accessPermissions)
			permissions.add(
					new RegistrantAccessPermission(userCollab.getName(), ap, 
							PermissionType.TEAM, RegistrantType.USER, "", true)
			);
		
		boolean status = false;
		
		try {
			status = RpcUtils.setRepositoryMemberPermissions(repo, permissions, HostURL, AdminName, AdminPwd);
		} catch (IOException e) {
			throw new InternalException("gitblit api exception for setRepositoryMemberPermissions");
		}
		
		return status;
	}
	
	public static boolean authenticateUser(String username, String password) throws InternalException, UserDoesNotExistException, PasswordMismatchException {
		UserModel user = getUser(username);
		if(!user.password.equals(toPassword(password)))
			throw new PasswordMismatchException(username);
		
		return true;		
	}
	
	/** Restituisce un hashmap che associa ad ogni utente i propri permessi per un determinato repository.
	 *  L'hashmap conterrà solo gli utenti che hanno (almeno) permessi di clone/pull sul repository
	 *  (-> gli utenti con AccessPermission VIEW non sono presenti nell'hashmap) 
	 * 
	 * @param repositoryName
	 * @return
	 * @throws IOException
	 * @throws RepositoryDoesNotExistException
	 */
	public static Map<String, Boolean> getCollaborators(String repositoryName) throws InternalException, RepositoryDoesNotExistException {
		Map<String, Boolean> collaborators = new HashMap<>();
		
		RepositoryModel repository = getRepositoryModel(repositoryName);
		List<RegistrantAccessPermission> permissions = null;
		
		permissions = getRepositoryMemberPermissions(repository);
		
		for(RegistrantAccessPermission permission : permissions) {
			if(!permission.isAdmin() && !permission.isOwner() && permission.toString().contains("R")) {
				boolean canWrite = permission.toString().contains("W");
				collaborators.put(permission.registrant, canWrite);
			}
		}
		
		return collaborators;
	}
	
	public static String toRepoUrl(String name) {
		return HostURL + "r/" + name + ".git";
	}
	
	/** Restituisce la codifica MD5 di una stringa
	 * 
	 * @param password
	 * @return
	 */
	static String toPassword(String password) {
		return "MD5:" + StringUtils.getMD5(password);
	}
	
	/** Restituisce lo UserModel dell'utente con un dato nickname. Un nickname è unico.
	 * 
	 * @param username
	 * @return
	 * @throws IOException
	 * @throws UserDoesNotExistException
	 */
	private static UserModel getUser(String username) throws InternalException, UserDoesNotExistException {
		UserModel user = null;
		try {
			user = (UserModel) RpcUtils.getUsers(HostURL, AdminName, AdminPwd)
					.stream()
					.filter(p -> p.username.equals(username))
					.findFirst().get();
		}
		catch (NoSuchElementException e) {
			throw new UserDoesNotExistException(username);
		} catch (IOException e) {
			throw new InternalException("gitblit api exception for getUser");
		}
		
		return user;
		/*
		 * RpcUtils.getUser(...) non restituiva il valore corretto
		 */
	}
	
	/** Restituisce True se l'utente ha i permessi di scrittura (può pushare) sul repository, False altrimenti.
	 * 
	 * @param user
	 * @param repositoryName
	 * @return
	 * @throws InternalException
	 * @throws RepositoryDoesNotExistException
	 */
	public static boolean canWriteToReposiory(String user, String repositoryName) throws InternalException, RepositoryDoesNotExistException {
		Boolean canWrite = false;
		
		Map<String, Boolean> permissions = getCollaborators(repositoryName);
		
		canWrite = permissions.get(user);
		
		if(canWrite == null || !canWrite.booleanValue())
			return false;
		
		return true;
	}
	
	/** Restituisce True se l'utente ha i permessi di lettura (può clonare, può pullare) sul repository, False altrimenti.
	 * 
	 * @param user
	 * @param repositoryName
	 * @return
	 * @throws InternalException
	 * @throws RepositoryDoesNotExistException
	 */
	public static boolean canReadToReposiory(String user, String repositoryName) throws InternalException, RepositoryDoesNotExistException {
		Boolean canWrite = false;
		
		Map<String, Boolean> permissions = getCollaborators(repositoryName);
		
		canWrite = permissions.get(user);
		
		return canWrite != null;
	}
	
	/** Restituisce una mappa dei repository in cui l'utente ha accesso (può clonare/può pullare, può pushare) 
	 * 
	 * @param user
	 * @return
	 * @throws InternalException
	 */
	public static Map<String, ImmutablePair<String, Boolean>> getUserAccessibleRepository(String user) throws InternalException {
		Map<String, ImmutablePair<String, Boolean>> repoAndOwner = new HashMap<>();
		
		for(RepositoryModel repository : getRepositories()) {
			for(RegistrantAccessPermission permission : getRepositoryMemberPermissions(repository)) {
				if(permission.registrant.equals(user)) {
					repoAndOwner.put(repository.name, new ImmutablePair<String, Boolean>(repository.owners.get(0), permission.toString().contains("W")));
					break;
				}
			}			
		}
		
		return repoAndOwner;
	}
	
	/** Restituisce una lista di permessi (RegistrantAccessPermission) per un repository.
	 * L'oggetto RegistrantAccessPermission contiene un campo utente e un campo per identificare il permesso.
	 * 
	 * @param repository
	 * @return
	 * @throws InternalException
	 */
	private static List<RegistrantAccessPermission> getRepositoryMemberPermissions(RepositoryModel repository) throws InternalException {
		List<RegistrantAccessPermission> permissions = null;
		
		try {
			permissions = RpcUtils.getRepositoryMemberPermissions(repository, HostURL, AdminName, AdminPwd);
		} catch (IOException e) {
			throw new InternalException("gitblit api exception for getRepositoryMemberPermissions");
		}
		
		return permissions;
	}
	
	/** Restituisce i RepositoryModel associati ai repository su GitBlit.
	 * 
	 * @return
	 * @throws InternalException
	 */
	private static Collection<RepositoryModel> getRepositories() throws InternalException {
		Collection<RepositoryModel> models = null;
		try {
			models = RpcUtils.getRepositories(HostURL, AdminName, AdminPwd).values();
		} catch (IOException e) {
			throw new InternalException("gitblit api exception on getRepositories");
		}
		
		return models;
	}
	
	/** Restituisce il RepositoryModel di un repository che possiede un determinato nome. Non possono esistere repository con lo stesso nome.
	 * 
	 * @param repository
	 * @return
	 * @throws IOException
	 * @throws RepositoryDoesNotExistException
	 */
	private static RepositoryModel getRepositoryModel(String repository) throws InternalException, RepositoryDoesNotExistException {
		RepositoryModel model = null;
		
		try {
			model = RpcUtils.getRepositories(HostURL, AdminName, AdminPwd).get(toRepoUrl(repository));
		} catch (IOException e) {
			throw new InternalException("gitblit api exception for getRepositories");
		}
		
		if(model == null)
			throw new RepositoryDoesNotExistException(repository);
		
		return model;
	}
	
	/** Gitblit crea inizialmente dei repository vuoti, che creano delle difficoltà nell'effettuare il primo commit con JGit dopo 
	 * un clone (mancano i riferimenti al repository remoto).
	 * 
	 * La funzione crea un repository temporaneo e effettua un commit "vuoto" su un repository, in modo tale che il repository non sia più vuoto. 
	 * 
	 * @param name
	 * @param owner
	 * @throws InternalException 
	 */
	private static void initialCommit(String name, String owner) throws InternalException {
		try {
			File tmpDir = File.createTempFile("tmpGitDir", "");
			tmpDir.delete();
			
			Git git = Git.cloneRepository()
					.setCredentialsProvider(new UsernamePasswordCredentialsProvider(AdminName, AdminPwd))
					.setDirectory(tmpDir).setURI(toRepoUrl(name)).call();
			git.commit().setMessage("initialization").call();

			git.push().setCredentialsProvider(new UsernamePasswordCredentialsProvider(AdminName, AdminPwd)).call();
		} catch (Exception e) {
			throw new InternalException("JGit api exception for initialCommit");
		}		
	}
}
