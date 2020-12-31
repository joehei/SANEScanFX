package de.heimbuchner.sanescanfx.options;

import java.io.IOException;
import java.util.List;

import au.com.southsky.jfreesane.SaneDevice;
import au.com.southsky.jfreesane.SaneException;
import au.com.southsky.jfreesane.SaneOption;
import javafx.collections.ObservableList;
import javafx.scene.control.ChoiceBox;

public class SaneOptionStringList extends ASaneOption {

	private ChoiceBox<String> cb;

	public SaneOptionStringList(SaneDevice saneDevice, String saneOptionName, ObservableList<ASaneOption> saneOptionsFX)
			throws IOException, SaneException {
		super(saneDevice, saneOptionName, saneOptionsFX);

		SaneOption saneOption = saneDevice.getOption(saneOptionName);
		List<String> validStringValues = saneOption.getStringConstraints();
		cb = new ChoiceBox<>();
		cb.getItems().setAll(validStringValues);
		updateControl();
		cb.getSelectionModel().selectedItemProperty().addListener((obs, ov, nv) -> {
			try {
				saneDevice.getOption(saneOptionName).setStringValue(nv);
				fireUpdate();
			} catch (IOException | SaneException e) {
				e.printStackTrace();
			}
		});
		getChildren().add(cb);
	}

	@Override
	void updateControl() {
		try {
			SaneOption saneOption = saneDevice.getOption(saneOptionName);
			if (saneOption.isActive() && saneOption.isWriteable()) {
				cb.getSelectionModel().select(saneOption.getStringValue());
				cb.setDisable(false);
			} else {
				cb.setDisable(true);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
