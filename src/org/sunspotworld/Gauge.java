package org.sunspotworld;

import com.sun.spot.sensorboard.EDemoBoard;
import com.sun.spot.sensorboard.peripheral.ISwitch;
import com.sun.spot.sensorboard.peripheral.ISwitchListener;
import com.sun.spot.sensorboard.peripheral.ITriColorLED;
import com.sun.spot.sensorboard.peripheral.LEDColor;
import java.util.Timer;
import java.util.TimerTask;

/**
 *
 * @author yamaguch
 */
public class Gauge implements ISwitchListener {
    ITriColorLED[] leds = EDemoBoard.getInstance().getLEDs();
    ISwitch sw1 = EDemoBoard.getInstance().getSwitches()[EDemoBoard.SW1];
    ISwitch sw2 = EDemoBoard.getInstance().getSwitches()[EDemoBoard.SW2];
    LEDColor color;
    int var;
    int Maxvalue;
    volatile int mode;
    volatile boolean on;
    volatile boolean selected;
    volatile boolean cancelled;

    Gauge(LEDColor colors,int mvar) {
        this.color = colors;
	var = 0;
	if(Maxvalue <= 7) { 
		Maxvalue = mvar;
	} else {
		Maxvalue = 7;
	}
        start();
    }

    private void start() {
        for (int i = 0; i < Maxvalue; i++) {
            leds[i].setColor(color);
        }
        for (int i = 0; i < var; i++) {
            leds[i].setOn();
        }
        sw1.addISwitchListener(this);
        sw2.addISwitchListener(this);
    }

    private void stop() {
        sw1.removeISwitchListener(this);
        sw2.removeISwitchListener(this);
	for(int i = 0; i < Maxvalue; i++) {
		leds[i].setOff();
	}
    }

    public boolean selected() {
        return selected;
    }

    public boolean cancelled() {
        return cancelled;
    }

    public int getVar() {
        return var;
    }

    public void cancel() {
        stop();
        cancelled = true;
    }

    public void switchPressed(ISwitch sw) {
        if (sw.equals(sw1)) {
		var++;
		if(var > Maxvalue) {
			var = 0;
		}
		System.out.println("Current Var is:" + var);
		for(int i = 0; i < Maxvalue; i++) {
			leds[i].setOff();
		}
		for(int i = 0; i < var; i++) {
			leds[i].setOn();
		}
        } else if (sw.equals(sw2)) {
            stop();
            selected = true;
        }
    }

    public void switchReleased(ISwitch sw) {
    }
}
