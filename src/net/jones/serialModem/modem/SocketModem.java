package net.jones.serialModem.modem;

 
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;

import net.jones.serialModem.zmodem.XModem;
import net.jones.serialModem.zmodem.YModem;


public class SocketModem extends SerialModem {

	private  Socket connectionSocket = null; 
	private  int port = -1;
	
	void startSession() throws Exception {
		
		buildMenu();
		connectionSocket =  new Socket("localhost", port);		
		lg.info("Socket Modem Restarted on: localhost:" + port);

		srOut  = connectionSocket.getOutputStream();
		srIn   = new ModemInputStream(connectionSocket.getInputStream());
		yModem = new YModem(srIn, srOut);
		xModem = new XModem(srIn, srOut);
		
		srOut.write(CLEAR);
		srOut.write(header);
		srOut.write(menu);
		
		while (true) {
			if(disconnected) {								
				srOut.write((PROMPT).getBytes());
				if(! processCommand(getStringFromPort(false).trim())) 
					srOut.write((CONFAIL).getBytes());

			} 
			Thread.sleep(1000);  
		}
	}
	
	public void go(int pport) {	
		
		port = pport;
		lg = Logger.getLogger( SocketModem.class.getName() );
		lg.setLevel(Level.ALL);
		lg.addHandler( new StreamHandler(System.out, new SimpleFormatter()));		 
		
		while (true) {
			try {
				startSession();
			} catch (Exception e) {
				lg.log(Level.WARNING, e.getMessage());	
			} finally {
					try {srIn.close();}  catch (Exception e) {}
					try {srOut.close();} catch (Exception e) {}
			}
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {}
		}
}
	
	
	public static void main(String[] args)  { 		
		(new SocketModem()).go(9090); 
	}
	
}

