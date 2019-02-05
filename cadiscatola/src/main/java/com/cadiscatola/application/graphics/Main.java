package com.cadiscatola.application.graphics;
	
import com.cadiscatola.api.model.User;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;


public class Main extends Application {
	User user = new User("utente1", "password1");
	private static Stage stage = null;
	
	@Override
	public void start(Stage primaryStage) {
		try {
			//CloudStorageUtils.setCloudServerURL("http://localhost:8080/");
			stage = primaryStage;
			
			stage.setTitle("Cadiscatola");
			stage.getIcons().add(new Image("icon.png"));
			
			Platform.setImplicitExit(false);
			Pane root = new LoginWindow();
			Scene scene = new Scene(root);
			primaryStage.setScene(scene);	
			
			primaryStage.setOnCloseRequest(event -> {
				event.consume();
				primaryStage.hide();
			});
			
			primaryStage.show();
			new AppTray(primaryStage);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void switchToScene(Parent root) {
		stage.setScene(new Scene(root));
	}
	
	public static Stage getPrimaryStage() {
		return stage;
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}
