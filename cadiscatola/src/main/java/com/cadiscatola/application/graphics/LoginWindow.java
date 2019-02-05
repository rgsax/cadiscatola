package com.cadiscatola.application.graphics;

import com.cadiscatola.api.model.User;
import com.cadiscatola.api.utils.CloudStorageUtils;
import com.cadiscatola.api.utils.exceptions.PasswordMismatchException;
import com.cadiscatola.api.utils.exceptions.UserAlreadyExistsException;
import com.cadiscatola.api.utils.exceptions.UserDoesNotExistException;
import com.cadiscatola.api.wrapper.exceptions.InternalException;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;

public class LoginWindow extends BorderPane {
	TextField userField = new TextField();
	PasswordField passwordField = new PasswordField();
	TextField ipField = new TextField("localhost");
	Button login = new Button("Login");
	Button register = new Button("Register");
	Label errorLabel = new Label("");
	
	public LoginWindow() {
		initGUI();
		initEH();
	}
	
	private void initGUI() {
		this.setMinSize(600, 400);
		this.setPadding(new Insets(30));
		GridPane pane = new GridPane();
		pane.setHgap(10);
		pane.setVgap(30);
		pane.setAlignment(Pos.CENTER);
		
		pane.add(new Label("username"), 0, 0);
		pane.add(userField, 1, 0);
		pane.add(new Label("password"), 0, 1);
		pane.add(passwordField, 1, 1);	
		pane.add(new Label("Server ip Address"), 0, 2);
		pane.add(ipField, 1, 2);
		pane.add(login, 0, 3);
		pane.add(register, 1, 3);
		this.setCenter(pane);
		
		errorLabel.setPadding(new Insets(30, 0, 0, 0));
		this.setBottom(errorLabel);
		
		BorderPane.setAlignment(pane, Pos.CENTER);
		BorderPane.setAlignment(errorLabel, Pos.CENTER);
	}
	
	private void initEH() {
		this.setOnKeyPressed(event -> {
			if(event.getCode().equals(KeyCode.ENTER))
				login();
		});
		
		login.setOnMouseClicked(event -> {
			login();
		});
		
		register.setOnMouseClicked(event -> {
			register();
		});
	}
	
	private void register() {
		User user = null;
		
		errorLabel.setText("");
		
		try {
			CloudStorageUtils.setCloudServerIp(ipField.getText());
			user = CloudStorageUtils.createUser(userField.getText(), passwordField.getText());
			Main.switchToScene(new InitialWindow(user));
		} catch (UserAlreadyExistsException e) {
			errorLabel.setText("L'utente " + userField.getText() + " esiste già!");
		} catch (InternalException e) {
			errorLabel.setText("Problema interno, riprovare più tardi");
		}
	}

	private void login() {
		User user = null;
		
		errorLabel.setText("");
		
		try {
			CloudStorageUtils.setCloudServerIp(ipField.getText());
			user = CloudStorageUtils.getUser(userField.getText(), passwordField.getText());
			Main.switchToScene(new InitialWindow(user));
		} catch (UserDoesNotExistException e1) {
			errorLabel.setText("L'utente " + userField.getText() +  " non esiste!");
		} catch (PasswordMismatchException e1) {
			errorLabel.setText("La password per l'utente " + userField.getText() +  " non è corretta!");
		} catch (InternalException e1) {
			errorLabel.setText("Problema interno, riprovare più tardi");
		}
	}
}
