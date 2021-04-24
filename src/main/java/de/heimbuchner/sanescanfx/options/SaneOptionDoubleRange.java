package de.heimbuchner.sanescanfx.options;

import java.io.IOException;

import au.com.southsky.jfreesane.SaneDevice;
import au.com.southsky.jfreesane.SaneOption;
import javafx.collections.ObservableList;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.HBox;

public class SaneOptionDoubleRange extends ASaneOption {

	private Slider slider;

	protected SaneOptionDoubleRange(SaneDevice saneDevice, String saneOptionName,
			ObservableList<ASaneOption> saneOptionsFX) throws IOException {
		super(saneDevice, saneOptionName, saneOptionsFX);

		SaneOption saneOption = saneDevice.getOption(saneOptionName);
		HBox box = new HBox(5);
		box.getChildren().add(new Label(String.format("%.2f", saneOption.getRangeConstraints().getMinimumFixed())));
		slider = new Slider();
		slider.setMin(saneOption.getRangeConstraints().getMinimumFixed());
		slider.setMax(saneOption.getRangeConstraints().getMaximumFixed());

		box.getChildren().add(slider);
		box.getChildren().add(new Label(String.format("%.2f", saneOption.getRangeConstraints().getMaximumFixed())));

		updateControl();
		getChildren().add(box);
		slider.valueProperty().addListener((obs, ov, nv) -> {
			try {
				saneDevice.getOption(saneOptionName).setFixedValue(nv.doubleValue());
				fireUpdate();
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}

	@Override
	void updateControl() {
		try {
			SaneOption saneOption = saneDevice.getOption(saneOptionName);
			if (saneOption.isActive() && saneOption.isWriteable()) {
				slider.setValue(saneOption.getFixedValue());
				slider.setDisable(false);
			} else {
				slider.setDisable(true);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
