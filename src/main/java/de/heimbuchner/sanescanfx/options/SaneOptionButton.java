package de.heimbuchner.sanescanfx.options;

import java.io.IOException;

import au.com.southsky.jfreesane.SaneDevice;
import au.com.southsky.jfreesane.SaneOption;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;

public class SaneOptionButton extends ASaneOption {

	private Button button;

	protected SaneOptionButton(SaneDevice saneDevice, String saneOptionName,
			ObservableList<ASaneOption> saneOptionsFX) throws IOException {
		super(saneDevice, saneOptionName, saneOptionsFX);

		button = new Button(saneDevice.getOption(saneOptionName).getTitle());

		updateControl();
		getChildren().add(button);
		button.setOnAction(event -> {
			try {
				saneDevice.getOption(saneOptionName).setButtonValue();
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
				button.setDisable(false);
			} else {
				button.setDisable(true);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
