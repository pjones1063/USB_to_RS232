package net.jones.serialModem.zmodem.xfer.zm.packet;


import net.jones.serialModem.zmodem.xfer.util.Buffer;
import net.jones.serialModem.zmodem.xfer.util.ByteBuffer;
import net.jones.serialModem.zmodem.xfer.zm.util.ZMPacket;

public class Finish extends ZMPacket {

	@Override
	public Buffer marshall() {
		ByteBuffer buff = ByteBuffer.allocateDirect(16);
		
		for(int i=0;i<2;i++)
			buff.put((byte) 'O');
		
		buff.flip();
		
		return buff;
	}
	
	@Override
	public String toString() {
		return "Finish: OO";
	}

}
