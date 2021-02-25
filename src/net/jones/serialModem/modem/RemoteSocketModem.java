package net.jones.serialModem.modem;

 
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.logging.Level;

import net.jones.serialModem.BatchStartUp;
import net.jones.serialModem.zmodem.XModem;
import net.jones.serialModem.zmodem.YModem;


public class RemoteSocketModem extends SerialModem {

	private  Socket connectionSocket = null;
	private  int port = -1;
	private  String host = "";
	
	public static void main(String[] args)  { 		
		(new RemoteSocketModem()).go("localhost", 9090); 
	} 

	
	
	public void go(String phost, int pport) {	
		cmdList = new ArrayList<String>();
		cmdIndex = -1;
		port = pport;
		host = phost;
		while (true) {
			try {
				startSession();
			} catch (Exception e) {
				LG.log(Level.SEVERE, " Exception:", e);			

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
		cmdList = new ArrayList<String>();
		
		buildMenu();
		connectionSocket =  new Socket(host, port);	
		
		LG.info(" ->Remote Socket Modem Restarted on: "+host+":" + port);
		
		srOut  = connectionSocket.getOutputStream();
		srIn   = connectionSocket.getInputStream();
		yModem = new YModem(srIn, srOut);
		xModem = new XModem(srIn, srOut);
		
		srOut.write(CLEAR);
		srOut.write(header);
		
		while (true) {
			if(disconnected) {								
				srOut.write(prompt);
				String cmd = getStringFromPort(false).trim();
				if(cmd != null && !cmd.equals(""))  
					if(cmd != null && !cmd.equals(""))  
						if(processCommand(cmd)) { 
							if(! cmdList.contains(cmd)) cmdList.add(cmd);
						}	else { 
							srOut.write((CONFAIL).getBytes());
						}				
					if(cmdList.contains(cmd)) cmdIndex = cmdList.indexOf(cmd);
			}
			
			Thread.sleep(100);  
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

