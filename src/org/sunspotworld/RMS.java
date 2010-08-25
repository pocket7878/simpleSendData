package org.sunspotworld;

import com.sun.squawk.util.ByteArrayInputStreamWithSetBytes;
import com.sun.squawk.util.ByteArrayOutputStreamWithGetBytes;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreException;
import javax.microedition.rms.RecordStoreNotOpenException;

/**
 *
 * @author yamaguch
 */
public class RMS {
    private RecordStore rs;
    private DataInputStream in;
    private ByteArrayInputStreamWithSetBytes baIn;
    private DataOutputStream out;
    private ByteArrayOutputStreamWithGetBytes baOut;
    private int index;

    private RMS(String name, boolean createIfNecessary) throws Exception {
        rs = RecordStore.openRecordStore(name, createIfNecessary);
    }

    public static RMS getInstance(String name, boolean createIfNecessary) {
        try {
            return new RMS(name, createIfNecessary);
        } catch (Exception e) {
            return null;
        }
    }

    public static String[] list() {
        return RecordStore.listRecordStores();
    }

    public static void delete(String name) {
        try {
            RecordStore.deleteRecordStore(name);
        } catch (RecordStoreException e) {
        }
    }

    public int getVersion() {
        try {
            return rs.getVersion();
        } catch (RecordStoreNotOpenException e) {
            return 0;
        }
    }

    public int getSize() {
        try {
            return rs.getSize();
        } catch (RecordStoreNotOpenException e) {
            return 0;
        }
    }

    public int getSizeAvailable() {
        try {
            return rs.getSizeAvailable();
        } catch (RecordStoreNotOpenException e) {
            return 0;
        }
    }

    public String getStat() {
        return "version: " + getVersion() + ", used: " + getSize() + ", available: " + getSizeAvailable();
    }

    void prepareInput() throws Exception {
        if (index == 0 && rs.getNumRecords() > 0) {
            index = 1;
        }
        baIn = new ByteArrayInputStreamWithSetBytes(rs.getRecord(index));
        in = new DataInputStream(baIn);
    }

    void prepareOutput() {
        if (out == null) {
            baOut = new ByteArrayOutputStreamWithGetBytes();
            out = new DataOutputStream(baOut);
        }
    }

    public boolean hasMoreRecords() {
        try {
            return rs.getNumRecords() > 0 && index <= rs.getNumRecords();
        } catch (RecordStoreNotOpenException e) {
            return false;
        }
    }

    public String readString() throws Exception {
        if (in == null) {
            prepareInput();
        }
        try {
            return in.readUTF();
        } catch (EOFException e) {
            if (index < rs.getNumRecords()) {
                index++;
                in = null;
                return readString();
            }
            throw e;
        }
    }

    public long readLong() throws Exception {
        if (in == null) {
            prepareInput();
        }
        try {
            return in.readLong();
        } catch (EOFException e) {
            if (index < rs.getNumRecords()) {
                index++;
                in = null;
                return readLong();
            }
            throw e;
        }
    }

    public double readDouble() throws Exception {
        if (in == null) {
            prepareInput();
        }
        try {
            return in.readDouble();
        } catch (EOFException e) {
            if (index < rs.getNumRecords()) {
                index++;
                in = null;
                return readDouble();
            }
            throw e;
        }
    }

    public void skip() {
        if (in != null) {
            try {
                in.close();
            } catch (IOException e) {
            } finally {
                index++;
                in = null;
            }
        }
    }

    public RMS write(String s) throws Exception {
        return writeString(s);
    }

    public RMS write(long n) throws Exception {
        return writeLong(n);
    }

    public RMS write(double d) throws Exception {
        return writeDouble(d);
    }

    protected RMS writeString(String s) throws Exception {
        if (out == null) {
            prepareOutput();
        }

        out.writeUTF(s);

        return this;
    }

    protected RMS writeLong(long n) throws Exception {
        if (out == null) {
            prepareOutput();
        }

        out.writeLong(n);

        return this;
    }

    protected RMS writeDouble(double v) throws Exception {
        if (out == null) {
            prepareOutput();
        }

        out.writeDouble(v);

        return this;
    }

    public RMS flush() throws Exception {
        byte[] data = baOut.getBytes();
        rs.addRecord(data, 0, data.length);
        try {
            out.close();
        } catch (IOException e) {
        } finally {
            out = null;
        }

        return this;
    }
}
