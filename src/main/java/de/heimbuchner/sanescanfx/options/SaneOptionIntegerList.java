package de.heimbuchner.sanescanfx.options;

import java.io.IOException;
import java.util.List;

import au.com.southsky.jfreesane.SaneDevice;
import au.com.southsky.jfreesane.SaneException;
import au.com.southsky.jfreesane.SaneOption;
import javafx.collections.ObservableList;
import javafx.scene.control.ChoiceBox;

public class SaneOptionIntegerList extends ASaneOption {

	private final ChoiceBox<Integer> intCb;

	protected SaneOptionIntegerList(SaneDevice saneDevice, String saneOptionName,
			ObservableList<ASaneOption> saneOptionsFX) throws IOException {
		super(saneDevice, saneOptionName, saneOptionsFX);

		SaneOption saneOption = saneDevice.getOption(saneOptionName);
		List<Integer> validIntValues = saneOption.getIntegerValueListConstraint();
		intCb = new ChoiceBox<>();
		intCb.getItems().setAll(validIntValues);

		updateControl();
		getChildren().add(intCb);
		intCb.getSelectionModel().selectedItemProperty().addListener((obs, ov, nv) -> {
			try {
				saneDevice.getOption(saneOptionName).setIntegerValue(nv);
				fireUpdate();
			} catch (IOException | SaneException e) {
				e.printStackTrace();
			}
		});
	}

	@Override
	void updateControl() {
		try {
			SaneOption saneOption = saneDevice.getOption(saneOptionName);
			if (saneOption.isActive() && saneOption.isWriteable()) {
				intCb.getSelectionModel().select(Integer.valueOf(saneOption.getIntegerValue()));
				intCb.setDisable(false);
			} else {
				intCb.setDisable(true);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
