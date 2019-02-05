package com.cadiscatola.application.file_watcher;

import java.io.File;

import org.apache.commons.io.monitor.FileAlterationMonitor;

import com.cadiscatola.api.model.User;
import com.cadiscatola.application.synchronizer.SynchronizeTask;

import javafx.util.Duration;

public class FileWatcher {
	private FileAlterationMonitor monitor;
	private SynchronizeTask synchronizer;
	
	public FileWatcher(SharedSpaceObserver observer) {
		initialiseMonitor(observer);
		initializeSynchronizer(observer.getDirectory(), observer.getUser());
	}
	
	public void start() {
		
		synchronizer.setPeriod(Duration.seconds(5));
		synchronizer.start();
		
		try {
			monitor.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void stopSynchronization() throws Exception {
		synchronizer.cancel();
		monitor.stop();
	}
	
	private void initialiseMonitor(SharedSpaceObserver observer) {
		monitor = new FileAlterationMonitor(1000);
		monitor.addObserver(observer);
	}
	
	private void initializeSynchronizer(File sharedSpacePath, User user) {
		synchronizer = new SynchronizeTask(sharedSpacePath, user);
	}
}
