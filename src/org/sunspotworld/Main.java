// vim: set foldmethod=marker:
package org.sunspotworld;

import com.sun.spot.sensorboard.EDemoBoard;//{{{
import com.sun.spot.util.Utils;
import com.sun.spot.sensorboard.io.*;
import com.sun.spot.sensorboard.peripheral.*;
import com.sun.spot.io.j2me.radiogram.*;//}}}
import javax.microedition.io.*;//{{{
import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;//}}}
import java.io.*;//{{{
import java.util.Timer;
import java.util.TimerTask;//}}}


public class Main extends MIDlet {

	//Camera Controller Timer and TimerTask
	private Timer t;
	private Timer tm;

	private static final int HOST_PORT = 67;
	private static final int SAMPLE_PERIOD = 10 * 10 * 5;  // in milliseconds

	private static final String DB = "DataRecordStore";
	static final int RECORD = 0;//{{{
	static final int RADIO = 1;
	static final int PRINT = 2;
	static final int DELETE = 3;//}}}

	static final int SECONDS = 1000;
	static final int MINUTES = 1000 * 60;
	private boolean already_overp = false;
	private int should_servop = 0;
	private boolean already_taskedp = false;
	private ModeSelector modeSelector;
	private Servo servo;
	private Gauge gauge;

	private RadiogramConnection rCon;//{{{
	private Datagram dg;//}}}

	private IAccelerometer3D accelSensor;//{{{
	private ITemperatureInput tempSensor;
	private ILightSensor lightSensor;
	private LM60 lm60;
	private CHSGSS chsgss;
	private SPC1000 spc1000;//}}}

	private ITriColorLED[] led = EDemoBoard.getInstance().getLEDs();

	protected IIOPin[] ioPins = EDemoBoard.getInstance().getIOPins();//{{{
	protected IIOPin d0 = ioPins[EDemoBoard.D0];
	protected IIOPin d1 = ioPins[EDemoBoard.D1];
	protected IIOPin d2 = ioPins[EDemoBoard.D2];
	protected IIOPin d3 = ioPins[EDemoBoard.D3];
	protected IIOPin d4 = ioPins[EDemoBoard.D4];//}}}

	protected IOutputPin[] outputPins = EDemoBoard.getInstance().getOutputPins();//{{{
	protected IOutputPin h0 = outputPins[EDemoBoard.H0];
	protected IOutputPin h1 = outputPins[EDemoBoard.H1];
	protected IOutputPin h2 = outputPins[EDemoBoard.H2];
	protected IOutputPin h3 = outputPins[EDemoBoard.H3];//}}}

	protected IScalarInput[] scalarInputs = EDemoBoard.getInstance().getScalarInputs();//{{{
	protected IScalarInput a4 = scalarInputs[EDemoBoard.A4];
	protected IScalarInput a1 = scalarInputs[EDemoBoard.A1];//}}}

	private	long now = 0L;//{{{
//	private	double x = 0.0;
//	private	double y = 0.0;
//	private	double z = 0.0;
	private double accel = 0.0;
	private	double insideTemperature;
	private	double outsideTemperature;
	private double LM60Temperature;
	private	double bright = 0.0;
	private	double pressure = 0.0;
	private double wet = 0.0;
	private double disconfort = 0.0;
	private double pressChange = 0.0;//}}}

	private RMS rms;

	class CamCtrlTask extends TimerTask {//{{{
		public void run() {
			pulseH2();
		}
	}//}}}

	class ServoCtrlTask extends TimerTask {//{{{
		public void run() {
			turnServo();
		}
	}//}}}

