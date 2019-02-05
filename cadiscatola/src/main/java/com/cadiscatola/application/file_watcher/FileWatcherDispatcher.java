package com.cadiscatola.application.file_watcher;

import java.io.File;

import com.cadiscatola.api.model.SharedSpace;
import com.cadiscatola.api.model.User;
import com.cadiscatola.api.utils.LocalStorageUtils;
import com.cadiscatola.api.utils.exceptions.SharedSpaceDoesNotExistException;
import com.cadiscatola.api.wrapper.exceptions.InternalException;

public class FileWatcherDispatcher {
	private static FileWatcherDispatcher instance = null;
	
	private FileWatcherDispatcher() { }
	
	public static FileWatcherDispatcher getInstance() {
		if(instance == null)
			instance = new FileWatcherDispatcher();
		
		return instance;
	}
	
	public FileWatcher createFileWatcher(File file, SharedSpace sharedSpace, User user) {
		System.out.println(file.getName());
		if(!file.exists()) {
			try {
				String filename = file.getAbsolutePath();
				LocalStorageUtils.downloadSharedSpace(sharedSpace, user, filename);
				file = new File(filename);
			} catch (SharedSpaceDoesNotExistException e) {
				System.out.println("spazio condiviso non esistente");
				e.printStackTrace();
			} catch(InternalException e) {
				System.out.println("Accesso negato");
				e.printStackTrace();
			}
		}
		
		FileWatcher fileWatcher = new FileWatcher(new SharedSpaceObserver(file, user));
		
		return fileWatcher;
	}
}
