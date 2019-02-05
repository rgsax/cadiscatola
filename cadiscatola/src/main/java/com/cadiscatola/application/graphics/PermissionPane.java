package com.cadiscatola.application.graphics;
import java.util.Map;
import java.util.Set;

import org.controlsfx.control.ToggleSwitch;

import com.cadiscatola.api.model.SharedSpace;
import com.cadiscatola.api.model.User;
import com.cadiscatola.api.utils.CloudStorageUtils;
import com.cadiscatola.api.utils.exceptions.SharedSpaceDoesNotExistException;
import com.cadiscatola.api.utils.exceptions.UserDoesNotExistException;
import com.cadiscatola.api.wrapper.exceptions.InternalException;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class PermissionPane extends HBox {
	private Map<String, Boolean> collaborators;
	
	private SharedSpace sharedSpace; 
	private ListView<String> usersWithAccessView = new ListView<>(); 
	
	private Button removeCollaborator = new Button("REMOVE COLLAB"); 
	
	private ToggleSwitch readWriteSwitch = new ToggleSwitch("Read");
	
	private Button addCollaborator = new Button("ADD COLLABORATOR");
	private TextField newCollabName = new TextField();
	
	private ScheduledService<Void> service = null;
	
	// PermissionPane compare solo quando ad operare è l'owner di Sp, quindi non è 
	// necessario dare come parametro un User che tenta di modificare i permessi.
	public PermissionPane(SharedSpace sharespace) {
		this.sharedSpace = sharespace; 
		try {
			collaborators = sharedSpace.getCollaborators();
		} catch (SharedSpaceDoesNotExistException | InternalException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		initGUI();
		initEH(); 
	}
	
	private void initGUI() {
		ObservableList<String> allowedUsersView = FXCollections.observableArrayList();
		Set<String> usernames = collaborators.keySet();
		for (String u: usernames) allowedUsersView.add(u);
		usersWithAccessView.setItems(allowedUsersView);
		this.getChildren().add(usersWithAccessView);
		
		this.setSpacing(15);
		
		readWriteSwitch.setDisable(true);
		removeCollaborator.setDisable(true);
		addCollaborator.setDisable(true);
		
		Pane pane1 = new VBox(new Label("Permissions"), readWriteSwitch);
		Pane pane2 = new HBox(5, addCollaborator, newCollabName);
		
		this.getChildren().addAll(new VBox(10, pane1, removeCollaborator, pane2));
		
	}
	
	private void initEH() {
		readWriteSwitch.selectedProperty().addListener((observable, oldValue, newValue) -> {
			User u = new User(usersWithAccessView.getSelectionModel().getSelectedItem(), null);
			if(newValue) {
				readWriteSwitch.setText("Write");
				try {
					CloudStorageUtils.setReadWriteUser(u, sharedSpace);
				} catch (UserDoesNotExistException | SharedSpaceDoesNotExistException | InternalException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			else {
				readWriteSwitch.setText("Read");
				try {
					CloudStorageUtils.setReadOnlyUser(u, sharedSpace);
				} catch (UserDoesNotExistException | SharedSpaceDoesNotExistException | InternalException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		
		addCollaborator.setOnMouseClicked(event -> {
			if(!collaborators.keySet().contains(newCollabName.getText())) {
				User u = new User(newCollabName.getText(), null);
				
				Service<Void> service = new Service<Void>() {
					@Override
					protected Task<Void> createTask() {
						return new Task<Void>() {
							@Override
							protected Void call() throws Exception {
								
								try {
									CloudStorageUtils.setReadOnlyUser(u, sharedSpace);
									collaborators.put(newCollabName.getText(), false);
									
									Platform.runLater(() -> {
										usersWithAccessView.getItems().add(u.getName());
									});
									
								} catch (UserDoesNotExistException e) {
									System.out.println("problemi");
									Platform.runLater(() -> {
										Alert alert = new Alert(AlertType.WARNING);
										alert.setHeaderText("Utente inesistente");
										alert.setContentText("L'utente " + newCollabName.getText() + " non eiste!");
										alert.showAndWait();
									});
								} catch (SharedSpaceDoesNotExistException e) {
									
								} catch (InternalException e) {
									e.printStackTrace();
								} finally {
									Platform.runLater(() -> newCollabName.setText(""));;
								}
								
								return null;
							}
						};
			
					}
				};
				
				service.start();
			}
		});
		
		newCollabName.textProperty().addListener(new ChangeListener<Object>() {
			@Override
			public void changed(ObservableValue<?> observable, Object oldValue, Object newValue) {
				addCollaborator.setDisable(newValue.equals(""));
			}
			
		});
		
		removeCollaborator.setOnMouseClicked(event -> {
			String collabName = usersWithAccessView.getSelectionModel().getSelectedItem();
			User collab = new User(collabName, null);
			
			Service<Void> service = new Service<Void>() {

				@Override
				protected Task<Void> createTask() {
					return new Task<Void>() {
						@Override
						protected Void call() throws Exception {
							try {								
								System.out.println(CloudStorageUtils.removeCollaborator(collab, sharedSpace));
								
								Platform.runLater(() -> {
									usersWithAccessView.getItems().remove(collabName);
								});
								
								//readWriteSwitch.setDisable(true);
							} catch (UserDoesNotExistException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							} catch (SharedSpaceDoesNotExistException e2) {
								// TODO Auto-generated catch block
								e2.printStackTrace();
							} catch (InternalException e3) {
								// TODO Auto-generated catch block
								e3.printStackTrace();
							}
							return null;
						}
					};
				}
			};
			
			service.start();
		});
		
		
		usersWithAccessView.getSelectionModel().selectedItemProperty().addListener( (observable, oldValue, newValue) -> {
			// Voglio poter switchare i permessi dell'utente selezionato
				if(newValue != null) {
					readWriteSwitch.setDisable(false);
					removeCollaborator.setDisable(false);
					readWriteSwitch.setText(collaborators.get(newValue) ? "Write" : "Read");
					readWriteSwitch.setSelected(collaborators.get(newValue));
				}
				else {
					readWriteSwitch.setDisable(true);
					removeCollaborator.setDisable(true);
				}
		});
		
		
		service = new ScheduledService<Void>() {
			
			@Override
			protected Task<Void> createTask() {
				return new Task<Void>() {
					
					@Override
					protected Void call() throws Exception {
						collaborators = CloudStorageUtils.getSharedSpaceCollaborators(sharedSpace);
						
						Platform.runLater(() -> {
							ObservableList<String> users = FXCollections.observableArrayList();
							collaborators.keySet().forEach(u -> users.add(u));
							usersWithAccessView.setItems(users);
						});
						
						return null;
					}
				};
			}
		};
		
		service.setPeriod(Duration.seconds(5));
		service.start();
	}
	
	public void stopUserSynchronization() {
		service.cancel();
	}
}