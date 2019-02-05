package com.cadiscatola.application.graphics;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.controlsfx.control.ToggleSwitch;

import com.cadiscatola.api.model.SharedSpace;
import com.cadiscatola.api.model.User;
import com.cadiscatola.application.file_watcher.FileWatcher;
import com.cadiscatola.application.file_watcher.FileWatcherDispatcher;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

public class InfoPane extends VBox{
	private static String entryPath = "./";
	
	public SharedSpace sharedSpace;
	private User user;
	
	private ToggleSwitch syncSwitch = new ToggleSwitch("sync");
	private FileWatcher fileWatcher = null;
	
	private Button addFileButton = new Button("Add file");
	private Button addFolderButton = new Button("Add folder");
	
	public Button exitFromSharedSpaceButton = new Button("Exit from shared space");
	public Button deleteSharedSpaceButton = new Button("Delete shared space");
	
	private PermissionPane permissionPane = null;
	
	private File sharedSpaceFolder;
	
	public InfoPane(SharedSpace sharedSpace, User user) {
		this.sharedSpace = sharedSpace;
		this.user = user;
		
		sharedSpaceFolder = new File(entryPath 
				+ sharedSpace.getOwner().getName() 
				+ "/" + sharedSpace.getName());
		
		initGUI();
		initEH();
	}
	
	public static void setEntryPath(String path) {
		entryPath = path;
	}
	
	private void initGUI() {
		this.setAlignment(Pos.TOP_CENTER);
		this.setPadding(new Insets(10));
		this.setSpacing(10);
		
		Label ownerLabel = new Label("owner: " + sharedSpace.getOwner().getName());
		Label sharedSpaceLabel = new Label("shared space: " + sharedSpace.getName());
		
		addFileButton.setDisable(true);
		addFolderButton.setDisable(true);
		
		Pane parentPane = new HBox(30);
		Pane pane = new VBox(5, ownerLabel, sharedSpaceLabel, syncSwitch, addFileButton, addFolderButton);	
		parentPane.setPadding(new Insets(10));
		parentPane.setStyle("-fx-border-color: black");
		parentPane.getChildren().add(pane);
		this.getChildren().add(parentPane);
		
		
		if (sharedSpace.getOwner().equals(user)){
			parentPane.getChildren().add(deleteSharedSpaceButton);
			permissionPane = new PermissionPane(sharedSpace);
			Pane pane2 = new VBox(new Label("Collaborators"), permissionPane);
			this.getChildren().add(pane2);
		}
		else {
			parentPane.getChildren().add(exitFromSharedSpaceButton);
		}
	}
	
	private void initEH() {		
		syncSwitch.selectedProperty().addListener(event -> {
			if(syncSwitch.isSelected()) {
				try {
					initializeFileWatcher();
					fileWatcher.start();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else
				stopSynchronization();
			
			boolean selected = syncSwitch.isSelected();
			addFileButton.setDisable(!selected);
			addFolderButton.setDisable(!selected);
			deleteSharedSpaceButton.setDisable(selected);
			exitFromSharedSpaceButton.setDisable(selected);
		});
		
		addFileButton.setOnMouseClicked(event -> {
			List<File> files = new FileChooser().showOpenMultipleDialog(Main.getPrimaryStage());
			if(files != null)
				for(File file : files) {
					try {
						FileUtils.copyFileToDirectory(file, new File(entryPath 
								+ sharedSpace.getOwner().getName() 
								+ "/" + sharedSpace.getName()));
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
		});
		
		addFolderButton.setOnMouseClicked(event -> {
			File directory = new DirectoryChooser().showDialog(Main.getPrimaryStage());
			if(directory != null) {
				System.out.println(directory.getName());
				try {
					FileUtils.copyDirectoryToDirectory(directory, sharedSpaceFolder);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		
	}
	
	private void initializeFileWatcher() {
		System.out.println(sharedSpaceFolder.getAbsolutePath());
		fileWatcher = FileWatcherDispatcher.getInstance().createFileWatcher(sharedSpaceFolder, sharedSpace, user);
	}
	
	public PermissionPane getPermissionPane() {
		return permissionPane;
	}
	
	public SharedSpace getSharedSpace() {
		return sharedSpace;
	}
	
	public void stopSynchronization() {
		if(fileWatcher != null)
			try {
				fileWatcher.stopSynchronization();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
	
}
