package net.jones.serialModem.modem;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;

import net.jones.serialModem.zmodem.XModem;
import net.jones.serialModem.zmodem.YModem;

import org.apache.commons.lang3.StringUtils;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

public class SerialModem {

 	protected final static char   LF       = '\n';
	protected final static char   CR       = '\r';
	protected final static char   ST	   = '*';
	
	protected final static String CRLF     = "\r\n";	
	protected final static String QT       = "`";
	
	protected final static String splush   = "file:///home/pi/banner.asc";
	protected final static String inbound  = "/home/pi/Transfer/inbound";
	protected final static String outbound = "/home/pi/Transfer/outbound";
	protected final static String _part    = "/part";
	protected final static String _bbs     = "/bbs";
	protected final static byte [] CLEAR   = new byte[] {27,91,50,74};
	
	protected final static int TO          = 30000;
	protected final static int SSHPORT     = 22;
	protected final static String CONFAIL  = CRLF+CRLF+"Connection Failed"+CRLF;
	protected final static String CONECT   = CRLF+CRLF+"Connecting....."+CRLF;
	protected final static String HUH      = CRLF+CRLF+"Huh?!"+CRLF;
	protected final static String PROMPT   = CRLF+CRLF+" PiModem- ";
	
	protected final Hashtable<Integer, BBS> bbss  = new Hashtable<Integer, BBS>();
	protected Logger lg;

	protected boolean  disconnected = true, timerb = true;
	protected Socket   socket;
	protected InputStream srIn;
	protected OutputStream srOut;
	protected Channel  channel;
	protected YModem  yModem;
	protected XModem  xModem;
	protected long timer;
	protected byte [] header,help,menu;
	protected int opts, stopCnt; 
	protected CommPortIdentifier portIdentifier;

	
	void startSession(String portName, int bRate) throws Exception {
		
   		portIdentifier = CommPortIdentifier.getPortIdentifier(portName);		
		if (portIdentifier.isCurrentlyOwned()) throw new IOException("Error - Serial port in use!");

		buildMenu();				
		CommPort commPort = portIdentifier.open(this.getClass().getName(), TO);
		SerialPort serialPort = (SerialPort) commPort;

		serialPort.setSerialPortParams(
				bRate,
				SerialPort.DATABITS_8, 
				SerialPort.STOPBITS_1,
				SerialPort.PARITY_NONE);
		
		srOut  = serialPort.getOutputStream();
		srIn   = new ModemInputStream(serialPort.getInputStream());
		
		yModem = new YModem(srIn, srOut);
		xModem = new XModem(srIn, srOut);
		
		srOut.write(CLEAR);
		srOut.write(header);
		srOut.write(menu);

		lg.info("Serial Modem Restarted: "+portName+ " @ "+bRate);
		
		while (true) {
			if(disconnected) {								
				srOut.write((PROMPT).getBytes());
				if(! processCommand(getStringFromPort(false).trim())) 
					srOut.write((CONFAIL).getBytes());

			} 
			Thread.sleep(1000);  
		}	
	}

	
	protected void buildMenu() {
		bbss.clear();
		opts = 0;
		try {
			String splash = new String(Files.readAllBytes(Paths.get(new URI(splush))));
			String[] part = splash.split(_part);
			header = part[0].replaceAll("\n", CRLF).getBytes();
			help   = part[1].replaceAll("\n", CRLF).getBytes();
			String b[] = part[2].split(_bbs);
			StringBuffer mn = new StringBuffer("");

			for (int x = 0; x < b.length; x++) {
				BBS bbs = new BBS(b[x]);
				if (bbs.port > 0) {
					bbss.put(new Integer(bbs.number), bbs);
					mn.append("   " + bbs.number 
							+ "  -  " + bbs.name.trim()
							+ "  -  " + bbs.host.trim() + ":" + bbs.port + CRLF);
				}
			}
			menu = mn.toString().getBytes();
		} catch (Exception e ){
			header = (CRLF+" >> Atari usbModem << "+CRLF).getBytes();	
			help   = (CRLF+" Help missing !!! "+CRLF).getBytes();	
            menu   = (CRLF+" >> Atari usbModem << "+CRLF).getBytes();
		}
	}

