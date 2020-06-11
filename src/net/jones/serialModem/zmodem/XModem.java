package net.jones.serialModem.zmodem;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;

import net.jones.serialModem.zmodem.xfer.zm.util.Modem;


public class XModem {
    private Modem modem;

    public XModem(InputStream inputStream, OutputStream outputStream) {
        this.modem = new Modem(inputStream, outputStream);
    }

    public void receive(Path file) throws IOException {
        modem.receive(file, false);
    }

    public void receive(Path file, boolean useCRC16) throws IOException {
        modem.receive(file, useCRC16);
    }
    
    public void send(Path file) throws IOException, InterruptedException {
        modem.send(file, false);
    }
    
    public void send(Path file,boolean useBlock1K) throws IOException, InterruptedException {
        modem.send(file, useBlock1K);
    }
}
