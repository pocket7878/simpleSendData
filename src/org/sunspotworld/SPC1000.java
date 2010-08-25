package org.sunspotworld;

import com.sun.spot.sensorboard.io.IIOPin;
import com.sun.spot.sensorboard.io.IInputPin;
import com.sun.spot.sensorboard.io.IOutputPin;

/**
 *
 * @author yamaguch
 */
public class SPC1000 {
    SpiShiftRegister spi;
    static final byte REVID = 0x00;
    static final byte OPERATION = 0x03;
    static final byte RSTR = 0x06;
    static final byte DATARD8 = 0x1f;
    static final byte DATARD16 = 0x20;
    static final byte TEMPOUT = 0x21;

    SPC1000(IOutputPin mosi, IInputPin miso, IOutputPin sck, IOutputPin ss) {
        spi = new SpiShiftRegister(mosi, miso, sck, ss);
    }

    SPC1000(IIOPin mosi, IIOPin miso, IIOPin sck, IIOPin ss) {
        mosi.setAsOutput(true);
        miso.setAsOutput(false);
        sck.setAsOutput(true);
        ss.setAsOutput(true);
        ss.setHigh();
        mosi.setLow();
        sck.setLow();

        spi = new SpiShiftRegister(mosi, miso, sck, ss);
    }

    void writeRegister(byte reg, byte value) {
        byte[] buf = {(byte) ((reg << 2) | 2), value};
        spi.transfer(buf);
    }

    int readRegister(byte reg) {
        byte[] buf = (reg < 0x20) ? new byte[]{(byte) (reg << 2), 0} : new byte[]{(byte) (reg << 2), 0, 0};
        spi.transfer(buf);
        return reg < 0x20 ? (buf[1] & 255) : (buf[1] & 255) << 8 | (buf[2] & 255);
    }

    void reset() throws InterruptedException {
        writeRegister(RSTR, (byte) 0x01);
        Thread.sleep(150);
    }

    void start() throws InterruptedException {
        reset();
        writeRegister(OPERATION, (byte) 0x0A); // start aquisition (high resolution mode, 1.8Hz)
    }

    void stop() {
        writeRegister(OPERATION, (byte) 0x00);
    }

    int getRevID() {
        return readRegister(REVID);
    }

    double getPressure() {
        return (((readRegister(DATARD8) & 7)) << 16 | readRegister(DATARD16)) / 400.0;
    }

    double getTemperature() {
        return readRegister(TEMPOUT) / 20.0;
    }
}
