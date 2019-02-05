package com.cadiscatola.api.utils;

import java.io.File;

import com.cadiscatola.api.model.SharedSpace;
import com.cadiscatola.api.model.User;
import com.cadiscatola.api.utils.exceptions.SharedSpaceDoesNotExistException;
import com.cadiscatola.api.wrapper.ClientUtils;
import com.cadiscatola.api.wrapper.exceptions.InternalException;
import com.cadiscatola.api.wrapper.exceptions.RepositoryDoesNotExistException;

public class LocalStorageUtils {
	private LocalStorageUtils() { }
	
	/** Crea una copia locale di uno Sharespace 
	 * 
	 * @param sharedSpace lo shared space da scaricare
	 * @param user	l'utente che intende scaricare lo shared space
	 * @param localPath il path di salvataggio della copia locale dello shared space
	 * @throws SharedSpaceDoesNotExistException 
	 * @throws InternalException 
	 */
	public static void downloadSharedSpace(SharedSpace sharedSpace, User user, String localPath) throws SharedSpaceDoesNotExistException, InternalException {
		try {
			ClientUtils.cloneRepository(Utils.getSharedSpaceName(sharedSpace), localPath, user.getName(), user.getPassword());
		} catch (RepositoryDoesNotExistException e) {
			throw new SharedSpaceDoesNotExistException(sharedSpace.getName());
		}
	}
	
	/**
	 * @throws SharedSpaceDoesNotExistException  Sincronizza la copia locale di uno Sharespace con la copia remota.
	 * 
	 * @param localPath Il path dello shared space locale
	 * @param user l'utente che chiama la sincronizzazione
	 * @throws InternalException 
	 * @throws  
	 */
	public static void synchronizeSharedSpace(String localPath, User user) throws InternalException, SharedSpaceDoesNotExistException {
		try {
			ClientUtils.pull(localPath, user.getName(), user.getPassword());
		} catch (RepositoryDoesNotExistException e) {
			throw new SharedSpaceDoesNotExistException(localPath);
		}
	}
	
	public static void registerocalUpdate(File file, User user) throws InternalException {
		ClientUtils.commitAll(file.getAbsolutePath(), user.getName(), user.getPassword());
	}
	
	/** Aggiorna la copia remota dello Sharespace con i cambiamenti della copia locale.
	 * 
	 * @param localPath il path dello shared space locale
	 * @param user l'utente che effettua l'update
	 * @throws SharedSpaceDoesNotExistException
	 * @throws InternalException 
	 */
	public static boolean updateSharedSpace(String localPath, User user) throws InternalException, SharedSpaceDoesNotExistException {
		boolean status = false;
		try {
			status = ClientUtils.commitAndPush(localPath, user.getName(), user.getPassword(), true);
		} catch (RepositoryDoesNotExistException e) {
			throw new SharedSpaceDoesNotExistException(localPath);
		}
		
		return status;
	}
}
