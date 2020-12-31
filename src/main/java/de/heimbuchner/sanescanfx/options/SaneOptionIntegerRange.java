package de.heimbuchner.sanescanfx.options;

import java.io.IOException;

import au.com.southsky.jfreesane.SaneDevice;
import au.com.southsky.jfreesane.SaneOption;
import javafx.collections.ObservableList;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.HBox;

public class SaneOptionIntegerRange extends ASaneOption {

	private Slider slider;
	
	private Label currentValue;

	protected SaneOptionIntegerRange(SaneDevice saneDevice,String saneOptionName, ObservableList<ASaneOption> saneOptionsFX) throws IOException {
		super(saneDevice, saneOptionName, saneOptionsFX);

		SaneOption saneOption = saneDevice.getOption(saneOptionName);
		HBox box = new HBox(5);
		box.getChildren().add(new Label(saneOption.getRangeConstraints().getMinimumInteger() + ""));
		slider = new Slider();
		slider.setMin(saneOption.getRangeConstraints().getMinimumInteger());
		slider.setMax(saneOption.getRangeConstraints().getMaximumInteger());
		slider.valueProperty().addListener((obse, oldval, newVal) -> slider.setValue(newVal.intValue()));

		box.getChildren().add(slider);
		box.getChildren().add(new Label(saneOption.getRangeConstraints().getMaximumInteger() + ""));
		
		currentValue = new Label("");
		box.getChildren().add(currentValue);

		updateControl();
		getChildren().add(box);
		slider.valueProperty().addListener((obs, ov, nv) -> {
			try {
				saneDevice.getOption(saneOptionName).setIntegerValue(nv.intValue());
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
				int v = saneOption.getIntegerValue();
				slider.setValue(v);
				slider.setDisable(false);
				currentValue.setText(v+"");
			} else {
				slider.setDisable(true);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
