package de.heimbuchner.sanescanfx.options;

import au.com.southsky.jfreesane.SaneDevice;
import au.com.southsky.jfreesane.SaneOption;
import javafx.collections.ObservableList;
import javafx.scene.control.CheckBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SaneOptionBoolean extends ASaneOption {

    private static final Logger log = LoggerFactory.getLogger(SaneOptionBoolean.class);
    private final CheckBox checkBox;

    protected SaneOptionBoolean(SaneDevice saneDevice, String saneOptionName, ObservableList<ASaneOption> saneOptionsFX) {
        super(saneDevice, saneOptionName, saneOptionsFX);

        checkBox = new CheckBox();

        updateControl();
        getChildren().add(checkBox);
        checkBox.selectedProperty().addListener((obs, ov, nv) -> {
            try {
                saneDevice.getOption(saneOptionName).setBooleanValue(nv);
                fireUpdate();
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        });
    }

    @Override
    void updateControl() {
        try {
            SaneOption saneOption = saneDevice.getOption(saneOptionName);
            if (saneOption.isActive() && saneOption.isWriteable()) {
                checkBox.setSelected(saneOption.getBooleanValue());
                checkBox.setDisable(false);
            } else {
                checkBox.setDisable(true);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

}
