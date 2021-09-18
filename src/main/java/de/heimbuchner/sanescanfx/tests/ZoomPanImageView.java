package de.heimbuchner.sanescanfx.tests;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class ZoomPanImageView extends ImageView {

	private static final int MIN_PIXELS = 10;

	public ZoomPanImageView() {
		super();
		imageProperty().addListener((obs, ov, nv) -> {
			if (nv != null) {
				init();
			}
		});
	}

	public ZoomPanImageView(Image image) {
		super(image);
		imageProperty().addListener((obs, ov, nv) -> {
			if (nv != null) {
				init();
			}
		});
		init();
	}

	private double clamp(double value, double min, double max) {
		if (value < min)
			return min;
		if (value > max)
			return max;
		return value;
	}

	private Point2D imageViewToImage(ImageView imageView, Point2D imageViewCoordinates) {
		double xProportion = imageViewCoordinates.getX() / imageView.getBoundsInLocal().getWidth();
		double yProportion = imageViewCoordinates.getY() / imageView.getBoundsInLocal().getHeight();
		return new Point2D(getViewport().getMinX() + xProportion * getViewport().getWidth(),
				getViewport().getMinY() + yProportion * getViewport().getHeight());
	}

	private void init() {

		setPreserveRatio(true);
		reset(this, getImage().getWidth(), getImage().getHeight());

		ObjectProperty<Point2D> mouseDown = new SimpleObjectProperty<>();

		setOnMousePressed(e -> {
			Point2D mousePress = imageViewToImage(this, new Point2D(e.getX(), e.getY()));
			mouseDown.set(mousePress);
		});

		setOnMouseDragged(e -> {
			Point2D dragPoint = imageViewToImage(this, new Point2D(e.getX(), e.getY()));
			shift(this, dragPoint.subtract(mouseDown.get()));
			mouseDown.set(imageViewToImage(this, new Point2D(e.getX(), e.getY())));
		});

		setOnScroll(e -> {
			double delta = -e.getDeltaY();

			double scale = clamp(Math.pow(1.005, delta), // altered the value from 1.01to zoom slower
					// don't scale so we're zoomed in to fewer than MIN_PIXELS in any direction:
					Math.min(MIN_PIXELS / getViewport().getWidth(), MIN_PIXELS / getViewport().getHeight()),
					// don't scale so that we're bigger than image dimensions:
					Math.max(getImage().getWidth() / getViewport().getWidth(),
							getImage().getHeight() / getViewport().getHeight()));
			if (scale != 1.0) {
				Point2D mouse = imageViewToImage(this, new Point2D(e.getX(), e.getY()));

				double newWidth = getViewport().getWidth();
				double newHeight = getViewport().getHeight();
				double imageViewRatio = (getFitWidth() / getFitHeight());
				double viewportRatio = (newWidth / newHeight);
				if (viewportRatio < imageViewRatio) {
					// adjust width to be proportional with height
					newHeight = newHeight * scale;
					newWidth = newHeight * imageViewRatio;
					if (newWidth > getImage().getWidth()) {
						newWidth = getImage().getWidth();
					}
				} else {
					// adjust height to be proportional with width
					newWidth = newWidth * scale;
					newHeight = newWidth / imageViewRatio;
					if (newHeight > getImage().getHeight()) {
						newHeight = getImage().getHeight();
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
				if (newWidth < getImage().getWidth()) {
					newMinX = clamp(mouse.getX() - (mouse.getX() - getViewport().getMinX()) * scale, 0,
							getImage().getWidth() - newWidth);
				}
				double newMinY = 0;
				if (newHeight < getImage().getHeight()) {
					newMinY = clamp(mouse.getY() - (mouse.getY() - getViewport().getMinY()) * scale, 0,
							getImage().getHeight() - newHeight);
				}

				setViewport(new Rectangle2D(newMinX, newMinY, newWidth, newHeight));
			}
		});

		setOnMouseClicked(e -> {
			if (e.getClickCount() == 2) {
				reset(this, getImage().getWidth(), getImage().getHeight());
			}
		});
	}

	// reset to the top left:
	public void reset(ImageView imageView, double width, double height) {
		imageView.setViewport(new Rectangle2D(0, 0, width, height));
	}

	// shift the viewport of the imageView by the specified delta, clamping so
	// the viewport does not move off the actual image:
	private void shift(ImageView imageView, Point2D delta) {
		double width = imageView.getImage().getWidth();
		double height = imageView.getImage().getHeight();

		double maxX = width - getViewport().getWidth();
		double maxY = height - getViewport().getHeight();

		double minX = clamp(getViewport().getMinX() - delta.getX(), 0, maxX);
		double minY = clamp(getViewport().getMinY() - delta.getY(), 0, maxY);

		imageView.setViewport(new Rectangle2D(minX, minY, getViewport().getWidth(), getViewport().getHeight()));
	}

}
