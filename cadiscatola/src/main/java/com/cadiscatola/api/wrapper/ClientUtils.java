package com.cadiscatola.api.wrapper;

import java.io.File;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.ResetCommand.ResetType;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.errors.RepositoryNotFoundException;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.RemoteRefUpdate;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import com.cadiscatola.api.wrapper.exceptions.InternalException;
import com.cadiscatola.api.wrapper.exceptions.RepositoryDoesNotExistException;

public class ClientUtils {
	private ClientUtils() { }
	
	/** Clona un repository.
	 * 
	 * @param repositoryName Il nome del repository da clonare
	 * @param path La file path del sistema dove inizializzare il repository Git
	 * @param username	Username dell'utente che sta cercando di clonare il repository
	 * @param password	Password dell'utente che sta cercando di clonare il repository 
	 * @throws RepositoryDoesNotExistException 
	 * @throws InternalException 
	 * 
	 */
	public static void cloneRepository(String repositoryName, String path, String username, String password) throws RepositoryDoesNotExistException, InternalException {
		String URI = ServerUtils.toRepoUrl(repositoryName);
		try {
			Git.cloneRepository()
				.setCredentialsProvider(new UsernamePasswordCredentialsProvider(username, password))
				.setURI(URI)
				.setDirectory(new File(path))
				.call();	
		} catch(InvalidRemoteException e) {
			throw new RepositoryDoesNotExistException(repositoryName);
		} catch(Exception e) {
			throw new InternalException("JGit api exception for clone on " + URI);
		}
	}
	
	/**
	 * Esegue un pull da un repository. In caso di merge-conflicts, allinea il repository locale al repository remoto attraverso un `git reset`.
	 * 
	 * @param path File path del repository locale su cui effettuare il pull.
	 * @param username Username dell'utente che intende effettuare il pull.
	 * @param password	Password dell'utente che intende effettuare il pull.
	 * @throws RepositoryDoesNotExistException 
	 * @throws InternalException 
	 */
	public static void pull(String path, String username, String password) throws RepositoryDoesNotExistException, InternalException {
		try {	
			Git git = Git.open(new File(path));
			PullResult pr = git.pull()
					.setCredentialsProvider(new UsernamePasswordCredentialsProvider(username, password))
					.call();
			if(!pr.getMergeResult().getMergeStatus().isSuccessful()) {
				git.reset().setMode(ResetType.HARD).setRef("origin/master").call();
			}
		} catch(RepositoryNotFoundException e) {
			throw new RepositoryDoesNotExistException(path);
			/*
			 * RepositoryNotFoundException è un'eccezione di jgit
			 */
		} catch(Exception e) {
			throw new InternalException("JGit api exception on pull for " + path);
		}
	}
	
	/**
	 * Esegue un commit di tutti i file in path ed esegue un push.
	 * 
	 * @param path File path del repository dal quale si intende effettuare un push.
	 * @param username	Username dell'utente che intende effettuare un push.
	 * @param password	Password dell'utente che intende effettuare un push.
	 * @param forcePush	Se True, verrà eseguito il comando `git push -force`, altrimenti `git push`
	 * @return True se l'operazione è andata a buon fine, False altrimenti.
	 * @throws InternalException 
	 * @throws RepositoryDoesNotExistException 
	 * 
	 */
	public static boolean commitAndPush(String path, String username, String password, boolean forcePush) throws InternalException, RepositoryDoesNotExistException {
		Git git = null;
		try {
			git = Git.open(new File(path));
		} catch(Exception e) {
			throw new InternalException("JGit api exception on commitAndPush for " + path);
		}
		commitAll(git);
		return push(git, username, password, forcePush);
	}
	
	/** Esegue un commit di tutti i file.
	 *  
	 * @param git Rappresenta lo stato corrente di git.
	 * @throws InternalException 
	 */
	public static void commitAll(Git git) throws InternalException{
		try {
			git.add().addFilepattern(".").call();
			git.commit().setAll(true).setMessage("").call();
		} catch(Exception e) {
			throw new InternalException("JGit api exception on commit");
		}
	}
	
	/** Esegue un commit di tutti i file.
	 *  
	 * @throws InternalException 
	 */
	public static void commitAll(String path, String username, String password) throws InternalException{
		try {
			Git git = Git.open(new File(path));
			git.add().addFilepattern(".").call();
			git.commit().setAll(true).setMessage("").call();
		} catch(Exception e) {
			throw new InternalException("JGit api exception on commit");
		}
	}
	
	/** Esegue un push dal repository corrente.
	 * 
	 * @param git Rappresenta lo stato corrente di git.
	 * @param username	Username dell'utente che intende effettuare un push
	 * @param password	Password dell'utente che intende effettuare un push
	 * @param forcePush	Se True, verrà eseguito il comando `git push -force`, altrimenti `git push`
	 * @return True se l'operazione è andata a buon fine, False altrimenti.
	 * @throws RepositoryDoesNotExistException 
	 * @throws InternalException 
	 */
	private static boolean push(Git git, String username, String password, boolean forcePush) throws RepositoryDoesNotExistException, InternalException  {
		
			Iterable<PushResult> pr;
			try {
				pr = git.push()
				.setForce(forcePush)
				.setCredentialsProvider(new UsernamePasswordCredentialsProvider(username, password))
				.call();
				
				for(PushResult p : pr) {
					for(RemoteRefUpdate ru : p.getRemoteUpdates())
						if(ru.getStatus().equals(RemoteRefUpdate.Status.REJECTED_OTHER_REASON))
							return false;
							/*
							 * Il push potrebbe fallire perchè l'utente 
							 * non ha privilegi di scrittura sul repository
							 */
				}
			} catch (TransportException | InvalidRemoteException e) {
				throw new RepositoryDoesNotExistException(git.getRepository().getDirectory().getName());
			} catch(Exception e) {
				throw new InternalException("JGit api exception on push for " + git.getRepository().getDirectory().getName());
			}
		
		return true;
	}
}
