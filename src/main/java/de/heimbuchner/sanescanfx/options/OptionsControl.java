package de.heimbuchner.sanescanfx.options;

import au.com.southsky.jfreesane.OptionGroup;
import au.com.southsky.jfreesane.OptionValueConstraintType;
import au.com.southsky.jfreesane.SaneDevice;
import au.com.southsky.jfreesane.SaneOption;
import de.heimbuchner.sanescanfx.main.JavaFXUtils;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

public class OptionsControl extends VBox {

	private final ObjectProperty<SaneDevice> device = new SimpleObjectProperty<>();

	private final ObservableList<ASaneOption> saneOptionsFX = FXCollections.observableArrayList();

	public OptionsControl(SaneDevice device) {
		this.device.setValue(device);
		this.device.addListener((obs, ov, nv) -> buildControls());
		buildControls();
	}

	private void buildControls() {
		getChildren().clear();
		if (device.getValue() != null) {
			SaneDevice dev = device.getValue();
			try {
				for (OptionGroup optionGroup : dev.getOptionGroups()) {
					GridPane gp = getPane();
					TitledPane pane = new TitledPane(optionGroup.getTitle(), gp);
					getChildren().add(pane);

					for (int row = 0; row < optionGroup.getOptions().size(); row++) {
						SaneOption saneOption = optionGroup.getOptions().get(row);
						Label l = new Label(saneOption.getTitle());
						Tooltip tooltip = new Tooltip(saneOption.getDescription() + " [" + saneOption.getName() + ","
								+ saneOption.getType() + "]");
						Tooltip.install(l, tooltip);
						gp.add(l, 0, row);

						ASaneOption saneOptionFX = null;
						switch (saneOption.getType()) {
							case STRING ->
									saneOptionFX = new SaneOptionStringList(dev, saneOption.getName(), saneOptionsFX);
							case INT -> {
								if (saneOption.getConstraintType()
										.equals(OptionValueConstraintType.VALUE_LIST_CONSTRAINT)) {
									saneOptionFX = new SaneOptionIntegerList(dev, saneOption.getName(), saneOptionsFX);
								} else if (saneOption.getConstraintType()
										.equals(OptionValueConstraintType.RANGE_CONSTRAINT)) {
									saneOptionFX = new SaneOptionIntegerRange(dev, saneOption.getName(), saneOptionsFX);
								} else {
									System.out.println(saneOption);
								}
							}
							case BOOLEAN ->
									saneOptionFX = new SaneOptionBoolean(dev, saneOption.getName(), saneOptionsFX);
							case FIXED ->
									saneOptionFX = new SaneOptionDoubleRange(dev, saneOption.getName(), saneOptionsFX);
							case BUTTON ->
									saneOptionFX = new SaneOptionButton(dev, saneOption.getName(), saneOptionsFX);
							default -> {
							}
						}
						if (saneOptionFX != null) {
							gp.add(saneOptionFX, 1, row);
						}

					}

				}
			} catch (Exception e) {
				JavaFXUtils.showExceptionDialog("Exception", null, "An exception occurred", e);
			}
		}
	}

	private GridPane getPane() {
		GridPane gp = new GridPane();
		gp.setHgap(5);
		gp.setVgap(5);
		ColumnConstraints columnConstraints = new ColumnConstraints();
		columnConstraints.setPrefWidth(150);
		gp.getColumnConstraints().add(columnConstraints);
		return gp;
	}

}
