package de.heimbuchner.sanescanfx.tests;

import javafx.application.Application;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class PlutoExplorer extends Application {

	private static final String IMAGE_CREDIT_URL = "http://www.nasa.gov/image-feature/global-mosaic-of-pluto-in-true-color";
	private static final String IMAGE_URL = "https://www.nasa.gov/sites/default/files/thumbnails/image/global-mosaic-of-pluto-in-true-color.jpg";
	private static final int MIN_PIXELS = 10;

	@Override
	public void start(Stage primaryStage) {
		Image image = new Image(IMAGE_URL);
		double width = image.getWidth();
		double height = image.getHeight();

		ImageView imageView = new ImageView(image);
		imageView.setPreserveRatio(true);
		reset(imageView, width / 2, height / 2);

		ObjectProperty<Point2D> mouseDown = new SimpleObjectProperty<>();

		imageView.setOnMousePressed(e -> {

			Point2D mousePress = imageViewToImage(imageView, new Point2D(e.getX(), e.getY()));
			mouseDown.set(mousePress);
		});

		imageView.setOnMouseDragged(e -> {
			Point2D dragPoint = imageViewToImage(imageView, new Point2D(e.getX(), e.getY()));
			shift(imageView, dragPoint.subtract(mouseDown.get()));
			mouseDown.set(imageViewToImage(imageView, new Point2D(e.getX(), e.getY())));
		});

		imageView.setOnScroll(e -> {
			double delta = -e.getDeltaY();
			updateViewport(image, width, height, imageView, e, delta);
		});

		imageView.boundsInParentProperty().addListener(new ChangeListener<Bounds>() {
			@Override
			public void changed(ObservableValue<? extends Bounds> observable, Bounds oldValue, Bounds newValue) {
				double newWidth = imageView.getViewport().getWidth();
				double newHeight = imageView.getViewport().getHeight();
				imageView.setViewport(new Rectangle2D(imageView.getViewport().getMinX(), imageView.getViewport().getMinY(), newWidth, newHeight));
			}
		});
		
		imageView.setOnMouseClicked(e -> {
			if (e.getClickCount() == 2) {
				reset(imageView, width, height);
			}
		});

		Hyperlink link = new Hyperlink("Image Credit: NASA/JHUAPL/SwRI");
		link.setOnAction(e -> getHostServices().showDocument(IMAGE_CREDIT_URL));

		link.setPadding(new Insets(10));
		link.setTooltip(new Tooltip(IMAGE_CREDIT_URL));

		HBox buttons = createButtons(width, height, imageView);
		Tooltip tooltip = new Tooltip("Scroll to zoom, drag to pan");
		Tooltip.install(buttons, tooltip);

		Pane container = new Pane(imageView);
		container.setPrefSize(800, 600);

		imageView.fitWidthProperty().bind(container.widthProperty());
		imageView.fitHeightProperty().bind(container.heightProperty());
		VBox root = new VBox(link, container, buttons);
		root.setFillWidth(true);
		VBox.setVgrow(container, Priority.ALWAYS);

		primaryStage.setScene(new Scene(root));
		primaryStage.setTitle("Pluto explorer");
		primaryStage.show();
	}

	private void updateViewport(Image image, double width, double height, ImageView imageView, ScrollEvent e,
			double delta) {
		Rectangle2D viewport = imageView.getViewport();

		double scale = clamp(Math.pow(1.005, delta), // altered the value from 1.01to zoom slower
				// don't scale so we're zoomed in to fewer than MIN_PIXELS in any direction:
				Math.min(MIN_PIXELS / viewport.getWidth(), MIN_PIXELS / viewport.getHeight()),
				// don't scale so that we're bigger than image dimensions:
				Math.max(width / viewport.getWidth(), height / viewport.getHeight()));
		if (scale != 1.0) {
			Point2D mouse = imageViewToImage(imageView, new Point2D(e.getX(), e.getY()));

			double newWidth = viewport.getWidth();
			double newHeight = viewport.getHeight();
			double imageViewRatio = (imageView.getFitWidth() / imageView.getFitHeight());
			double viewportRatio = (newWidth / newHeight);
			if (viewportRatio < imageViewRatio) {
				// adjust width to be proportional with height
				newHeight = newHeight * scale;
				newWidth = newHeight * imageViewRatio;
				if (newWidth > image.getWidth()) {
					newWidth = image.getWidth();
				}
			} else {
				// adjust height to be proportional with width
				newWidth = newWidth * scale;
				newHeight = newWidth / imageViewRatio;
				if (newHeight > image.getHeight()) {
					newHeight = image.getHeight();
				}
			}

			// To keep the visual point under the mouse from moving, we need
			// (x - newViewportMinX) / (x - currentViewportMinX) = scale
			// where x is the mouse X coordinate in the image
			// solving this for newViewportMinX gives
			// newViewportMinX = x - (x - currentViewportMinX) * scale
			// we then clamp this value so the image never scrolls out
			// of the imageview:
			double newMinX = 0;
			if (newWidth < image.getWidth()) {
				newMinX = clamp(mouse.getX() - (mouse.getX() - viewport.getMinX()) * scale, 0, width - newWidth);
			}
			double newMinY = 0;
			if (newHeight < image.getHeight()) {
				newMinY = clamp(mouse.getY() - (mouse.getY() - viewport.getMinY()) * scale, 0, height - newHeight);
			}

			imageView.setViewport(new Rectangle2D(newMinX, newMinY, newWidth, newHeight));
		}
	}

	private HBox createButtons(double width, double height, ImageView imageView) {
		Button reset = new Button("Reset");
		reset.setOnAction(e -> reset(imageView, width / 2, height / 2));
		Button full = new Button("Full view");
		full.setOnAction(e -> reset(imageView, width, height));
		HBox buttons = new HBox(10, reset, full);
		buttons.setAlignment(Pos.CENTER);
		buttons.setPadding(new Insets(10));
		return buttons;
	}

	// reset to the top left:
	private void reset(ImageView imageView, double width, double height) {
		imageView.setViewport(new Rectangle2D(0, 0, width, height));
	}

	// shift the viewport of the imageView by the specified delta, clamping so
	// the viewport does not move off the actual image:
	private void shift(ImageView imageView, Point2D delta) {
		Rectangle2D viewport = imageView.getViewport();

		double width = imageView.getImage().getWidth();
		double height = imageView.getImage().getHeight();

		double maxX = width - viewport.getWidth();
		double maxY = height - viewport.getHeight();

		double minX = clamp(viewport.getMinX() - delta.getX(), 0, maxX);
		double minY = clamp(viewport.getMinY() - delta.getY(), 0, maxY);

		imageView.setViewport(new Rectangle2D(minX, minY, viewport.getWidth(), viewport.getHeight()));
	}

	private double clamp(double value, double min, double max) {

		if (value < min)
			return min;
		if (value > max)
			return max;
		return value;
	}

	// convert mouse coordinates in the imageView to coordinates in the actual
	// image:
	private Point2D imageViewToImage(ImageView imageView, Point2D imageViewCoordinates) {
		double xProportion = imageViewCoordinates.getX() / imageView.getBoundsInLocal().getWidth();
		double yProportion = imageViewCoordinates.getY() / imageView.getBoundsInLocal().getHeight();

		Rectangle2D viewport = imageView.getViewport();
		return new Point2D(viewport.getMinX() + xProportion * viewport.getWidth(),
				viewport.getMinY() + yProportion * viewport.getHeight());
	}

	public static void main(String[] args) {
		launch(args);
	}
}