	protected void startApp() throws MIDletStateChangeException {//{{{
		modeSelector = new ModeSelector(LEDColor.GREEN, LEDColor.ORANGE,LEDColor.CYAN, LEDColor.RED);
		servo = new Servo(h3);
		try {
			rCon = (RadiogramConnection) Connector.open("radiogram://broadcast:" + HOST_PORT);
			dg = rCon.newDatagram(rCon.getMaximumLength());  // only sending 12 bytes of data
		} catch (Exception e) {
			System.err.println("Caught " + e + " in connection initialization.");
			System.exit(1);
		}

		while(!modeSelector.selected()) {
			Utils.sleep(100);
		}

		if(modeSelector.getMode() != PRINT && modeSelector.getMode() != DELETE) {
			gauge = new Gauge(LEDColor.BLUE,8);
			while(!gauge.selected()) {
				Utils.sleep(100);
			}
			System.out.println(gauge.getVar());
			shallowSleep(gauge.getVar() * MINUTES);
		}

		spc1000 = new SPC1000(d0, d2, d4, d1); //mosi, miso, sck, ss
		try {
			spc1000.start();
		} catch(InterruptedException e) {
		}
		System.out.println("REVID:" + spc1000.getRevID());

		switch (modeSelector.getMode()) {
			case RECORD://{{{
				rms = RMS.getInstance(DB, true);
				accelSensor = EDemoBoard.getInstance().getAccelerometer();
				((LIS3L02AQAccelerometer) accelSensor).setScale(LIS3L02AQAccelerometer.SCALE_6G);
				tempSensor = EDemoBoard.getInstance().getADCTemperature();
				lightSensor = EDemoBoard.getInstance().getLightSensor();
				lm60 = new LM60(a1);
				chsgss = new CHSGSS(a4);
				t = new Timer();
				t.schedule(new CamCtrlTask(), 30000, 30000);
//				spc1000 = new SPC1000(d0, d2, d4, d1,1013); //mosi, miso, sck, ss
//				while(pressChange < 0.5) {
//					pressure = spc1000.getPressure();
//					now = System.currentTimeMillis();
//					Utils.sleep(500);
//					pressChange = spc1000.getPressure() - pressure;
//					System.out.println(pressChange + ":::\n");
//				}
				break;//}}}
			case RADIO://{{{
				accelSensor = EDemoBoard.getInstance().getAccelerometer();
				((LIS3L02AQAccelerometer) accelSensor).setScale(LIS3L02AQAccelerometer.SCALE_6G);
				tempSensor = EDemoBoard.getInstance().getADCTemperature();
				lightSensor = EDemoBoard.getInstance().getLightSensor();
				lm60 = new LM60(a1);
				chsgss = new CHSGSS(a4);
				t = new Timer();
				t.schedule(new CamCtrlTask(), 30000, 30000);
//				spc1000 = new SPC1000(d0, d2, d4, d1,1013); //mosi, miso, sck, ss
//				try {
//					spc1000.start();
//				} catch(InterruptedException e) {
//				}
				break;//}}}
			case PRINT://{{{
				rms = RMS.getInstance(DB, false);
				break;//}}}
//			case DEBUG://{{{
//				d4.setAsOutput(true);
//				d4.setLow();
//				break;//}}}
		}

		// TODO Add some functions to wait falling sunspot.
		if(modeSelector.getMode() != DELETE && modeSelector.getMode() != PRINT) {
			tm = new Timer();
			ServoCtrlTask svTask = new ServoCtrlTask();
			long cur = 0L;
			while(should_servop == 0) {
				try {
					accel = accelSensor.getAccel();
				}catch(IOException e) {
				}
				if(accel <= 0.4 && already_overp) {
					//Must Start now
					should_servop = 1;	
					if(already_taskedp == true) {
						svTask.cancel();
					}
					break;
				}
				else if(((System.currentTimeMillis() - cur) >= 8000) && already_taskedp) {
					should_servop = 2;	
					break;
				} 
				else if(accel >= 6) {
					//Timer Start
					already_overp = true;
					if(already_taskedp == false) {
						tm.schedule(svTask, 8000);
						cur = System.currentTimeMillis();
						already_taskedp = true;
					}
				}
			}

			if(should_servop == 1) {
				servo.setValue(1500);
				for(int i = 0; i < 2; i++) {
					servo.setPosition(2.0f);
					Utils.sleep(1000);
					servo.setPosition(0.0f);
					Utils.sleep(1000);
				}
			}
		}

		// Main Loop
		while(true) {
			switch (modeSelector.getMode()) {
				case RECORD://{{{
					// Flash an LED to indicate a sampling event
					led[7].setRGB(0, 255, 0);
					led[7].setOn();
					// Get the current time and sensor reading
					now = System.currentTimeMillis();
					try {
//						x = accelSensor.getAccelX();
//						y = accelSensor.getAccelY();
//						z = accelSensor.getAccelZ();
						accel = accelSensor.getAccel();
						bright = lightSensor.getValue();
						insideTemperature = tempSensor.getCelsius();
						outsideTemperature = spc1000.getTemperature();
						LM60Temperature = lm60.getTemperature();
						pressure = spc1000.getPressure();
						wet = chsgss.getShitudo();
//						disconfort = 1.8 * LM60Temperature - 0.55 * (1 - wet / 100) * (1.8 * LM60Temperature - 26) + 32;
					} catch (IOException p) {
						System.out.println("Can't get data from sensor");
						notifyDestroyed(); // cause the MIDlet to exit
					}
					try {
						rms.write(now);
//						rms.write(x);
//						rms.write(y);
//						rms.write(z);
						rms.write(accel);
						rms.write(bright);
						rms.write(insideTemperature);
						rms.write(outsideTemperature);
						rms.write(LM60Temperature);
						rms.write(pressure);
						rms.write(wet);
//						rms.write(disconfort);
						rms.flush();
					} catch (Exception e) {
						e.printStackTrace();
						notifyDestroyed(); // cause the MIDlet to exit
					}
					led[7].setOff();
					Utils.sleep(SAMPLE_PERIOD - (System.currentTimeMillis() - now));
					break;//}}}
				case RADIO://{{{
					try {
						now = System.currentTimeMillis();

//						x = accelSensor.getAccelX();
//						y = accelSensor.getAccelY();
//						z = accelSensor.getAccelZ();

						accel = accelSensor.getAccel();
						bright = lightSensor.getValue();
						insideTemperature = tempSensor.getCelsius();
						outsideTemperature = spc1000.getTemperature();
						LM60Temperature = lm60.getTemperature();
						pressure = spc1000.getPressure();
						wet = chsgss.getShitudo();
						disconfort = 1.8 * LM60Temperature - 0.55 * (1 - wet / 100) * (1.8 * LM60Temperature - 26) + 32;
						// Flash an LED to inidicate a sampling Event
						led[7].setColor(LEDColor.ORANGE);
						led[7].setOn();

						// Package the time and sensor reading into a radio datagram and send it
						dg.reset();
						dg.writeLong(now);
//						dg.writeDouble(x);
//						dg.writeDouble(y);
//						dg.writeDouble(z);
						dg.writeDouble(accel);
						dg.writeDouble(bright);
						dg.writeDouble(insideTemperature);
						dg.writeDouble(outsideTemperature);
						dg.writeDouble(LM60Temperature);
						dg.writeDouble(pressure);
						dg.writeDouble(wet);
						dg.writeDouble(disconfort);
						rCon.send(dg);

//						System.out.println("Height:" + spc1000.getHeight() + "\n");
						System.out.println("Pressure" + pressure + "\n");
						System.out.println("Shitudo:" + chsgss.getShitudo() + "\n");
						led[7].setOff();
						Utils.sleep(SAMPLE_PERIOD - (System.currentTimeMillis() - now));
						break;
					} catch(Exception e) {
						System.err.println("Caught" + e + " while collecting/sending sensor sample.");
						notifyDestroyed(); // cause the MIDlet to exit
					}//}}}
				case PRINT://{{{
					if (rms.hasMoreRecords()) {
						// Flash an LED to indicate a sampling event
						led[7].setRGB(0, 0, 255);
						led[7].setOn();
						try {
							now = rms.readLong();
//							x = rms.readDouble();
//							y = rms.readDouble();
//							z = rms.readDouble();
							accel = rms.readDouble();
							bright = rms.readDouble();
							insideTemperature = rms.readDouble();
							outsideTemperature = rms.readDouble();
							LM60Temperature = rms.readDouble();
							pressure = rms.readDouble();
							wet = rms.readDouble();
							disconfort = 1.8 * LM60Temperature - 0.55 * (1 - wet / 100) * (1.8 * LM60Temperature - 26) + 32;
						} catch (Exception e) {
							e.printStackTrace();
						}
						// Send Data
						try {
							dg.reset();
							dg.writeLong(now);
//							dg.writeDouble(x);
//							dg.writeDouble(y);
//							dg.writeDouble(z);
							dg.writeDouble(accel);
							dg.writeDouble(bright);
							dg.writeDouble(insideTemperature);
							dg.writeDouble(outsideTemperature);
							dg.writeDouble(LM60Temperature);
							dg.writeDouble(pressure);
							dg.writeDouble(wet);
							dg.writeDouble(disconfort);
							rCon.send(dg);
						} catch(Exception e) {
							System.err.println("Caught" + e + " while collecting/sending sensor sample.");
							notifyDestroyed(); // cause the MIDlet to exit
						}

						System.out.println(DateUtils.toDateString(now) + ":");
//						System.out.println("AccelX: " + x);
//						System.out.println("AccelY: " + y);
//						System.out.println("AccelZ: " + z);
						System.out.println("AccelTotal:" + accel);
						System.out.println("Brightness: " + bright);
						System.out.println("Inside Temperature: " + insideTemperature);
						System.out.println("OutsideTemperature: " + outsideTemperature);
						System.out.println("LM60Temperature: " + LM60Temperature);
						System.out.println("Pressure: " + pressure);
						System.out.println("Wet: " + wet);
						System.out.println("Disconfort: " + disconfort);
						rms.skip();
						led[7].setOff();
						break;
					} else {
						blinkAllLED(LEDColor.BLUE);
						notifyDestroyed(); // cause the MIDlet to exit
					}//}}}
				case DELETE://{{{
					RMS.delete(DB);
					blinkAllLED(LEDColor.RED);
					notifyDestroyed(); // cause the MIDlet to exit//}}}
//				case DEBUG://{{{
//					heater(true, 10);
//					notifyDestroyed();//}}}
			}
		}
	}//}}}

//	private void heater(boolean flag, int second) {//{{{
//		if(flag == true) {
//			d4.setAsOutput(true);
//			d4.setHigh(true);
//			//wait
//			shallowSleep(second);
//			blinkAllLED(LEDColor.MAUVE);
//			d4.setLow();
//			d4.setAsOutput(false);
//		}
//	}//}}}

