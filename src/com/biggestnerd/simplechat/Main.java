package com.biggestnerd.simplechat;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.Optional;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Pair;


public class Main extends Application {
	
	private static Main instance;
	
	private TextField inputField;
	private ObservableList<String> messages;
	private Connection connection;
	
	private void sendMessage() {
		connection.sendMessage(inputField.getText().trim());
		inputField.setText("");
	}
	
	public void addMessage(String msg) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				if(messages.get(0).equals("")) messages.remove(0);
				messages.add(msg);
			}
		});
	}
	
	private void clearChat() {
		messages.clear();
		for(int i = 0; i < 15; i++) {
			messages.add("");
		}
	}
	
	@Override
	public void start(Stage primaryStage) {
		instance = this;
		connection = new Connection();
		try {
			BorderPane root = new BorderPane();
			Scene scene = new Scene(root,400,400);
			root.setTop(createMenuBar(primaryStage));
			root.setCenter(createMessageArea());
			root.setBottom(createInputBox(scene.getWidth()));
			primaryStage.setScene(scene);
			primaryStage.setResizable(false);
			primaryStage.sizeToScene();
			primaryStage.show();
			primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
				@Override
				public void handle(WindowEvent event) {
					connection.disconnect();
				}
			});
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private Node createMenuBar(Stage stage) {
		Menu connectionMenu = new Menu("Connection");
		MenuItem connect = new MenuItem("Connect");
		connect.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				showConnectDialog();
			}
		});
		connectionMenu.getItems().add(connect);
		MenuItem clear = new MenuItem("Clear Chat");
		clear.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				clearChat();
			}
		});
		connectionMenu.getItems().add(clear);
		MenuItem save = new MenuItem("Save Session");
		save.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				openSaveFileChooser(stage);
			}
		});
		connectionMenu.getItems().add(save);
		connectionMenu.getItems().add(new SeparatorMenuItem());
		MenuItem disconnect = new MenuItem("Disconnect");
		disconnect.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				connection.disconnect();
			}
		});
		connectionMenu.getItems().add(disconnect);
		MenuBar menu = new MenuBar();
		menu.getMenus().add(connectionMenu);
		return menu;
	}
	
	private Node createInputBox(double sceneWidth) {
		HBox inputBox = new HBox();
		inputField = new TextField();
		Button sendButton = new Button("Send");
		sendButton.setPrefWidth(100);
		sendButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				sendMessage();
			}	
		});
		inputField.setOnKeyPressed(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent event) {
				if(event.getCode().equals(KeyCode.ENTER)) {
					sendMessage();
				}
			}
		});
		inputField.setOnKeyTyped(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent event) {
				if(!connection.isConnected()) event.consume();
			}
		});
		inputField.setPrefWidth(sceneWidth - sendButton.getWidth());
		inputBox.getChildren().addAll(inputField, sendButton);
		
		Platform.runLater(() -> inputField.requestFocus());
		return inputBox;
	}
	
	private Node createMessageArea() {
		messages = FXCollections.observableArrayList();
		//15 messages can be on screen at any given time;
		for(int i = 0; i < 15; i++) {
			messages.add("");
		}
		ListView<String> messageView = new ListView<String>();
		messageView.setItems(messages);
		messages.addListener(new ListChangeListener<String>() {
			@Override
			public void onChanged(Change<? extends String> change) {
				if(messages.size() > 0) {
					messageView.scrollTo(messages.size() - 1);
				}
			}
		});
		return messageView;
	}
	
	@SuppressWarnings("unchecked")
	private void showConnectDialog() {
		Dialog<Pair<String, String>[]> dialog = new Dialog<>();
		dialog.setTitle("Connect");
		
		ButtonType connectType = new ButtonType("Connect", ButtonData.OK_DONE);
		dialog.getDialogPane().getButtonTypes().add(connectType);
		
		GridPane grid = new GridPane();
		grid.setHgap(10);
		grid.setVgap(10);
		grid.setPadding(new Insets(20, 150, 10, 10));
		
		TextField server = new TextField();
		server.setPromptText("IP");
		TextField port = new TextField(String.valueOf(Connection.DEFAULT_PORT));
		port.setPromptText("Port");
		TextField name = new TextField();
		port.setPromptText("Name");
		TextField pass = new TextField();
		pass.setPromptText("Password");
		grid.add(new Label("IP:"), 0, 0);
		grid.add(server, 1, 0);
		grid.add(new Label("Port:"), 0, 1);
		grid.add(port, 1, 1);
		grid.add(new Label("Name:"), 0, 2);
		grid.add(name, 1, 2);
		grid.add(new Label("Pass:"), 0, 3);
		grid.add(pass, 1, 3);
		
		Node connectButton = dialog.getDialogPane().lookupButton(connectType);
		connectButton.setDisable(true);
		
		server.textProperty().addListener((observable, oldValue, newValue) -> {
			connectButton.setDisable(newValue.trim().isEmpty() || name.getText().trim().isEmpty());
		});
		
		name.textProperty().addListener((observable, oldValue, newValue) -> {
			connectButton.setDisable(newValue.trim().isEmpty() || server.getText().trim().isEmpty());
		});
		
		dialog.getDialogPane().setContent(grid);
		
		Platform.runLater(() -> server.requestFocus());
		
		dialog.setResultConverter(dialogButton -> {
			if(dialogButton == connectType) {
				return new Pair[]{new Pair<>(server.getText(), port.getText()), 
					new Pair<>(name.getText(), pass.getText())};
			}
			return null;
		});
		
		Optional<Pair<String, String>[]> result = dialog.showAndWait();
		if(result != null) {
			Pair<String, String>[] pairs = result.get();
			int p = Connection.DEFAULT_PORT;
			try {
				p = Integer.parseInt(pairs[0].getValue());
			} catch (NumberFormatException e) {}
			connection.connect(pairs[0].getKey(), p, pairs[1].getKey(), pairs[1].getValue());
		}
	}
	
	private void openSaveFileChooser(Stage stage) {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Save Chat Session");
		File dir = new File(System.getProperty("user.home"), "SimpleChat");
		if(!dir.exists()) dir.mkdirs();
		fileChooser.setInitialDirectory(dir);
		fileChooser.setInitialFileName(getSaveFileName());
		File file = fileChooser.showSaveDialog(stage);
		if(file != null) {
			try {
				PrintWriter writer = new PrintWriter(new FileWriter(file));
				for(String line : messages) {
					writer.println(line);
				}
				writer.flush();
				writer.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private String getSaveFileName() {
		return Calendar.YEAR + "-" + Calendar.MONTH + "-" + Calendar.DATE
			+ "-" + Calendar.HOUR_OF_DAY + "-" + Calendar.MINUTE + ".txt";
	}
	
	public static Main getInstance() {
		return instance;
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}
