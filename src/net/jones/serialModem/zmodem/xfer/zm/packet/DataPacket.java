package net.jones.serialModem.zmodem.xfer.zm.packet;


import net.jones.serialModem.zmodem.xfer.util.*;
import net.jones.serialModem.zmodem.xfer.zm.util.ZDLEEncoder;
import net.jones.serialModem.zmodem.xfer.zm.util.ZMPacket;
import net.jones.serialModem.zmodem.xfer.zm.util.ZModemCharacter;

public class DataPacket extends ZMPacket {

	public static DataPacket unmarshall(Buffer buff, CRC crc){
		byte[] data = new byte[buff.remaining() - crc.size() - 1];
		
		buff.get(data);
		
		ZModemCharacter type;
		type = ZModemCharacter.forbyte(buff.get());

		
		byte[] netCrc = new byte[crc.size()];
		buff.get(netCrc);
		
		if(!Arrays.equals(netCrc, crc.getBytes()))
			throw new InvalidChecksumException();
		
		return  new DataPacket(type,data);
	}
	
	
	private ZModemCharacter type;
	private byte[] data = new byte[0];
	
	public DataPacket(ZModemCharacter fe){
		type = fe;
	}
	
	public DataPacket(ZModemCharacter fr, byte[] d){
		this(fr);
		data = d;
	}
	
	public void copyData(byte[] d){
		data = Arrays.copyOf(d, d.length);
	}
	
	public byte[] data(){
		return data;
	}
	
	@Override
	public Buffer marshall(){
		ZDLEEncoder encoder;
		ByteBuffer buff = ByteBuffer.allocateDirect(data.length*2+64);
		
		CRC crc = new CRC(CRC.Type.CRC16);
		
		encoder = new ZDLEEncoder(data);
		
		crc.update(data);
		buff.put(encoder.zdle(),0,encoder.zdleLen());
				
		buff.put(ZModemCharacter.ZDLE.value());
		
		crc.update(type.value());
		buff.put(type.value());
		
		crc.finalize();
		
		encoder = new ZDLEEncoder(crc.getBytes());
		buff.put(encoder.zdle(),0,encoder.zdleLen());
		
		buff.flip();
		
		return buff;
	}
	
	public void setData(byte[] d){
		data = d;
	}
	
	@Override
	public String toString() {
		return type+":"+data.length+" bytes";
	}
	

	public ZModemCharacter type(){
		return type;
	}
}
