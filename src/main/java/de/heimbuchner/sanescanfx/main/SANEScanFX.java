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
import de.heimbuchner.sanescanfx.options.OptionsControl;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class SANEScanFX extends Application implements Initializable {

	@FXML
	private Button previewScan;

	@FXML
	private ImageView imageview;

	@FXML
	private TextField port;

	@FXML
	private TextField host;

	@FXML
	private Button refreshDevice;

	@FXML
	private ComboBox<SaneDeviceFX> deviceList;

	@FXML
	private StackPane deviceVeil;

	@FXML
	private ScrollPane scanOption;

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

	private void printOptions(SaneDevice device) throws IOException, SaneException {
		for (OptionGroup optionGroup : device.getOptionGroups()) {
			System.out.println(optionGroup.getTitle());
			for (SaneOption saneOption : optionGroup.getOptions()) {
				System.out.println(
						" " + saneOption.getTitle() + " " + saneOption.isActive() + " " + saneOption.isWriteable());
				if (saneOption.getType().equals(OptionValueType.STRING)) {
					List<String> validValues = saneOption.getStringConstraints();
					if (validValues != null) {
						System.out.println("   " + saneOption.getStringValue() + " " + validValues);
					}
				}

				try {
					if (saneOption.getType().equals(OptionValueType.INT)) {
						List<Integer> validValues = saneOption.getIntegerValueListConstraint();
						if (validValues != null) {
							System.out.println("   " + validValues);
						}
					}
				} catch (Exception e) {
					// TODO: handle exception
				}

			}
		}
	}

	private OptionsControl optionsControl;

	private SaneSession session;
	
	private SaneDevice currentDevice;
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {

		refreshDevice.setOnAction(event -> {
			deviceVeil.setVisible(true);
			
			if (currentDevice!=null) {
				try {
					currentDevice.close();
					System.out.println("device closed");
				} catch (Exception e) {
					System.out.println(e.getMessage());
				}
			}
			
			if (session!=null) {
				try {
					session.close();
					System.out.println("session closed");
				} catch (Exception e) {
					System.out.println(e.getMessage());
				}
			}

			Task<List<SaneDeviceFX>> worker = new Task<List<SaneDeviceFX>>() {
				@Override
				protected List<SaneDeviceFX> call() throws Exception {
					InetAddress adress = InetAddress.getByName(host.getText());
					Integer intPort = Integer.valueOf(port.getText());

					try {
						session = SaneSession.withRemoteSane(adress, intPort);
					} catch (Exception e2) {
						System.out.println(e2.getMessage());
						session = SaneSession.withRemoteSane(adress, intPort);
					}
					
					List<SaneDevice> devices = session.listDevices();
					List<SaneDeviceFX> fxList = FXCollections.observableArrayList();
					for (SaneDevice saneDevice : devices) {
						fxList.add(new SaneDeviceFX(saneDevice));
					}
					return fxList;
				}
			};

			worker.setOnFailed(wse -> {
				deviceVeil.setVisible(false);
				JavaFXUtils.showExceptionDialog("Exception", null, "An exception occured", worker.getException());
			});

			worker.setOnSucceeded(wse -> {
				deviceVeil.setVisible(false);
				deviceList.getItems().setAll(worker.getValue());
				deviceList.getSelectionModel().selectFirst();
			});

			Thread t = new Thread(worker);
			t.setDaemon(true);
			t.start();
		});

		deviceList.getSelectionModel().selectedItemProperty().addListener((obs, ov, nv) -> {

			if (ov != null) {
				try {
					ov.getSaneDevice().close();
				} catch (IOException e) {
					System.out.println(e.getMessage());
				}
			}

			scanOption.setContent(null);

			if (nv != null) {
				try {
					currentDevice = nv.getSaneDevice();
					currentDevice.open();
					optionsControl = new OptionsControl(currentDevice);
					scanOption.setContent(optionsControl);
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		});

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
				device.getOption("mode").setStringValue("Color");

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
