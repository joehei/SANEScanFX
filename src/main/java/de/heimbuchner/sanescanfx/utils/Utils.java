package de.heimbuchner.sanescanfx.utils;

import java.io.File;

public class Utils {

	public static String getUserDataDirectory(String applicationName) {
		return System.getProperty("user.home") + File.separator + "." + applicationName + File.separator
				+ applicationName + File.separator;
	}

	private Utils() {
	}

}
