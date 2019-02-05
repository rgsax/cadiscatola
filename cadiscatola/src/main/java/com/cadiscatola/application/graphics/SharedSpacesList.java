package com.cadiscatola.application.graphics;

import java.util.ArrayList;

import org.apache.commons.lang3.tuple.ImmutablePair;

import com.cadiscatola.api.model.SharedSpace;
import com.cadiscatola.api.model.User;
import com.cadiscatola.api.utils.CloudStorageUtils;
import com.cadiscatola.api.wrapper.exceptions.InternalException;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;

public class SharedSpacesList extends VBox {
	final User user;
	
	public final ListView<SharedSpace> ownerSpaces = new ListView<>();
	public final ListView<SharedSpace> readSpaces = new ListView<>();
	public final ListView<SharedSpace> writeSpaces = new ListView<>();
	
	public SharedSpacesList(User user) throws InternalException {
		super();
		this.user = user;
	
		initGUI();
		initEH();
	}
	
	private void initGUI() {
		this.setMinWidth(20);
		this.setSpacing(20);
		this.setPadding(new Insets(10));
		
		ownerSpaces.setMaxHeight(100);
		readSpaces.setMaxHeight(100);
		writeSpaces.setMaxHeight(100);
		
		ObservableList<SharedSpace> ownerList = FXCollections.observableArrayList();
		ObservableList<SharedSpace> readList = FXCollections.observableArrayList();
		ObservableList<SharedSpace> writeList = FXCollections.observableArrayList();
		
		try {
			for(ImmutablePair<SharedSpace, Boolean> sharedSpace : CloudStorageUtils.getAccessibleSharedSpaces(user)) {
							
				if(sharedSpace.left.getOwner().equals(user))
					ownerList.add(sharedSpace.left);
				else if(sharedSpace.right.booleanValue())//canWrite
					writeList.add(sharedSpace.left);
				else
					readList.add(sharedSpace.left);
			}
		} catch (InternalException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		ownerSpaces.setItems(ownerList);
		readSpaces.setItems(readList);
		writeSpaces.setItems(writeList);
		
		ObservableList<Node> children = this.getChildren();
		
		VBox pane1 = new VBox(new Label("owner"), ownerSpaces);
		children.add(pane1);
		
		VBox pane2 = new VBox(new Label("read"), readSpaces);
		children.add(pane2);
		
		VBox pane3 = new VBox(new Label("write"), writeSpaces);
		children.add(pane3);
		
		this.setStyle("-fx-border-color: black;");
	}
	
	public ArrayList<SharedSpace> getSharedSpaces() {
		ArrayList<SharedSpace> sharedSpaces = new ArrayList<>();
		
		for(Object o : ownerSpaces.getItems().toArray())
			sharedSpaces.add((SharedSpace)o);
		
		for(Object o : writeSpaces.getItems().toArray())
			sharedSpaces.add((SharedSpace)o);
		
		for(Object o : readSpaces.getItems().toArray())
			sharedSpaces.add((SharedSpace)o);
		
		return sharedSpaces;		
	}
	
	public void addOwnedSharedSpace(SharedSpace sharedSpace) {
		ownerSpaces.getItems().add(sharedSpace);
	}
	
	private void initEH() {
		
	}
}
