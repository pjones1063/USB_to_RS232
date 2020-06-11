package net.jones.serialModem.zmodem.xfer.util;

/**
 * Created by asirotinkin on 11.11.2014.
 */
public class CRC8 implements XCRC {
    @Override
    public long calcCRC(byte[] block) {
        byte checkSumma = 0;
        for (int i = 0; i < block.length; i++) {
            checkSumma += block[i];
        }
        return checkSumma;
    }

    @Override
    public int getCRCLength() {
        return 1;
    }

}