	protected boolean processCommand(String command) throws IOException  {

		String opt = "";
		if(command == null || command.equals("")) return true;
		
		StringTokenizer st = new StringTokenizer(command.trim());
		int stCount = st.countTokens();
		ArrayList<String> cmd = new ArrayList<String>(stCount);
		while (st.hasMoreElements()) cmd.add(st.nextToken());
		
		if (stCount > 0) opt = cmd.get(0).toLowerCase().trim();
 		
	     if(stCount == 1 && StringUtils.isNumeric(command)) {
		   return connectBBS(Integer.parseInt(command));
	     
	     } else if("atz".equals(opt) || "cls".equals(opt)) {		
			srOut.write(CLEAR);
			srOut.write(header);
			srOut.write(menu);
			srOut.write(CRLF.getBytes());
			srOut.flush();
			return true;

		} else	if("?".equals(opt) || "help".equals(opt)){		
			srOut.write(CLEAR);
			srOut.write(help);
			srOut.write(CRLF.getBytes());
			srOut.flush();
			return true;

		} else	if("getdtm".equals(opt)){		
			srOut.write( (new SimpleDateFormat("yyyy MM dd kk mm").format(new Date())).getBytes());
			srOut.write(CRLF.getBytes());
			srOut.flush();
			return true;
			
		} else if(( (stCount == 3 && "atdt".equals(opt)) ||  
	  	 	  	    (stCount == 3 && "bbs".equals(opt)))  &&
			        StringUtils.isNumeric(cmd.get(2)) ) {
			return connectTCP(cmd.get(1), Integer.parseInt(cmd.get(2)));

			
		} else if("timer".equals(opt)){
			timerb = ! timerb;
			srOut.write(CRLF.getBytes());
			srOut.write(("   Inactive timer:  " + (timerb ?"On":"off") ).getBytes());
			srOut.write(CRLF.getBytes());
			srOut.flush();
			return true;
		
		} else if(stCount == 2 && "ssh".equals(opt)) {
			return connectSSH(cmd.get(1));
		
		} else if("lsi".equals(opt)) {
			return listFiles(inbound);

		} else if("lso".equals(opt)) {
			return listFiles(outbound);
			
		} else if("ysend".equals(opt)) {
			return connectYSND();

		} else if("yrecv".equals(opt)) {
			return connectYREC();
	
		} else if("xsend".equals(opt) & stCount == 2) {
			return connectXSND(cmd.get(1));

		} else if("xrecv".equals(opt) & stCount == 2 ) {
			return connectXREC(cmd.get(1));
					
		} else {			
			srOut.write(HUH.getBytes());
			return true;
		}

	}

		
	protected boolean listFiles(String path) {
		File dir = new File(path);
		try {
			srOut.write((CRLF+"  Files in: "+path+CRLF).getBytes());
			if (!dir.isDirectory())	return false;
			File[] lst = dir.listFiles();
			for (int i = 0; i < lst.length; i++) 
				srOut.write( ((lst[i].isFile()?"   ":" * ") + lst[i].getPath()+CRLF).getBytes());
			
			srOut.write((CRLF+CRLF).getBytes());
		} catch (IOException e) {
			lg.log(Level.SEVERE, e.getMessage(), e);			
			return false;
		}
		return true;
	}
 
	

	protected  String getStringFromPort(boolean pwd) {	
		StringBuilder line = new StringBuilder();
		int c = 0;
		try {
			while ((c = srIn.read()) > -1 && c != 0x0D) {			
				c = (c == 0x7F) ? 0x08 : c;	
				//System.out.print((char) (c & 0xFF));
				if(pwd)
					srOut.write(ST & 0xFF);
				else
					srOut.write(c  & 0xFF);

				if(c == 0x08 && line.length() > 0)
					line.deleteCharAt(line.length()-1);
				else
					line.append((char) (c & 0xFF));				
			
			}			

		} catch (IOException e) {
			lg.log(Level.SEVERE, e.getMessage(), e);
			return "";
		}
		
		return line.toString().trim();
	}
	
	
	
	protected boolean connectBBS(int command) throws IOException {
		Integer o = new Integer(command);
		if(!bbss.containsKey(o)) return false;		
		BBS bbs =  bbss.get(o);		
		connectTCP(bbs.host, bbs.port);
		return true;
	}


	
	protected boolean connectYREC() throws IOException    {
		disconnected = false;
		timerb = false;
		Path path;
		try {			
			srOut.write((CRLF+CRLF+" ** YMODEM batch receive to :"+inbound+CRLF).getBytes());
			srOut.flush();
			path = Paths.get(new URI("file://"+inbound));
			Thread.sleep(5000);
			yModem.receiveFilesInDirectory(path);
			disconnected = true;
			return true;
		
		} catch (Exception e) {
			lg.log(Level.SEVERE, e.getMessage(), e); 
			srOut.write((CRLF+CRLF+CONFAIL+CRLF).getBytes());
			disconnected = true;
			return false;
		} 
		
	}


