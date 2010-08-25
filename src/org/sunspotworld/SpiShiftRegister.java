package org.sunspotworld;

import com.sun.spot.sensorboard.io.IInputPin;
import com.sun.spot.sensorboard.io.IOutputPin;

/**
 *
 * @author yamaguch
 */
public class SpiShiftRegister {
    IOutputPin mosi, sck, ss;
    IInputPin miso;

    public SpiShiftRegister(IOutputPin mosi, IInputPin miso, IOutputPin sck, IOutputPin ss) {
        this.mosi = mosi;
        this.miso = miso;
        this.sck = sck;
        this.ss = ss;
    }

    public byte transfer(byte value) {
        ss.setLow();
        byte data = shift(value);
        ss.setHigh();
        return data;
    }

    public void transfer(byte[] value) {
        ss.setLow();
        for (int i = 0; i < value.length; i++) {
            value[i] = shift(value[i]);
        }
        ss.setHigh();
    }

    byte shift(byte value) {
        byte value1 = 0;
        for (int bit = 128; bit != 0; bit >>= 1) {
            sck.setLow();
            mosi.setHigh((value & bit) != 0);
            sck.setHigh();
            value1 |= miso.isHigh() ? bit : 0;
        }
        return value1;
    }
}
