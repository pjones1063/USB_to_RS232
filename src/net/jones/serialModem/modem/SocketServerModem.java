package net.jones.serialModem.modem;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.logging.Level;

import net.jones.serialModem.BatchStartUp;
import net.jones.serialModem.modem.SerialModem.MacroInputStream;
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
				LG.log(Level.SEVERE, " Exception:", e);				

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
		cmdList = new ArrayList<String>();
		cmdIndex = -1;
		
		try { if(cntSock != null) cntSock.close();}  catch (Exception e) {}		
		try { if(svrSock != null) svrSock.close();}  catch (Exception e) {}
		
		buildMenu();
		svrSock = new ServerSocket(port);
		LG.info(" ->Socket Server Modem Restarted on port: "+port);

		cntSock = svrSock.accept();
		srOut  = cntSock.getOutputStream();		
		srIn   = cntSock.getInputStream();
		yModem = new YModem(srIn, srOut);
		xModem = new XModem(srIn, srOut);
			
		srOut.write(CLEAR);
		srOut.write(header);

		while (true) {
			if(disconnected) {								
				srOut.write(prompt);
				String cmd = getStringFromPort(false).trim();
				if(cmd != null && !cmd.equals(""))  
					if(processCommand(cmd))	
						cmdList.add(cmd);
					else 
						srOut.write((CONFAIL).getBytes());
			} 
			Thread.sleep(100);  
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

