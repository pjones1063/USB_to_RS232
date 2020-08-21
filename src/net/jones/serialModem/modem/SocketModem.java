package net.jones.serialModem.modem;

 
import java.io.IOException;
import java.net.Socket;
import net.jones.serialModem.zmodem.XModem;
import net.jones.serialModem.zmodem.YModem;


public class SocketModem extends SerialModem {

	public static void main(String[] args)  { 		
		(new SocketModem()).go(9090); 
	} 
	private  Socket connectionSocket = null;
	
	private  int port = -1;
	
	public void go(int pport) {	
		port = pport;
		
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
		connectionSocket =  new Socket("localhost", port);	
		System.out.println("usbModem->Socket Modem Restarted on: localhost:" + port);

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

