package com.cadiscatola.application.graphics;

import java.util.ArrayList;

import org.apache.commons.lang3.tuple.ImmutablePair;

import com.cadiscatola.api.model.SharedSpace;
import com.cadiscatola.api.utils.CloudStorageUtils;
import com.cadiscatola.api.wrapper.exceptions.InternalException;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.scene.Node;
import javafx.scene.layout.Pane;

public class RefreshingTask extends ScheduledService<Void> {
	SharedSpacesList sharedSpacesList;
	
	public RefreshingTask(SharedSpacesList sharedSpacesList) {
		this.sharedSpacesList = sharedSpacesList;
	}
	
	public void stopRefreshing() {
		this.cancel();
	}

	@Override
	protected Task<Void> createTask() {
		return new Task<Void>() {
			
			@Override
			protected Void call() throws Exception {
				
				try {
					System.out.println("refresho");
					ArrayList<ImmutablePair<SharedSpace, Boolean>> newSharedSpaces = CloudStorageUtils.getAccessibleSharedSpaces(sharedSpacesList.user);
					Platform.runLater(() -> {
						try {
							refreshSharedSpacesLists(newSharedSpaces);
						} catch (InternalException e) {
							e.printStackTrace();
						}
					});
				} catch (InternalException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				return null;
			}
		};
	}
	
	public void refreshSharedSpacesLists(ArrayList<ImmutablePair<SharedSpace, Boolean>> newSharedSapces) throws InternalException {
		ObservableList<SharedSpace> ownerList = FXCollections.observableArrayList();
		ObservableList<SharedSpace> readList = FXCollections.observableArrayList();
		ObservableList<SharedSpace> writeList = FXCollections.observableArrayList();
		
		for(ImmutablePair<SharedSpace, Boolean> sharedSpace : newSharedSapces) {
						
			if(sharedSpace.left.getOwner().equals(sharedSpacesList.user))
				ownerList.add(sharedSpace.left);
			else if(sharedSpace.right.booleanValue())//canWrite
				writeList.add(sharedSpace.left);
			else
				readList.add(sharedSpace.left);
		}
		
		findAndRemoveSharedSpaces(sharedSpacesList.ownerSpaces.getItems(), ownerList);
		findAndRemoveSharedSpaces(sharedSpacesList.readSpaces.getItems(), readList);
		findAndRemoveSharedSpaces(sharedSpacesList.writeSpaces.getItems(), writeList);
	}
	
	private void findAndRemoveSharedSpaces(ObservableList<SharedSpace> current, ObservableList<SharedSpace> newSpaces) {
		ArrayList<SharedSpace> toRemove = new ArrayList<>();
		
		for(SharedSpace space : current) {
			if(newSpaces.contains(space))
				newSpaces.remove(space);
			else
				toRemove.add(space);
		}
		
		for(SharedSpace space : newSpaces) {
			current.add(space);
		}
		
		for(SharedSpace space : toRemove) {
			current.remove(space);
			InitialWindow window = (InitialWindow)sharedSpacesList.getParent();
			Node pane = window.getCenter();
			if(pane instanceof InfoPane && ((InfoPane)pane).getSharedSpace().equals(space)) {
				((InfoPane)pane).stopSynchronization();
				window.setCenter(new EmptyPane());
			}
		}
	}
}
