package de.heimbuchner.sanescanfx.main;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import au.com.southsky.jfreesane.OptionGroup;
import au.com.southsky.jfreesane.OptionValueType;
import au.com.southsky.jfreesane.SaneDevice;
import au.com.southsky.jfreesane.SaneException;
import au.com.southsky.jfreesane.SaneOption;
import au.com.southsky.jfreesane.SaneSession;
import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

public class SANEScanFX extends Application implements Initializable {

	@FXML
	private Button previewScan;

	@FXML
	private ImageView imageview;

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("SANEScanFX.fxml"));
		Parent parent = loader.load();
		Scene s = new Scene(parent);
		primaryStage.setScene(s);
		primaryStage.show();
	}

	private void printOptions(SaneDevice device) throws IOException {
		for (OptionGroup optionGroup : device.getOptionGroups()) {
			System.out.println(optionGroup.getTitle());
			for (SaneOption saneOption : optionGroup.getOptions()) {
				System.out.println(" " + (saneOption.isActive() && saneOption.isWriteable()) + " " + saneOption);
				if (saneOption.getType().equals(OptionValueType.STRING)) {
					List<String> validValues = saneOption.getStringConstraints();
					if (validValues != null) {
						System.out.println("   " + validValues);
					}
				}

				if (saneOption.getType().equals(OptionValueType.INT)) {
					try {
						List<Integer> validValues = saneOption.getIntegerValueListConstraint();
						if (validValues != null) {
							System.out.println("   " + validValues);
						}
					} catch (Exception e2) {
						// TODO: handle exception
					}

				}
			}
		}
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		previewScan.setOnAction(e -> {
			try {
				InetAddress adress = InetAddress.getByName("192.168.178.57");
				System.out.println(adress);
				SaneSession session = SaneSession.withRemoteSane(adress);
				List<SaneDevice> devices = session.listDevices();
				System.out.println(devices);
				SaneDevice device = devices.get(0);
				device.open();

				printOptions(device);

				System.out.println(device.getOption("mode").getStringValue());
				device.getOption("mode").setStringValue("Gray");

				printOptions(device);

				BufferedImage image = device.acquireImage();
				Image imageFX = SwingFXUtils.toFXImage(image, null);
				imageview.setImage(imageFX);
				device.close();
			} catch (IOException | SaneException e1) {
				e1.printStackTrace();
			}

		});

	}

}
