package org.sunspotworld;

import com.sun.spot.sensorboard.EDemoBoard;//{{{
import com.sun.spot.sensorboard.io.*;
import com.sun.spot.sensorboard.peripheral.*;
import java.io.*;
import javax.microedition.io.*;//}}}

public class CHSGSS {
	private IScalarInput pin;	
	
	private double Temporary = 0.0;
	private double value = 0.0;
	private double range = 0.0;
	public CHSGSS(IScalarInput p) {
		pin = p;
	}

	public double getShitudo() {
		try {
			value = pin.getValue();	
			range = pin.getRange();
			System.out.println("Value:" + value);
			System.out.println("Range:" + range);
			Temporary =  value * 3 / range * 100;
			return Temporary;
		} catch (IOException e) {
			return Temporary;
		}
	}
}
