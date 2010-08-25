package org.sunspotworld;

import com.sun.spot.sensorboard.EDemoBoard;//{{{
import com.sun.spot.sensorboard.io.*;
import com.sun.spot.sensorboard.peripheral.*;
import java.io.*;
import javax.microedition.io.*;//}}}

public class LM60 {
	private IScalarInput pin;	
	
	private double Temporary = 0.0;
	public LM60(IScalarInput p) {
		pin = p;
	}

	public double getTemperature() {
		try {
			Temporary = ((3000.0 * pin.getValue() / pin.getRange()) - 424) / 6.25;
			return Temporary;
		} catch (IOException e) {
			return Temporary;
		}
	}
}
