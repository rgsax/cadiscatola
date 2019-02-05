package com.cadiscatola.application.graphics;

import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;

public class EmptyPane extends BorderPane {
	public EmptyPane() {
		setCenter(new Label("Seleziona un repository per vederne i dettagli.")); 
	}
}
