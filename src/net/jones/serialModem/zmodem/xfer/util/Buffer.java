package net.jones.serialModem.zmodem.xfer.util;

public interface Buffer {
	public ByteBuffer asByteBuffer();
    public HexBuffer asHexBuffer();
    public void flip();
	public byte get() ;
	public Buffer get(byte[] dst) ;
	public Buffer get(byte[] dst, int offset, int len) ;
	public byte get(int index) ;
    public Buffer get(int index, byte[] dst) ;
    public Buffer get(int index, byte[] dst, int offset, int len);
	public boolean hasRemaining();
	public Buffer put(byte b) ;
	public Buffer put(byte[] dst) ;
	public Buffer put(byte[] dst, int offset, int len) ;
	public Buffer put(int index, byte b) ;
	public Buffer put(int index, byte[] dst) ;
	public Buffer put(int index, byte[] dst, int offset, int len) ;
	public int remaining();

}
