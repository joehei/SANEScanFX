package de.heimbuchner.sanescanfx.main;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

import au.com.southsky.jfreesane.SaneDevice;
import au.com.southsky.jfreesane.SaneSession;
import au.com.southsky.jfreesane.ScanListener;
import au.com.southsky.jfreesane.ScanListenerAdapter;
import de.heimbuchner.sanescanfx.controls.TextfieldCompletion;
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
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

public class SANEScanFX extends Application implements Initializable {

	public static void main(String[] args) {
		launch(args);
	}

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

	@FXML
	private ChoiceBox<Integer> timerSec;

	@FXML
	private GridPane fileGrid;

	@FXML
	private Button chooseOutputDir;

	@FXML
	private ChoiceBox<String> fileFormat;

	@FXML
	private Label filePathPreview;

	private TextfieldCompletion outputDirectory;

	private TextfieldCompletion fileNamePattern;

	private OptionsControl optionsControl;

	private SaneSession session;

	private SaneDevice currentDevice;

	private int fileCounter = 0;

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

	@Override
	public void initialize(URL location, ResourceBundle resources) {

		timerSec.getItems().setAll(Arrays.asList(1, 5, 10, 15, 20, 25, 30, 45, 60, 120, 180));
		timerSec.getSelectionModel().select(Integer.valueOf(30));

		List<String> suggestions = new ArrayList<>(Arrays.asList("${counter}", "${counter2}", "${counter3}",
				"${counter4}", "${counter5}", "${counter6}", "${day}", "${hour12}", "${hour24}", "${minute}",
				"${month}", "${resolution}", "${sec}", "${year}"));

		outputDirectory = new TextfieldCompletion();
		outputDirectory.getSuggestionListItems().setAll(suggestions);
		outputDirectory.textProperty().addListener((obs, ov, nv) -> updateFileNamePreview());
		fileGrid.add(outputDirectory, 1, 0);

		chooseOutputDir.setOnAction(event -> {
			DirectoryChooser directoryChooser = new DirectoryChooser();
			File selectedDirectory = directoryChooser.showDialog(chooseOutputDir.getScene().getWindow());
			if (selectedDirectory != null) {
				outputDirectory.setText(selectedDirectory.toPath().toString());
			}
		});

		fileNamePattern = new TextfieldCompletion();
		fileNamePattern.getSuggestionListItems().setAll(suggestions);
		fileNamePattern.textProperty().addListener((obs, ov, nv) -> updateFileNamePreview());
		fileNamePattern.setText("${year}-${month}-${day}_${hour24}-${minute}-${sec}");
		fileGrid.add(fileNamePattern, 1, 1);

		fileFormat.getItems().addAll(Arrays.asList("png", "jpg", "bmp"));
		fileFormat.getSelectionModel().selectFirst();
		fileFormat.getSelectionModel().selectedItemProperty().addListener((obs, ov, nv) -> updateFileNamePreview());
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

	private String substituteVars(String s) {
		LocalDateTime ldt = LocalDateTime.now();

		String rv = s.replace("${counter}", fileCounter + "");
		rv = rv.replace("${counter2}", String.format("%02d", fileCounter));
		rv = rv.replace("${counter3}", String.format("%03d", fileCounter));
		rv = rv.replace("${counter4}", String.format("%04d", fileCounter));
		rv = rv.replace("${counter5}", String.format("%05d", fileCounter));
		rv = rv.replace("${counter6}", String.format("%06d", fileCounter));

		rv = rv.replace("${day}", ldt.format(DateTimeFormatter.ofPattern("dd")));
		rv = rv.replace("${month}", ldt.format(DateTimeFormatter.ofPattern("MM")));
		rv = rv.replace("${hour12}", ldt.format(DateTimeFormatter.ofPattern("hh")));
		rv = rv.replace("${hour24}", ldt.format(DateTimeFormatter.ofPattern("HH")));
		rv = rv.replace("${minute}", ldt.format(DateTimeFormatter.ofPattern("mm")));
		rv = rv.replace("${sec}", ldt.format(DateTimeFormatter.ofPattern("ss")));
		rv = rv.replace("${year}", ldt.format(DateTimeFormatter.ofPattern("yyyy")));

		String res = "200";
		if (currentDevice != null) {
			try {
				res = currentDevice.getOption("resolution").getIntegerValue() + "";
			} catch (Exception e) {
				// nothing to do if it dows not work
			}
		}

		rv = rv.replace("${resolution}", res);

		return rv;
	}

	private void updateFileNamePreview() {
		StringBuilder sb = new StringBuilder();
		sb.append(substituteVars(outputDirectory.getText()));
		sb.append(File.separator);
		sb.append(substituteVars(fileNamePattern.getText()));
		sb.append(".");
		sb.append(fileFormat.getSelectionModel().getSelectedItem());
		filePathPreview.setText(sb.toString());
	}

}
