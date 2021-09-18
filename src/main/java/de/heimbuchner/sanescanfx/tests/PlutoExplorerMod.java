package de.heimbuchner.sanescanfx.tests;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class PlutoExplorerMod extends Application {

	private static final String IMAGE_CREDIT_URL = "http://www.nasa.gov/image-feature/global-mosaic-of-pluto-in-true-color";
	private static final String IMAGE_URL = "https://images.unsplash.com/photo-1564419429381-98dbcf916478?ixlib=rb-1.2.1&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=1544&q=80";

	private ZoomPanImageView zoomPanImageView;

	@Override
	public void start(Stage primaryStage) {
		Image image = new Image(IMAGE_URL);
		double width = image.getWidth();
		double height = image.getHeight();

		Hyperlink link = new Hyperlink("Image Credit: NASA/JHUAPL/SwRI");
		link.setOnAction(e -> getHostServices().showDocument(IMAGE_CREDIT_URL));

		link.setPadding(new Insets(10));
		link.setTooltip(new Tooltip(IMAGE_CREDIT_URL));

		zoomPanImageView = new ZoomPanImageView(image);

		HBox buttons = createButtons(width, height, zoomPanImageView);
		Tooltip tooltip = new Tooltip("Scroll to zoom, drag to pan");
		Tooltip.install(buttons, tooltip);

		Pane container = new Pane(zoomPanImageView);
		container.setPrefSize(800, 600);

		zoomPanImageView.fitWidthProperty().bind(container.widthProperty());
		zoomPanImageView.fitHeightProperty().bind(container.heightProperty());
		VBox root = new VBox(link, container, buttons);
		root.setFillWidth(true);
		VBox.setVgrow(container, Priority.ALWAYS);

		primaryStage.setScene(new Scene(root));
		primaryStage.setTitle("Pluto explorer");
		primaryStage.show();
	}

	private HBox createButtons(double width, double height, ImageView imageView) {
		Button reset = new Button("Reset");
		reset.setOnAction(e -> zoomPanImageView.reset(imageView, width / 2, height / 2));
		Button full = new Button("Full view");
		full.setOnAction(e -> zoomPanImageView.reset(imageView, width, height));
		HBox buttons = new HBox(10, reset, full);
		buttons.setAlignment(Pos.CENTER);
		buttons.setPadding(new Insets(10));
		return buttons;
	}

	public static void main(String[] args) {
		launch(args);
	}
}