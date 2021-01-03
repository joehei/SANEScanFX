package de.heimbuchner.sanescanfx.options;

import au.com.southsky.jfreesane.SaneDevice;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.layout.StackPane;

abstract class ASaneOption extends StackPane {

	protected String saneOptionName;

	protected SaneDevice saneDevice;

	protected ObservableList<ASaneOption> saneOptionsFX;

	protected ASaneOption(SaneDevice saneDevice, String saneOptionName, ObservableList<ASaneOption> saneOptionsFX) {
		this.saneDevice = saneDevice;
		this.saneOptionName = saneOptionName;
		this.saneOptionsFX = saneOptionsFX;
		saneOptionsFX.add(this);
		setAlignment(Pos.CENTER_LEFT);
	}

	protected void fireUpdate() {
		for (ASaneOption aSaneOption : saneOptionsFX) {
			aSaneOption.updateControl();
		}
	}

	public String getSaneOption() {
		return saneOptionName;
	}

	abstract void updateControl();

}
