module sanescanfx {

	requires java.desktop;
	requires javafx.base;
	requires javafx.controls;
	requires javafx.fxml;
	requires javafx.swing;
	requires org.kordamp.ikonli.core;
	requires org.kordamp.ikonli.fontawesome;
	requires org.kordamp.ikonli.javafx;
	requires org.kordamp.ikonli.material2;
	requires transitive javafx.graphics;
	requires transitive jfreesane;
    requires atlantafx.base;
    requires org.apache.logging.log4j.core;
    requires org.hometree.workspacefx;
    requires org.slf4j;

    opens de.heimbuchner.sanescanfx.main to javafx.fxml;

	exports de.heimbuchner.sanescanfx.main;
	exports de.heimbuchner.sanescanfx.controls;
	exports de.heimbuchner.sanescanfx.tests;
	exports de.heimbuchner.sanescanfx.options;

}