	private void blinkAllLED(LEDColor color) {//{{{
		for(int i = 0; i <= 7; i++) {
			led[i].setColor(color);
		}

		for(int j = 0; j < 4; j++) {
			for(int i = 0; i <= 7; i++) {
				led[i].setOn();
			}
			Utils.sleep(600);
			for(int i = 0; i <= 7; i++) {
				led[i].setOff();
			}
			Utils.sleep(600);
		}
	}//}}}

	private void shallowSleep(long n) {//{{{
		while (n > 0) {
			Utils.sleep((n > 2 * SECONDS) ? (2 * SECONDS) : n);
			n -= 2 * SECONDS;
		}
	}//}}}

	protected void pauseApp() {//{{{
		// This will never be called by the Squawk VM
	}//}}}

	private void pulseH2() {//{{{
		h2.setHigh();
		Utils.sleep(100);
		h2.setLow();
		Utils.sleep(1300);
		h2.setHigh();
		Utils.sleep(100);
		h2.setLow();
		System.out.println("PulseH2 Done.");
	}//}}}
	
	private void turnServo() {//{{{
		servo.setValue(1500);
		for(int i = 0; i < 2; i++) {
			servo.setPosition(2.0f);
			Utils.sleep(1000);
			servo.setPosition(0.0f);
			Utils.sleep(1000);
		}
	}//}}}

	protected void destroyApp(boolean unconditional) throws MIDletStateChangeException {//{{{
		if (unconditional) {
			try {
				cleanup();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			throw new MIDletStateChangeException();
		}
	}//}}}

	protected void cleanup() throws Exception {//{{{
	}//}}}
}
