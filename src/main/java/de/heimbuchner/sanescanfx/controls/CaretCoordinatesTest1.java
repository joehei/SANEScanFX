package de.heimbuchner.sanescanfx.controls;

import de.heimbuchner.sanescanfx.main.JavaFXUtils;
import javafx.application.Application;
import javafx.geometry.Point2D;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Path;
import javafx.stage.Popup;
import javafx.stage.Stage;

public class CaretCoordinatesTest1 extends Application {

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) {
		final TextArea textArea = new TextArea();
		final BorderPane root = new BorderPane();
		root.setCenter(textArea);

		final Popup popup = new Popup();
		popup.setAutoHide(true);
		VBox suggestionBox = new VBox();
		suggestionBox.setStyle("-fx-border-color: -fx-accent");
		popup.getContent().add(suggestionBox);
		suggestionBox.getChildren().add(new Label("Here are some suggestions:"));
		for (int i = 1; i <= 10; i++) {
			suggestionBox.getChildren().add(new Label("Suggestion " + i));
		}

		textArea.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
			if (event.getCode() == KeyCode.SPACE && event.isControlDown()) {
				Path caret = JavaFXUtils.findCaret(textArea);
				Point2D screenLoc = JavaFXUtils.findScreenLocation(caret);
				popup.show(textArea, screenLoc.getX()+4, screenLoc.getY() + 20);
			}
		});

		textArea.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
			if (popup.isShowing()) {
				popup.hide();
			}
		});

		primaryStage.setScene(new Scene(root, 600, 400));
		primaryStage.show();
	}

}