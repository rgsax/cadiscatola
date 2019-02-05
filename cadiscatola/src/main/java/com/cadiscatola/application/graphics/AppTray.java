package com.cadiscatola.application.graphics;
import java.awt.AWTException;
//import java.awt.CheckboxMenuItem;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
//import java.awt.event.ItemEvent;
//import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javafx.application.Platform;
import javafx.stage.Stage;

public class AppTray {
	private SystemTray tray;
	
	private MenuItem exitButton; 
//	private CheckboxMenuItem syncToggle; 
	private TrayIcon ticon; 
	
	private Stage stage;
	
	public void showApplicationWindow() {
		Platform.runLater(() -> stage.show());
	}
	
	public void hideApplicationWindow() {
		if(stage.isShowing())
			Platform.runLater(() -> stage.hide());
	}
	
	public void maximizeApplicationWindow() {
		if(stage.isShowing())
			Platform.runLater(() -> stage.setIconified(false));
	}
	
	public void quitApplication() {
		System.out.println("QUIT APPLICATION");
		System.exit(0);
	}
	
	public void startSyncing() {
		System.out.println("START SYNCING");
		ticon.setToolTip("Syncing...");
	}
	
	public void stopSyncing() {
		System.out.println("STOP SYNCING"); 
		ticon.setToolTip("Stop syncing...");
	}
	
	private void setupListeners() {
		exitButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				quitApplication();
			}
		});
		
//		syncToggle.addItemListener(new ItemListener() {
//			@Override
//			public void itemStateChanged(ItemEvent e) {
//				if (e.getStateChange() == ItemEvent.SELECTED) {
//					startSyncing();
//				} else if (e.getStateChange() == ItemEvent.DESELECTED) {
//					stopSyncing();
//				}
//			}
//		});
		
		ticon.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if(stage.isIconified())
					maximizeApplicationWindow();
				else if(!stage.isShowing()) 
					showApplicationWindow();
				else
					hideApplicationWindow();
			}
		});	
	}
	
	private void setupWidgets() {
		Image img = Toolkit.getDefaultToolkit().getImage(ClassLoader.getSystemResource("trayIcon.png"));
		
		tray = SystemTray.getSystemTray(); 
		ticon = new TrayIcon(img);
		ticon.setImageAutoSize(true);
		
		PopupMenu pp = new PopupMenu();

		exitButton = new MenuItem("Close application");
		//syncToggle = new CheckboxMenuItem("Sync all Sharespaces");
		//syncToggle.setState(false);
		
		pp.add(exitButton);
		//pp.add(syncToggle); 
			
		ticon.setPopupMenu(pp);
		try {
			tray.add(ticon);
		} catch (AWTException e) {
			e.printStackTrace();
		}
	}
	
	public AppTray(Stage stage) {
		this.stage = stage;
		
		setupWidgets(); 
		setupListeners();
		showApplicationWindow();
	}
}
