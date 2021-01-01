package de.heimbuchner.sanescanfx.main;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import au.com.southsky.jfreesane.SaneDevice;
import au.com.southsky.jfreesane.SaneSession;
import au.com.southsky.jfreesane.ScanListener;
import au.com.southsky.jfreesane.ScanListenerAdapter;
import de.heimbuchner.sanescanfx.options.OptionsControl;
import javafx.application.Application;
import javafx.application.Platform;
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
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class SANEScanFX extends Application implements Initializable {

	@FXML
	private Button scan;

	@FXML
	private Button previewScan;

	@FXML
	private Button timerScan;

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

	@FXML
	private StackPane scanVeil;

	@FXML
	private Label scanVeilLabel;

	@FXML
	private ProgressBar scanVeilProgress;

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		
		Image icon = new Image(SANEScanFX.class.getResourceAsStream("icon.png"));
		FXMLLoader loader = new FXMLLoader(getClass().getResource("SANEScanFX.fxml"));
		Parent parent = loader.load();
		Scene s = new Scene(parent);
		primaryStage.setScene(s);
		primaryStage.setTitle("SANEScanFX");
		primaryStage.getIcons().add(icon);
		primaryStage.show();
	}

	private OptionsControl optionsControl;

	private SaneSession session;

	private SaneDevice currentDevice;

	@Override
	public void initialize(URL location, ResourceBundle resources) {

		refreshDevice.setOnAction(event -> {
			deviceVeil.setVisible(true);

			if (currentDevice != null) {
				try {
					currentDevice.close();
					System.out.println("device closed");
				} catch (Exception e) {
					System.out.println(e.getMessage());
				}
			}

			if (session != null) {
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

					scan.setDisable(false);
					previewScan.setDisable(false);
					timerScan.setDisable(false);
				} catch (Exception e) {
					e.printStackTrace();
				}

			} else {
				scan.setDisable(true);
				previewScan.setDisable(true);
				timerScan.setDisable(true);
			}
		});

		scan.setOnAction(event -> executeScan(null, "Scan ..."));

		previewScan.setOnAction(event -> executeScan(100, "Preview ..."));

	}

	private void executeScan(Integer resolution, String scanType) {

		ScanListener progressBarUpdater = new ScanListenerAdapter() {
			@Override
			public void recordRead(SaneDevice device, final int totalBytesRead, final int imageSize) {
				final double fraction = 1.0 * totalBytesRead / imageSize;
				Platform.runLater(() -> scanVeilProgress.setProgress(fraction));
			}
		};

		scanVeil.setVisible(true);
		scanVeilProgress.setProgress(0);
		scanVeilLabel.setText(scanType);

		Task<Image> scanWorker = new Task<Image>() {
			@Override
			protected Image call() throws Exception {
				int originalRes = currentDevice.getOption("resolution").getIntegerValue();
				currentDevice.getOption("resolution").setIntegerValue(resolution == null ? originalRes : resolution);

				BufferedImage image = currentDevice.acquireImage(progressBarUpdater);

				currentDevice.getOption("resolution").setIntegerValue(originalRes);
				return SwingFXUtils.toFXImage(image, null);
			}
		};

		scanWorker.setOnFailed(wse -> {
			scanVeil.setVisible(false);
			JavaFXUtils.showExceptionDialog("Exception", null, "An exception occured", scanWorker.getException());
		});

		scanWorker.setOnSucceeded(wse -> {
			scanVeil.setVisible(false);
			imageview.setImage(scanWorker.getValue());
			imageview.setFitWidth(imageview.getImage().getWidth());
			imageview.setFitHeight(imageview.getImage().getHeight());
		});

		Thread t = new Thread(scanWorker);
		t.setDaemon(true);
		t.start();
	}

}
