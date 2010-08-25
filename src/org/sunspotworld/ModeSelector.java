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
public class ModeSelector implements ISwitchListener {
    ITriColorLED[] leds = EDemoBoard.getInstance().getLEDs();
    ISwitch sw1 = EDemoBoard.getInstance().getSwitches()[EDemoBoard.SW1];
    ISwitch sw2 = EDemoBoard.getInstance().getSwitches()[EDemoBoard.SW2];
    Timer timer = new Timer();
    LEDColor[] colors;
    int nModes;
    volatile int mode;
    volatile boolean on;
    volatile boolean selected;
    volatile boolean cancelled;

    ModeSelector(LEDColor[] colors) {
        this.colors = colors;
        nModes = colors.length;
        start();
    }

    ModeSelector(LEDColor color1) {
        this(new LEDColor[]{color1});
    }

    ModeSelector(LEDColor color1, LEDColor color2) {
        this(new LEDColor[]{color1, color2});
    }

    ModeSelector(LEDColor color1, LEDColor color2, LEDColor color3) {
        this(new LEDColor[]{color1, color2, color3});
    }

    ModeSelector(LEDColor color1, LEDColor color2, LEDColor color3, LEDColor color4) {
        this(new LEDColor[]{color1, color2, color3, color4});
    }

    ModeSelector(LEDColor color1, LEDColor color2, LEDColor color3, LEDColor color4,
            LEDColor color5) {
        this(new LEDColor[]{color1, color2, color3, color4, color5});
    }

    ModeSelector(LEDColor color1, LEDColor color2, LEDColor color3, LEDColor color4,
            LEDColor color5, LEDColor color6) {
        this(new LEDColor[]{color1, color2, color3, color4, color5, color6});
    }

    ModeSelector(LEDColor color1, LEDColor color2, LEDColor color3, LEDColor color4,
            LEDColor color5, LEDColor color6, LEDColor color7) {
        this(new LEDColor[]{color1, color2, color3, color4, color5, color6, color7});
    }

    ModeSelector(LEDColor color1, LEDColor color2, LEDColor color3, LEDColor color4,
            LEDColor color5, LEDColor color6, LEDColor color7, LEDColor color8) {
        this(new LEDColor[]{color1, color2, color3, color4, color5, color6, color7, color8});
    }

    private void start() {
        for (int i = 0; i < nModes; i++) {
            leds[i].setColor(colors[i]);
        }
        timer.schedule(new TimerTask() {
            public void run() {
                leds[mode].setOn(on ^= true);
            }
        }, 0, 250);
        sw1.addISwitchListener(this);
        sw2.addISwitchListener(this);
    }

    private void stop() {
        timer.cancel();
        sw1.removeISwitchListener(this);
        sw2.removeISwitchListener(this);
        on = false;
        leds[mode].setOff();
    }

    public boolean selected() {
        return selected;
    }

    public boolean cancelled() {
        return cancelled;
    }

    public int getMode() {
        return mode;
    }

    public void cancel() {
        stop();
        cancelled = true;
    }

    public void switchPressed(ISwitch sw) {
        if (sw.equals(sw1)) {
            leds[mode].setOff();
            mode = (mode + 1) % nModes;
        } else if (sw.equals(sw2)) {
            stop();
            selected = true;
        }
    }

    public void switchReleased(ISwitch sw) {
    }
}
