package de.heimbuchner.sanescanfx.controls;

import java.util.Arrays;

import de.heimbuchner.sanescanfx.main.JavaFXUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Point2D;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Path;
import javafx.stage.Popup;

public class TextfieldCompletion extends TextField {

	public TextfieldCompletion(String text) {
		super(text);
		init();
	}
	
	public TextfieldCompletion() {
		this("");
	}

	private final Popup popup = new Popup();

	private final TextField searchField = new TextField();

	private ObservableList<String> suggestionListItems;

	public ObservableList<String> getSuggestionListItems() {
		return suggestionListItems;
	}

	public void setSuggestionListItems(ObservableList<String> suggestionListItems) {
		this.suggestionListItems = suggestionListItems;
	}

	private void init() {

		TextfieldCompletion textfieldCompletion = this;

		popup.setAutoHide(true);
		VBox vBox = new VBox();

		suggestionListItems = FXCollections.observableArrayList();
		suggestionListItems.setAll(Arrays.asList("Option 1", "Option 2", "Option 3"));
		FilteredList<String> filterList = new FilteredList<>(suggestionListItems);

		ListView<String> suggestionList = new ListView<>();
		suggestionList.setItems(filterList);
		suggestionList.getSelectionModel().selectFirst();
		suggestionList.getSelectionModel().selectedItemProperty().addListener((obs, ov, nv) -> {
			if (nv == null) {
				suggestionList.getSelectionModel().selectFirst();
			}
		});
		suggestionList.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
			if (event.getCode().equals(KeyCode.ENTER)) {
				insertSuggestedText(textfieldCompletion, suggestionList);
			}
			if (event.getCode().equals(KeyCode.UP) && //
			suggestionList.getSelectionModel().getSelectedItem() != null && //
			suggestionList.getSelectionModel().getSelectedItem().equals(suggestionList.getItems().get(0))) {
				searchField.requestFocus();
			}
		});

		searchField.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
			if (event.getCode().equals(KeyCode.ENTER)) {
				insertSuggestedText(textfieldCompletion, suggestionList);
			}
			if (event.getCode().equals(KeyCode.DOWN) || event.getCode().equals(KeyCode.UP)) {
				suggestionList.requestFocus();
			}
		});
		searchField.textProperty()
				.addListener((obs, ov, nv) -> filterList.setPredicate(t -> t.toLowerCase().contains(nv.toLowerCase())));

		vBox.getChildren().add(searchField);
		vBox.getChildren().add(suggestionList);
		popup.getContent().add(vBox);

		addEventHandler(KeyEvent.KEY_PRESSED, event -> {
			if (event.getCode() == KeyCode.SPACE && event.isControlDown()) {
				Path caret = JavaFXUtils.findCaret(textfieldCompletion);
				Point2D screenLoc = JavaFXUtils.findScreenLocation(caret);
				popup.show(textfieldCompletion, screenLoc.getX(), screenLoc.getY() + 20);
			}
		});
	}

	private void insertSuggestedText(TextfieldCompletion textfieldCompletion, ListView<String> suggestionList) {
		popup.hide();
		String toInsert = suggestionList.getSelectionModel().getSelectedItem();

		if (toInsert == null) {
			return;
		}

		String part1 = textfieldCompletion.getText().substring(0, textfieldCompletion.getCaretPosition());
		String part2 = textfieldCompletion.getText().substring(textfieldCompletion.getCaretPosition(),
				textfieldCompletion.getText().length());
		int newCartPos = (part1 + toInsert).length();
		textfieldCompletion.setText(part1 + toInsert + part2);
		textfieldCompletion.positionCaret(newCartPos);
	}

}
