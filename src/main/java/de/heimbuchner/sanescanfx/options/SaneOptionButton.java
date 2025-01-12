package de.heimbuchner.sanescanfx.options;

import java.io.IOException;

import au.com.southsky.jfreesane.SaneDevice;
import au.com.southsky.jfreesane.SaneOption;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SaneOptionButton extends ASaneOption {

	private static final Logger log = LoggerFactory.getLogger(SaneOptionButton.class);
	private final Button button;

	protected SaneOptionButton(SaneDevice saneDevice, String saneOptionName, ObservableList<ASaneOption> saneOptionsFX)
			throws IOException {
		super(saneDevice, saneOptionName, saneOptionsFX);

		button = new Button(saneDevice.getOption(saneOptionName).getTitle());

		updateControl();
		getChildren().add(button);
		button.setOnAction(event -> {
			try {
				saneDevice.getOption(saneOptionName).setButtonValue();
				fireUpdate();
			} catch (Exception e) {
				log.error(e.getMessage(),e);
			}

		});
	}

	@Override
	void updateControl() {
		try {
			SaneOption saneOption = saneDevice.getOption(saneOptionName);
            button.setDisable(!saneOption.isActive() || !saneOption.isWriteable());
		} catch (Exception e) {
			log.error(e.getMessage(),e);
		}
	}

}
