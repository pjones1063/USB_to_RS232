package net.jones.serialModem.zmodem.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface FileAdapter {
	public boolean exists();
	public FileAdapter getChild(String name);
	public InputStream getInputStream() throws IOException;
	public String getName();
	public OutputStream getOutputStream() throws IOException;
	public OutputStream getOutputStream(boolean append) throws IOException;
	public boolean isDirectory();
	public long length();
	public String toString();
}