	protected boolean connectYSND() throws IOException   {
		disconnected = false;
		timerb = false;
		try {
			srOut.write((CRLF+CRLF+" ** YMODEM batch send from :"+outbound+CRLF).getBytes());
			srOut.flush();
			
			File [] files = new File(outbound).listFiles();
			Thread.sleep(1000);
			yModem.batchSend(files);			
			disconnected = true;
			return true;
		
		} catch (Exception e) {
			lg.log(Level.SEVERE, e.getMessage(), e);	
			srOut.write((CRLF+CRLF+CONFAIL+CRLF).getBytes());
			disconnected = true;
			return false;
		} 
	}


	protected boolean connectXSND(String file) throws IOException   {
		disconnected = false;
		timerb = false;
		Path path;
		try {
			srOut.write((CRLF+CRLF+" ** XMODEM sending file :"+file+CRLF).getBytes());
			srOut.flush();
			
			path = Paths.get(new URI("file://"+outbound+"/"+file));
			Thread.sleep(5000);
			xModem.send(path, false);			
			disconnected = true;
			return true;
		
		} catch (Exception e) {
			lg.log(Level.SEVERE, e.getMessage(), e);	
			srOut.write((CRLF+CRLF+CONFAIL+CRLF).getBytes());
			disconnected = true;
			return false;
		} 
	}

	
	protected boolean connectXREC(String file) throws IOException   {
		disconnected = false;
		timerb = false;
		Path path;
		try {
			srOut.write((CRLF+CRLF+" ** XMODEM receiving file "+file+CRLF).getBytes());					
			srOut.flush();
			
			path = Paths.get(new URI("file://"+inbound+"/"+file));
			Thread.sleep(5000);
			xModem.receive(path,false);			
			disconnected = true;
			return true;
		
		} catch (Exception e) {
			lg.log(Level.SEVERE, e.getMessage(), e);
			srOut.write((CRLF+CRLF+CONFAIL+CRLF).getBytes());
			disconnected = true;
			return false;
		} 
	}
	
	protected boolean connectTCP(String host, int port) throws IOException   {
		srOut.write(CONECT.getBytes());

		try {if(socket != null && socket.isConnected()) socket.close(); Thread.sleep(2000);} catch (Exception n) {}

		try {
			disconnected = false; 
			socket = new Socket(host, port);

			(new Thread(new TCPReadWrite(srIn, socket.getOutputStream()) )).start();
			(new Thread(new TCPReadWrite(socket.getInputStream(), srOut) )).start();

		} catch (Exception e) {
			lg.log(Level.WARNING, e.getMessage(), e);
			try {if(socket != null && socket.isConnected()) socket.close();} catch (Exception n) {}
			srOut.write(e.getMessage().getBytes());
			disconnected = true;
			stopCnt = 0;
			return false;
		} 
		return true;
	}



	protected boolean connectSSH(String command) throws IOException   {

		try {if (channel != null && channel.isConnected()) {channel.disconnect();Thread.sleep(2000);}} catch (Exception n) {}

		try {
				
			JSch jsch = new JSch();
			int sp = command.indexOf('@');
			
			String user = command.substring(0, sp);
			String host = command.substring(sp+1);
	
			srOut.write((CRLF+CRLF+"Password: ").getBytes());
            String psswd = getStringFromPort(true);

			srOut.write((CRLF+CRLF+"Terminal type (dumb): ").getBytes());
            String tty = getStringFromPort(false);
            if(null == tty || tty.equals("")) tty = "dumb";
            		
			if(host.length() < 1 || user.length() < 1 || psswd.length() < 1) return false;
			srOut.write(CONECT.getBytes());

			Session session = jsch.getSession(user, host, SSHPORT);
			session.setPassword(psswd);
			java.util.Properties config = new java.util.Properties(); 
			config.put("StrictHostKeyChecking", "no");
			session.setConfig(config);
			session.connect(TO);
			channel = session.openChannel("shell");
			
			//((ChannelShell) channel).setPtyType("dumb");
			//((ChannelShell) channel).setPtyType("xterm");
			//((ChannelShell) channel).setPtyType("vt100");
			
			((ChannelShell) channel).setPtyType(tty);
			((ChannelShell) channel).setEnv("LANG", "ja_JP.eucJP");

			disconnected = false;		 
			channel.setInputStream(srIn,true);
			channel.setOutputStream(srOut,true);
			channel.connect(3000);				 
			
			(new Thread(new ChannelCloser(channel) )).start();
			
			
		} catch (Exception e) {
			lg.log(Level.WARNING, e.getMessage(), e);
			try {if(channel != null && channel.isConnected()) channel.disconnect();} catch (Exception n) {}
			srOut.write((CRLF+e.getMessage()+CRLF).getBytes());
			disconnected = true;
			return false;
		} 
		
		return true;
	}

	
	public void  userExit()  {			
		disconnected = true;
		stopCnt = 0;
		try {Thread.sleep(500); } catch (InterruptedException e) {}
		try {if(socket  != null && socket.isConnected()) socket.close();} catch (Exception n) {}
		try {if(channel != null && channel.isConnected()){channel.disconnect();channel.getSession().disconnect();}} catch (Exception n) {}		
	}
	

