package com.cadiscatola.application.graphics;

import java.io.File;
import java.util.HashMap;

import com.cadiscatola.api.model.SharedSpace;
import com.cadiscatola.api.model.User;
import com.cadiscatola.api.utils.CloudStorageUtils;
import com.cadiscatola.api.utils.exceptions.SharedSpaceAlreadyExistsException;
import com.cadiscatola.api.utils.exceptions.SharedSpaceDoesNotExistException;
import com.cadiscatola.api.utils.exceptions.UserDoesNotExistException;
import com.cadiscatola.api.wrapper.exceptions.InternalException;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.stage.DirectoryChooser;
import javafx.util.Duration;

public class InitialWindow extends BorderPane {
	private final User user;
	private SharedSpacesList sharedSpacesList;
	private HashMap<SharedSpace, InfoPane> infoPanes = new HashMap<>();
	
	private Button logout = new Button("Logout");
	private Button createSharedSpaceButton = new Button("Create shared space");
	private RefreshingTask refreshingTask;
	
	public InitialWindow(User user) throws InternalException {
		super();		
		this.user = user;
		
		initGUI();
		
		refreshingTask = new RefreshingTask(sharedSpacesList);
		refreshingTask.setPeriod(Duration.seconds(5));
		refreshingTask.start();
		
		initEH();
	}
	
	private void initGUI() throws InternalException {
		this.setMinSize(800, 600);
		this.setPadding(new Insets(10));
		sharedSpacesList = new SharedSpacesList(user);
		this.setLeft(sharedSpacesList);
		
		Label userLabel = new Label(user.getName());
		BorderPane box = new BorderPane();
		box.setRight(logout);
		box.setCenter(userLabel);
		box.setLeft(createSharedSpaceButton);
		box.setPadding(new Insets(10));
		
		this.setTop(box);
		this.setCenter(new EmptyPane());
		
		BorderPane.setAlignment(box, Pos.TOP_CENTER);
		
		for(SharedSpace sp : sharedSpacesList.getSharedSpaces()) {
			infoPanes.put(sp, new InfoPane(sp, user));
		}
	}
	
	private void initEH() {
		logout.setOnMouseClicked(new EventHandler<Event>() {
			@Override
			public void handle(Event event) {
				refreshingTask.stopRefreshing();
				Main.switchToScene(new LoginWindow());
			}
		});
		
		sharedSpacesList.ownerSpaces.getSelectionModel().selectedItemProperty()
			.addListener(new ChangeListener<SharedSpace>() {

				@SuppressWarnings("unlikely-arg-type")
				@Override
				public void changed(ObservableValue<? extends SharedSpace> observable, SharedSpace oldValue,
						SharedSpace newValue) {
					if(newValue != null) {
						sharedSpacesList.readSpaces.getSelectionModel().clearSelection();
						sharedSpacesList.writeSpaces.getSelectionModel().clearSelection();
						addInfoPane(newValue);
					}
					else {
						infoPanes.remove(oldValue);
						InitialWindow.this.getChildren().remove(oldValue);
					}
				}
		
		});
		
		sharedSpacesList.readSpaces.getSelectionModel().selectedItemProperty()
			.addListener(new ChangeListener<SharedSpace>() {

			@SuppressWarnings("unlikely-arg-type")
			@Override
			public void changed(ObservableValue<? extends SharedSpace> observable, SharedSpace oldValue,
					SharedSpace newValue) {
				if(newValue != null) {
					sharedSpacesList.ownerSpaces.getSelectionModel().clearSelection();
					sharedSpacesList.writeSpaces.getSelectionModel().clearSelection();
					addInfoPane(newValue);
				}
				else {
					infoPanes.remove(oldValue);
					InitialWindow.this.getChildren().remove(oldValue);
				}
			}
	
		});
		
		sharedSpacesList.writeSpaces.getSelectionModel().selectedItemProperty()
			.addListener(new ChangeListener<SharedSpace>() {
				
			@SuppressWarnings("unlikely-arg-type")
			@Override
			public void changed(ObservableValue<? extends SharedSpace> observable, SharedSpace oldValue,
					SharedSpace newValue) {
				if(newValue != null) {
					sharedSpacesList.ownerSpaces.getSelectionModel().clearSelection();
					sharedSpacesList.readSpaces.getSelectionModel().clearSelection();
					addInfoPane(newValue);
				}
				else {
					infoPanes.remove(oldValue);
					InitialWindow.this.getChildren().remove(oldValue);
				}
			}
	
		});
		
		createSharedSpaceButton.setOnMouseClicked(event -> {
			File dir = new DirectoryChooser().showDialog(Main.getPrimaryStage());
			if(dir != null) {				
					try {
						SharedSpace space = CloudStorageUtils.createSharedSpace(dir.getName(), user);
						sharedSpacesList.addOwnedSharedSpace(space);
						infoPanes.put(space, new InfoPane(space, user));
						Alert alert = new Alert(AlertType.INFORMATION);
						alert.setHeaderText("Shared space creato");
						alert.setContentText("Lo shared space " + space.getName() + " è stato creato con successo");
						alert.showAndWait();
					} catch (SharedSpaceAlreadyExistsException e) {
						Alert alert = new Alert(AlertType.ERROR);
						alert.setHeaderText("Shared space non creato");
						alert.setContentText("Lo shared space esiste già");
						alert.showAndWait();
					} catch (InternalException e) {
						Alert alert = new Alert(AlertType.ERROR);
						alert.setHeaderText("Errore interno");
						alert.setContentText("Errore interno, riprovare più tardi");
						alert.showAndWait();
					}

			}
		});
		
		for(SharedSpace space : infoPanes.keySet()) {
			InfoPane pane = infoPanes.get(space);
			
			initializeInfoPane(pane, space);
		}		
	}
	
	private void initializeInfoPane(InfoPane pane, SharedSpace space) {
		pane.deleteSharedSpaceButton.setOnMouseClicked(event -> {
			try {
				CloudStorageUtils.deleteSharedSpace(space);
				sharedSpacesList.ownerSpaces.getItems().remove(space);
				infoPanes.remove(space);
				this.getChildren().remove(pane);
			} catch (SharedSpaceDoesNotExistException | InternalException e) {
				e.printStackTrace();
			}
		});
		
		pane.exitFromSharedSpaceButton.setOnMouseClicked(event -> {
			try {
				CloudStorageUtils.removeCollaborator(user, space);
				if(!sharedSpacesList.readSpaces.getItems().remove(space)) {
					sharedSpacesList.writeSpaces.getItems().remove(space);
				}
				
				PermissionPane p = pane.getPermissionPane();
				if(p != null) {
					p.stopUserSynchronization();
				}
				
				infoPanes.remove(space);
				this.getChildren().remove(pane);
			} catch (UserDoesNotExistException | SharedSpaceDoesNotExistException | InternalException e) {
				e.printStackTrace();
			}
			
		});
	}
	
	private void addInfoPane(SharedSpace sharedSpace) {
		InfoPane infoPane = infoPanes.get(sharedSpace);
		if(infoPane == null) {
			infoPane = new InfoPane(sharedSpace, user);
			infoPanes.put(sharedSpace, infoPane);
		}
		this.setCenter(infoPane);
		BorderPane.setAlignment(infoPane, Pos.CENTER);
		initializeInfoPane(infoPane, sharedSpace);
	}
}
