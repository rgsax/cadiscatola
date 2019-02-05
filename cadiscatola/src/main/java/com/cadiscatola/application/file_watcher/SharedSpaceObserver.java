package com.cadiscatola.application.file_watcher;

import java.io.File;

import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.monitor.FileAlterationListener;
import org.apache.commons.io.monitor.FileAlterationObserver;

import com.cadiscatola.api.model.User;
import com.cadiscatola.api.utils.LocalStorageUtils;
import com.cadiscatola.api.wrapper.exceptions.InternalException;

public class SharedSpaceObserver extends FileAlterationObserver {
	private static final long serialVersionUID = 1L;
	
	private User user;

	public SharedSpaceObserver(File monitoredFolder, User user) {
		super(monitoredFolder, FileFilterUtils.notFileFilter(new PathFilter()));
		this.user = user;
		
		addListener();
	}
	
	public User getUser() {
		return user;
	}

	private void registerLocalUpdate() {	
		try {
			LocalStorageUtils.registerocalUpdate(super.getDirectory(), user);
		} catch (InternalException e) {
			e.printStackTrace();
		}		
	}
	
	private void addListener() {
		addListener(new FileAlterationListener() {
			
			@Override
			public void onStop(FileAlterationObserver file) {
				//System.out.println("stopping service");
			}
			
			@Override
			public void onStart(FileAlterationObserver file) {
				//System.out.println("starting service");				
			}
			
			@Override
			public void onFileDelete(File file) {
				System.out.println("deleted file " + file.getName());
				registerLocalUpdate();
			}
			
			@Override
			public void onFileCreate(File file) {
				System.out.println("created file " + file.getName());
				registerLocalUpdate();
			}
			
			@Override
			public void onFileChange(File file) {
				System.out.println("updated file " + file.getName());
				registerLocalUpdate();	
			}
			
			@Override
			public void onDirectoryDelete(File file) {
				System.out.println("deleted directory " + file.getName());
				registerLocalUpdate();
			}
			
			@Override
			public void onDirectoryCreate(File file) {
				System.out.println("created directory " + file.getName());
				registerLocalUpdate();
			}
			
			@Override
			public void onDirectoryChange(File file) {
				System.out.println("updated directory " + file.getName());
				registerLocalUpdate();
			}
		});		
	}

}
