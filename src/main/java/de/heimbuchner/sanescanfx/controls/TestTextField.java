package de.heimbuchner.sanescanfx.controls;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class TestTextField extends Application{

	public static void main(String[] args) {
		launch(args);
	}
	
	@Override
	public void start(Stage primaryStage) throws Exception {
		StackPane sp = new StackPane(new TextfieldCompletion());
		Scene s = new Scene(sp, 200, 200);
		primaryStage.setScene(s);
		primaryStage.show();
	}

}