	protected class BBS {
		public int number, port;
		public String host, name;
		public BBS(String bb) {
			try {
				String [] bbs = bb.split(QT);
				if(bbs.length > 2) {
					name = bbs[1]; 
					host = bbs[2];
					port = Integer.parseInt(bbs[3]);
					number = ++opts;
				} else {
					port   = -1;
					number = -1;
				}	
			} catch (Exception nf){
				port   = -1;
				number = -1;
			}
		}
	} 


	protected  class TCPReadWrite implements Runnable {
		InputStream in;
		OutputStream out;
		public TCPReadWrite(InputStream i, OutputStream o) {in = i; out = o;}
		public void run() {
			try {
				int b=0;
				while (!disconnected  && !socket.isClosed()) 
					if ((b = in.read()) > -1) out.write(b & 0xFF);	
			} catch (IOException e) {
				lg.log(Level.SEVERE, e.toString(), e);
			}
			disconnected = true;
		}
	}


	protected class ChannelCloser implements Runnable {
		Channel channel;
		public ChannelCloser(Channel chan) { channel = chan; }
		public void run() {
			try {
				while (channel.getExitStatus() == -1 && ! disconnected) {
					try {Thread.sleep(2000);} catch (Exception e) {}
				}
				
				userExit();
				
			} catch (Exception e) {
				lg.log(Level.SEVERE, e.getMessage(), e);
			}
			disconnected = true;
		}
	}
	
	
	
	protected class ModemInputStream extends InputStream  {
		protected InputStream  inputstream;
		public ModemInputStream(InputStream mis) {
			inputstream = mis;
			stopCnt = 0;
			timer = new Date().getTime() / 1000;	
			(new Thread(new Runnable() {
				@Override
				public void run() {
					while (true) {						
						try {Thread.sleep(60000);} catch (Exception e) {}
						long now = new Date().getTime() / 1000;
						if(timerb && !disconnected && (now - timer) > (5 * 60) )
							try {userExit();} catch (Exception e) {}
					}	

				}
			})).start();
		}

		
		protected void testStop (byte b) throws IOException {
			if(! disconnected  ) {								
				if (b == 43 ) {
					if (++stopCnt > 3)  userExit();
				} else  {
					stopCnt = 0;
				}
			}
			
		    timer = new Date().getTime() / 1000;
		}


		@Override
		public int read() throws IOException {
			int b =  inputstream.read();
			if(b > -1) testStop((byte) (b & 0xff));
			return b;
		}


		@Override
		public int read(byte[] b) throws IOException {
			int a = inputstream.read(b);
			if(a > -1) for(byte c:b) testStop(c);
			return a;
		}


		@Override
		public int read(byte[] b, int off,  int len) throws IOException {
			int a = inputstream.read(b, off, len>1024?1024:len);			
			if(a > -1) testStop(b[off]);
			return a;
		}

		@Override
		public long skip(long n) throws IOException {return inputstream.skip(n);}
		@Override
		public int available() throws IOException {return inputstream.available();}
		@Override
		public void close() throws IOException {userExit();} 
		@Override
		public void mark(int readlimit) {inputstream.mark(readlimit);}
		@Override
		public void reset() throws IOException {inputstream.reset();}
		@Override
		public boolean markSupported() { return inputstream.markSupported();}
		@Override
		public boolean equals(Object o) { return inputstream.equals(o);}
		@Override
		public String toString() { return inputstream.toString();}		

	}

	
	public void go(final String portname, final int bRate) {
		
		lg = Logger.getLogger( SerialModem.class.getName() );
		lg.setLevel(Level.ALL);
		lg.addHandler( new StreamHandler(System.out, new SimpleFormatter()));
	    
		while (true) {
			try {
				startSession(portname, bRate);				
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

	
	public static void main(String[] args) { 
		(new SerialModem()).go("/dev/ttyUSB0", 19200);
		}

}

