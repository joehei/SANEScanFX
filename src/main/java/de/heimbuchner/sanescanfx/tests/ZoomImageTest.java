package de.heimbuchner.sanescanfx.tests;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

public class ZoomImageTest extends Application {

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		// Create panel
		StackPane zoomPane = new StackPane();
		zoomPane.getChildren().add(new Circle(100, 100, 10));
		zoomPane.getChildren().add(new Circle(200, 200, 20));

		// Create operator
		AnimatedZoomOperator zoomOperator = new AnimatedZoomOperator();

		// Listen to scroll events (similarly you could listen to a button click,
		// slider, ...)
		zoomPane.setOnScroll(event -> {
			double zoomFactor = 1.5;
			if (event.getDeltaY() <= 0) {
				// zoom out
				zoomFactor = 1 / zoomFactor;
			}
			zoomOperator.zoom(zoomPane, zoomFactor, event.getSceneX(), event.getSceneY());
		});

		Scene s = new Scene(zoomPane);
		primaryStage.setScene(s);
		primaryStage.show();
	}

}
