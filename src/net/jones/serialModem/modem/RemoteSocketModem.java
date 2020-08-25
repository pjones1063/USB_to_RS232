package net.jones.serialModem.modem;

 
import java.io.IOException;
import java.net.Socket;

import net.jones.serialModem.BatchStartUp;
import net.jones.serialModem.zmodem.XModem;
import net.jones.serialModem.zmodem.YModem;


public class RemoteSocketModem extends SerialModem {

	public static void main(String[] args)  { 		
		(new RemoteSocketModem()).go("localhost", 9090); 
	} 
	private  Socket connectionSocket = null;
	
	private  int port = -1;
	private  String host = "";
	
	
	public void go(String phost, int pport) {	
		port = pport;
		host = phost;
		
		while (true) {
			try {
				startSession();
			} catch (Exception e) {
				System.out.println("usbModem->:"+e.getMessage());			
	
			} finally {
					try {srIn.close();}  catch (Exception e) {}
					try {srOut.close();} catch (Exception e) {}
			}
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {}
		}
}
	
	void startSession() throws Exception {
		
		buildMenu();
		connectionSocket =  new Socket(host, port);	
		
		System.out.println("usbModem->Remote Socket Modem Restarted on: "+host+":" + port);
		System.out.println("    -m,--menufile       :  "+ BatchStartUp.splush);
		System.out.println("    -x,--xmlfile        :  "+ BatchStartUp.dialxml);
		System.out.println("    -o,--outboundfolder :  "+ BatchStartUp.outbound);
		System.out.println("    -i,--inboundfolder  :  "+ BatchStartUp.inbound);

		
		srOut  = connectionSocket.getOutputStream();
		srIn   = connectionSocket.getInputStream();
		yModem = new YModem(srIn, srOut);
		xModem = new XModem(srIn, srOut);
		
		srOut.write(CLEAR);
		srOut.write(header);
		
		while (true) {
			if(disconnected) {								
				srOut.write((PROMPT).getBytes());
				if(! processCommand(getStringFromPort(false).trim())) 
					srOut.write((CONFAIL).getBytes());

			} 
			Thread.sleep(250);  
		}
	}
	
	protected int userPassword() throws IOException {
		connectionSocket.getOutputStream().write(bbsHost.password.getBytes());
		connectionSocket.getOutputStream().flush();
		return -1;
	}
	
	
	protected int userUserID() throws IOException {
		connectionSocket.getOutputStream().write(bbsHost.user.getBytes());
		connectionSocket.getOutputStream().flush();
		return -1;
	}
	
}

