package de.heimbuchner.sanescanfx.main;

import au.com.southsky.jfreesane.SaneDevice;

public class SaneDeviceFX {

	private SaneDevice saneDevice;

	public SaneDeviceFX(SaneDevice saneDevice) {
		this.saneDevice = saneDevice;
	}

	public SaneDevice getSaneDevice() {
		return saneDevice;
	}

	public void setSaneDevice(SaneDevice saneDevice) {
		this.saneDevice = saneDevice;
	}

	@Override
	public String toString() {
		return saneDevice.getName();
	}

}
