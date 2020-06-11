package net.jones.serialModem.zmodem.xfer.util;

/**
 * Created by Muzeffer on 2016/6/30.
 */
public interface XCRC {
    long calcCRC(byte[] block);
    int getCRCLength();
}
