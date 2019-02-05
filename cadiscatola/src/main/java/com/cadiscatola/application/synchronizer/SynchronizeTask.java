package com.cadiscatola.application.synchronizer;

import java.io.File;

import com.cadiscatola.api.model.User;
import com.cadiscatola.api.utils.LocalStorageUtils;
import com.cadiscatola.api.utils.exceptions.SharedSpaceDoesNotExistException;
import com.cadiscatola.api.wrapper.exceptions.InternalException;

import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;

public class SynchronizeTask extends ScheduledService<Void>{
	
	File sharedSpacePath;
	User user;
	
	public SynchronizeTask(File sharedSpacePath, User user) {
		this.sharedSpacePath = sharedSpacePath;
		this.user = user;
	}

	@Override
	protected Task<Void> createTask() {
		return new Task<Void>() {
			@Override
			protected Void call() throws Exception {
				
				try {
					LocalStorageUtils.synchronizeSharedSpace(sharedSpacePath.getAbsolutePath(), user);
					LocalStorageUtils.updateSharedSpace(sharedSpacePath.getAbsolutePath(), user);
				} catch (SharedSpaceDoesNotExistException e) {
				
				}
				catch (InternalException e) {
					
				}
				
				return null;
			}
		};
	}

}
