package net.jones.serialModem.modem;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import net.jones.serialModem.BatchStartUp;
import net.jones.serialModem.zmodem.XModem;
import net.jones.serialModem.zmodem.YModem;

public class SocketServerModem extends SerialModem {

	private ServerSocket svrSock = null; 
	private Socket cntSock = null; 	
	private int port = -1;

	
	public static void main(String[] args)  {
			(new SocketServerModem()).go(9090);
	}
	
	
	public void go(int pport) {
		port = pport;		

		while (true) {
			try {		
				startSession();
			} catch (Exception e) {
				System.out.println("usbModem->:"+e.getMessage());				

			} finally {
				try {srIn.close();}     catch (Exception e) {}
				try {srOut.close();}    catch (Exception e) {}
				try {cntSock.close();}  catch (Exception e) {}
				try {svrSock.close();}  catch (Exception e) {}
			}

			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {}
		}
	}
	
	void startSession() throws Exception {
		
		try { if(cntSock != null) cntSock.close();}  catch (Exception e) {}		
		try { if(svrSock != null) svrSock.close();}  catch (Exception e) {}
		
		buildMenu();
		svrSock = new ServerSocket(port);
		System.out.println("usbModem->Socket Server Modem Restarted on port: "+port);
		System.out.println("    -m,--menufile       :  "+ BatchStartUp.splush);
		System.out.println("    -x,--xmlfile        :  "+ BatchStartUp.dialxml);
		System.out.println("    -o,--outboundfolder :  "+ BatchStartUp.outbound);
		System.out.println("    -i,--inboundfolder  :  "+ BatchStartUp.inbound);

		cntSock = svrSock.accept();
		srOut  = cntSock.getOutputStream();		
		srIn   = cntSock.getInputStream();
		yModem = new YModem(srIn, srOut);
		xModem = new XModem(srIn, srOut);
			
		srOut.write(CLEAR);
		srOut.write(header);

		while (true) {
			if(disconnected) {								
				srOut.write((prompt));
				if(! processCommand(getStringFromPort(false).trim())) 
					srOut.write((CONFAIL).getBytes());

			} 
			Thread.sleep(250);  
		}
	}
	
	protected int userPassword() throws IOException {
		cntSock.getOutputStream().write(bbsHost.password.getBytes());
		cntSock.getOutputStream().flush();
		return -1;
	}
	protected int userUserID() throws IOException {
		cntSock.getOutputStream().write(bbsHost.user.getBytes());
		cntSock.getOutputStream().flush();
		return -1;
	}

}

