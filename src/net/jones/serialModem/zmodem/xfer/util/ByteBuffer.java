package net.jones.serialModem.zmodem.xfer.util;



public class ByteBuffer implements Buffer {
	
	public static ByteBuffer allocate(int capacity){
		return new ByteBuffer(java.nio.ByteBuffer.allocate(capacity));
	}
		
	public static ByteBuffer allocateDirect(int capacity){
		return new ByteBuffer(java.nio.ByteBuffer.allocateDirect(capacity));
	}
	
	private java.nio.ByteBuffer _wrapped;

	protected ByteBuffer(java.nio.ByteBuffer b){
		_wrapped = b;
	}
	public ByteBuffer asByteBuffer() {
		return this;
	}

	public HexBuffer asHexBuffer() {
		return new HexBuffer(_wrapped);
	}

	public Buffer asReadOnlyBuffer() {
		return new ByteBuffer(_wrapped.asReadOnlyBuffer());
	}

	public Buffer compact() {
		_wrapped.compact();
		return this;
	}

    public Buffer duplicate() {
		return new ByteBuffer(_wrapped.duplicate());
	}
	
    public void flip() {
		_wrapped.flip();
	}
	
	public byte get() {
		return _wrapped.get();
	}
	
	public Buffer get(byte[] dst) {
    	return get(dst, 0, dst.length);
    }
	
	public Buffer get(byte[] dst, int offset, int len) {
    	for(;offset<len;offset++)
    		dst[offset] = get();
    	
    	return this;
    }

	public byte get(int index) {
		return  _wrapped.get(index);
	}
	
    public Buffer get(int index,byte[] dst) {
    	return get(index,dst, 0, dst.length);
    }
	
    public Buffer get(int index,byte[] dst, int offset, int len) {
    	for(;offset<len;offset++)
    		dst[offset] = get(index++);
    	
    	return this;
    }

	public char getChar() {
		return (char)get();
	}
	
	public char getChar(int index) {
		return (char)get(index);
	}
	public int getInt() {
		return Arrays.toInt(new byte[]{get(),get(),get(),get()}, Arrays.Endianness.Little);
	}

	public int getInt(int index) {
		return Arrays.toInt(new byte[]{get(index),get(index+1),get(index+2),get(index+3)}, Arrays.Endianness.Little);
	}

	public long getLong() {
		return Arrays.toLong(new byte[]{get(),get(),get(),get(),get(),get(),get(),get()},  Arrays.Endianness.Little);
	}

	public long getLong(int index) {
		return Arrays.toLong(new byte[]{get(index),get(index+1),get(index+2),get(index+3),get(index+4),get(index+5),get(index+6),get(index+7)},  Arrays.Endianness.Little);
	}

	public short getShort() {
		return Arrays.toShort(new byte[]{get(),get()}, Arrays.Endianness.Little);
	}

	public short getShort(int index) {
		return Arrays.toShort(new byte[]{get(index),get(index+1)}, Arrays.Endianness.Little);
	}

	public boolean hasRemaining() {
		return remaining()>0;
	}

	public boolean isDirect() {
		return _wrapped.isDirect();
	}

	public boolean isReadOnly() {
		return _wrapped.isReadOnly();
	}

	public Buffer put(byte b) {
		_wrapped.put(b);
		return this;
	}

	public Buffer put(byte[] dst) {
		return put(dst,0,dst.length);
	}

	public Buffer put(byte[] dst,int offset,int len) {
		for(;offset<len;offset++)
			put(dst[offset]);
				
		return this;
	}

	public Buffer put(int index, byte b) {
		_wrapped.put(index,b);
		return this;
	}

	public Buffer put(int index,byte[] dst) {
		return put(index,dst,0,dst.length);
	}

	public Buffer put(int index,byte[] dst,int offset, int len) {
		for(;offset<len;offset++)
			put(index++,dst[offset]);
				
		return this;
	}

	public Buffer putChar(char value) {
		return put((byte)value);
	}

	public Buffer putChar(int index, char value) {
		return put(index,(byte)value);
	}

	public Buffer putInt(int value) {
		return put(Arrays.fromInt(value,  Arrays.Endianness.Little));
	}

	public Buffer putInt(int index, int value) {
		return put(index,Arrays.fromInt(value,  Arrays.Endianness.Little));
	}

	public Buffer putLong(int index, long value) {
		return put(index,Arrays.fromLong(value,  Arrays.Endianness.Little));
	}

	public Buffer putLong(long value) {
		return put(Arrays.fromLong(value,  Arrays.Endianness.Little));
	}

	public Buffer putShort(int index, short value) {
		return put(index,Arrays.fromShort(value, Arrays.Endianness.Little));
	}

	public Buffer putShort(short value) {
		return put(Arrays.fromShort(value, Arrays.Endianness.Little));
	}

	public int remaining() {
		return _wrapped.remaining();
	}

	public Buffer slice() {
		return new ByteBuffer(_wrapped.slice());
	}
	
}


