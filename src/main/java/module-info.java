module sanescanfx {

	requires javafx.controls;
	requires javafx.fxml;
	requires transitive javafx.graphics;

	requires org.kordamp.ikonli.core;
	requires org.kordamp.ikonli.javafx;
	requires org.kordamp.ikonli.material2;
	requires org.kordamp.ikonli.fontawesome;

	requires transitive jfreesane;
	requires java.desktop;
	requires javafx.swing;

	opens de.heimbuchner.sanescanfx.main to javafx.fxml;

	exports de.heimbuchner.sanescanfx.main;
	exports de.heimbuchner.sanescanfx.controls;

}