package net.jones.serialModem.modem;

import com.fazecast.jSerialComm.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;

import javax.print.attribute.HashPrintJobAttributeSet;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import net.jones.serialModem.BatchStartUp;
import net.jones.serialModem.zmodem.XModem;
import net.jones.serialModem.zmodem.YModem;

import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.FileHandler;


public class SerialModem {
	
	protected final static char   LF          = '\n';	
	protected final static char   CR          = '\r';
	protected final static char   ST	      = '*';	
	
	protected final static int    ESC         = 0x1B;
	protected final static int    DEL         = 0x08;
	protected final static int    BS          = 0x7F;
	protected final static int    UP          = 0x41;
	protected final static int    DOWN        = 0x42;
	
	protected final static int    TO          = 30000;
	protected final static int    SSHPORT     = 22;
	

	protected final static byte [] CLEAR      = new byte[] {27,91,50,74};
	
	protected final static String CRLF        = "\r\n";
	protected final static String SPC5        = "     ";
	protected final static String SPC1        = " ";	
	protected final static String QT          = "`";
	protected final static String _part       = "/part";
	protected final static String _fl         = "file://";
	protected final static String CONFAIL     = CRLF+CRLF+" * Something didn't work! *"+CRLF;
	protected final static String TIMEOUTEXIT = CRLF+"  Inactive Timeout Reached"+CRLF;
	protected final static String CONEXIT     = CRLF+"  Connection Exit"+CRLF;	
	protected final static String CONECT      = CRLF+CRLF+"Connecting....."+CRLF;
	protected final static String SLIST       = CRLF+CRLF+"  Num    Name  -  Host" +CRLF+
			                                              "  ---    ---------------------------------" +CRLF;
	protected final Hashtable<Integer, BBSEntry> bbsHostTable  = new Hashtable<Integer, BBSEntry>();
	protected boolean  disconnected = true;
	protected Socket   socket;
	protected InputStream srIn;
	protected OutputStream srOut;
	protected Channel  channel;
//  protected CommPortIdentifier portIdentifier;	
    protected Session session;
	protected YModem   yModem;
	protected XModem   xModem;
	protected BBSEntry bbsHost;
	
	public byte [] header,help, prompt;
	protected int opts;
	protected boolean esc = false;
	
	protected ArrayList<String> cmdList;
	protected int cmdIndex = -1;

    protected static final Logger LG = Logger.getLogger(SerialModem.class.getName());

 	protected class BBSEntry {
		public int number, port;
		public String host, name,user, password;
		public boolean ssh;
		public BBSEntry(String bb) {
			ssh = false;
			user = "";
			password = "";		
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
					port = Integer.parseInt(bbs[3]);				}	
			} catch (Exception nf){
				port   = -1;
				number = -1;
			}
			
		}

		public BBSEntry(String bbsName, String bbsHost, String bbsPort, String bbsProtocol, String bbsLogin, String bbsPassword ) {
			try {
				name = bbsName;
				host = bbsHost;
				port = Integer.parseInt(bbsPort);
				ssh = bbsProtocol.equals("ssh");
				user = bbsLogin;
				password = bbsPassword;
				
				number = ++opts;	
			} catch (Exception nf){
				port   = -1;
				number = -1;
			}
		}
	}
 		

 protected  class TelnetThreader implements Runnable {
 		InputStream in;
 		OutputStream out;
 		public TelnetThreader(InputStream i, OutputStream o) {
 			in  = i;
 			out = o;
 		}
 		public void run() {
 			try {
 				int b=0;
 				while (!disconnected  && !socket.isClosed()) 
 					if ((b = in.read()) > -1) out.write(b & 0xFF);	
 			} catch (IOException e) {
 				LG.info("-> telnet connection closing");
 			}
 			
 			disconnected = true;
 			try {in.close();} catch (IOException e)  {}
 			try {out.close();} catch (IOException e) {}	
 		}
 	}
	
  	
	protected class MacroOutputStream extends OutputStream  {
		protected OutputStream  outputStream;
		
		public MacroOutputStream(OutputStream mos) {
				outputStream = mos;		
		}
		
		@Override
		public void write(int b) throws IOException {
		    outputStream.write(b);		    
		}
		@Override
		public void close() throws IOException {
			LG.info("-> closing output buffer"); // but not really
			disconnected = true;
			outputStream.flush();
			userExit();
		}
		@Override
		public void write(byte [] b) throws IOException {
		    outputStream.write(b);		    
		}
		@Override
		public void write(byte [] b, int o, int l) throws IOException {
		    outputStream.write(b,o,l);		    
		}
		@Override
		public void flush() throws IOException {
		    outputStream.flush();
		}		
		@Override
		public boolean equals(Object o) { return outputStream.equals(o);}
		
	}
 	
	protected class MacroInputStream extends InputStream  {
		protected InputStream  inputstream;
		public MacroInputStream(InputStream mis) {
			inputstream = mis;
			esc = false;		
		}

		@Override
		public int available() throws IOException {return inputstream.available();}
		@Override
		public void close() throws IOException {
				LG.info("-> closing input buffer");  // but not really
				disconnected = true;
			}
		
		protected int doMacros(int chr) throws IOException {
			if (!disconnected) {
			 if(esc && chr == 45) 
				 return userExit();
			 else if (esc && chr == 117 && !bbsHost.user.isEmpty()) 
				  return userUserID();				  
			 else if (esc && chr == 112 && !bbsHost.password.isEmpty()) 
				  return userPassword();
			 
			}
			esc = (chr==27)? true: false;					
			return chr;
		}

		@Override
		public boolean equals(Object o) { return inputstream.equals(o);}
		@Override
		public void mark(int readlimit) {inputstream.mark(readlimit);}
		@Override
		public boolean markSupported() { return inputstream.markSupported();}
		@Override
		public int read() throws IOException {
			int c =  inputstream.read();			
			return doMacros(c);
		} 
		@Override
		public int read(byte[] b) throws IOException {
			int a = inputstream.read(b);
			if(a > -1) doMacros(b[0]);
			return a;
		}
		@Override
		public int read(byte[] b, int off,  int len) throws IOException {
			int a = inputstream.read(b, off, len >1024?1024:len);
			if(a > -1) doMacros(b[off]);
			return a;
		}
		@Override
		public void reset() throws IOException {inputstream.reset();}
		@Override
		public long skip(long n) throws IOException {return inputstream.skip(n);}
		@Override
		public String toString() { return inputstream.toString();}		

	}	
	
	protected void buildBBSDirectory () {
		bbsHostTable.clear();
		opts = 0;
		try {	
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.parse(new File(BatchStartUp.dialxml));
			NodeList nodeList = document.getDocumentElement().getElementsByTagName("BBS");
			for (int i = 0; i < nodeList.getLength(); i++) {
				Node node = nodeList.item(i);
				if(node.getNodeName().equals("BBS")) {
					NamedNodeMap m =  node.getAttributes();

					BBSEntry bbs = new BBSEntry(
							m.getNamedItem("name").getNodeValue(),
							m.getNamedItem("ip").getNodeValue(), 
							m.getNamedItem("port").getNodeValue(),
							m.getNamedItem("protocol").getNodeValue(),
							m.getNamedItem("login").getNodeValue(),
							m.getNamedItem("password").getNodeValue() );

					bbsHostTable.put(new Integer(bbs.number), bbs);
				}
			}

		} catch (Exception e) {
			LG.log(Level.SEVERE, " Exception:", e);
		}                  
	}


	protected boolean doSaveBBSDirectory (int n, String login, String password) {

		if (!bbsHostTable.containsKey(n)) return false;
		
		BBSEntry updBBS = bbsHostTable.get(n);		
		
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.parse(new File(BatchStartUp.dialxml));
			NodeList nodeList = document.getDocumentElement().getElementsByTagName("BBS");
			for (int i = 0; i < nodeList.getLength(); i++) {
				Node node = nodeList.item(i);
				if(node.getNodeName().equals("BBS")) {
					NamedNodeMap m =  node.getAttributes();
					if(updBBS.name.equals(m.getNamedItem("name").getNodeValue() )) {
						m.getNamedItem("login").setNodeValue(login);
						m.getNamedItem("password").setNodeValue(password);

						TransformerFactory transformerFactory =  TransformerFactory.newInstance();
						Transformer transformer = transformerFactory.newTransformer();
						DOMSource dom = new DOMSource(document);
						StreamResult streamResult = new StreamResult(new File(BatchStartUp.dialxml) );
						transformer.transform(dom, streamResult);
						
						Thread.sleep(250);	
						buildBBSDirectory();
						
						srOut.write((CRLF+" Info saved: "+updBBS.name+CRLF).getBytes());
						srOut.flush();						
						
						return true;					
					}
				}
			}

		} catch (Exception e) {
			LG.log(Level.SEVERE, " Exception:", e);
			return false;
		}            
		return false; 
	}

	

	protected boolean doListBBSDirectory (int n) {

		if (!bbsHostTable.containsKey(n)) return false;
		BBSEntry lstBBS = bbsHostTable.get(n);
		
		try {
			srOut.write((CRLF+CRLF+ "  * "+lstBBS.name).getBytes());
			srOut.write((CRLF+" Host : "+lstBBS.host +":"+ lstBBS.port).getBytes());
			srOut.write((CRLF+" User : "+lstBBS.user).getBytes());
			srOut.write((CRLF+" Pw   : "+lstBBS.password).getBytes());
			srOut.write((CRLF+" ssh  : "+lstBBS.ssh+CRLF).getBytes());
			return true;

		} catch (Exception e) {
			LG.log(Level.SEVERE, " Exception:", e);
			return false;
		} 
	}

	
	protected boolean doPromptSet(String newprompt ) {
		try {
			
			Path path = Paths.get(new URI(_fl+BatchStartUp.splush));
			String splash = new String(Files.readAllBytes(path));
			
			String[] part = splash.split(_part);
			header = part[0].replaceAll("\n", CRLF).getBytes();
			help   = part[1].replaceAll("\n", CRLF).getBytes();
			prompt = (CRLF+CRLF+SPC1+ newprompt.trim() + SPC1).getBytes();
			
			splash = part[0]   + _part +
				     part[1]   + _part +
					 newprompt + _part; 
			
			Files.write(path, splash.getBytes());
			
			srOut.write((CRLF+SPC1+"Prompt set to: "+newprompt+CRLF).getBytes());
			
		} catch (Exception e ){
			prompt  = (CRLF+CRLF+SPC1+":>"+SPC1).getBytes();
			return false;	

		}
		return true;
	}

	
	
	protected void buildMenu() {
		try {
			String splash = new String(Files.readAllBytes(Paths.get(new URI(_fl+BatchStartUp.splush))));
			String[] part = splash.split(_part);
			header = part[0].replaceAll("\n", CRLF).getBytes();
			help   = part[1].replaceAll("\n", CRLF).getBytes();
			prompt = (CRLF+CRLF+SPC1+ part[2].trim() + SPC1).getBytes();

			buildBBSDirectory();			
		} catch (Exception e ){
			header = (CRLF+" >> Atari usbModem << "+CRLF).getBytes();	
			help   = (CRLF+" Help missing !!! "+CRLF).getBytes();	
			prompt  = (CRLF+CRLF+SPC1+":>"+SPC1).getBytes();
		}		
	}


	protected boolean doClear() {
		try {
			srOut.write(CLEAR);
			srOut.write(header);
			srOut.write(CRLF.getBytes());
			srOut.flush();
		} catch (IOException e) {
			LG.log(Level.SEVERE, " Exception:", e);
			return false;
		}
		return true;
	}
	
	
	@SuppressWarnings("removal")
	protected boolean doConnect(int command) throws IOException {
		Integer o = new Integer(command);
		if(!bbsHostTable.containsKey(o)) return false;		
		bbsHost =  bbsHostTable.get(o);	
		
		if(bbsHost.ssh)
			return doConnectSSH("bbsuser@"+bbsHost.host+":"+bbsHost.port);
		else
			return doConnectTelnet(bbsHost.host, bbsHost.port);
	}

	
	protected boolean doConnectSSH(String command) throws IOException   {

		try {
			if(channel != null) channel.disconnect();
			channel = null;	
		} catch (Exception n) { }

		
		try {
			if(session != null) session.disconnect();
			session = null;	
		} catch (Exception n) { }

		
		try {
			LG.info("-> doConnectSSH():" + command);
			JSch jsch = new JSch();
			int sp = command.lastIndexOf('@');

			String user = command.substring(0, sp);
			String host = command.substring(sp+1);
			int    port = SSHPORT;

			if(host.contains(":")) {
				try {
					sp = host.lastIndexOf(":");
					port = Integer.parseInt(host.substring(sp+1));
					host = host.substring(0, sp);
				} catch (Exception n) {} 
			}
   
			srOut.write((CRLF+CRLF+SPC5+"Password: ").getBytes());
			String psswd = getStringFromPort(true);

			srOut.write((CRLF+CRLF+SPC5+"Terminal type (ansi): ").getBytes());
			String pty = getStringFromPort(false);
			if(null == pty || pty.equals("")) pty = "ansi";

			srOut.write((CRLF+SPC5+"Terminal width (80): ").getBytes());
			String width = getStringFromPort(false);
			if(null == width || width.equals("")) width = "80";

			srOut.write((CRLF+SPC5+"Terminal height (25): ").getBytes());
			String height = getStringFromPort(false);
			if(null == height || height.equals("")) height = "25";

			if(host.length() < 1 || user.length() < 1 || psswd.length() < 1) return false;
			srOut.write(CONECT.getBytes());

			session = jsch.getSession(user, host, port);
			session.setPassword(psswd);
			 
			java.util.Properties config = new java.util.Properties(); 
			config.put("StrictHostKeyChecking", "no");
			session.setConfig(config);
			session.connect(0);

			channel = session.openChannel("shell");

			((ChannelShell) channel).setPtyType(pty);
			((ChannelShell) channel).setEnv("LANG", "ja_JP.eucJP");
			((ChannelShell) channel).setPtyType(pty,
					Integer.parseInt(width),Integer.parseInt(height),
					Integer.parseInt(width),Integer.parseInt(height));

			disconnected = false;		 
			channel.setInputStream(new MacroInputStream(srIn), false);
			channel.setOutputStream(new MacroOutputStream(srOut), false);
			
			channel.connect(60000);		

		} catch (Exception e) {
			LG.log(Level.SEVERE, " Exception:", e);
			srOut.write((CRLF + e.getMessage() + CRLF).getBytes());
			try {channel.disconnect();} catch (Exception e1) {}
			try {session.disconnect();} catch (Exception e1) {}			
			disconnected = true;
			esc = false;
			return false;
		}
		
		return true;
	}

	protected boolean doConnectTelnet(String hostadr) throws IOException {
		String[] host = hostadr.split(":");
		if (StringUtils.isNumeric(host[1]) )   			
			return doConnectTelnet(host[0], Integer.parseInt(host[1]));
		
		return false;
	}
	

	protected boolean doConnectTelnet(String host, int port) throws IOException   {
		srOut.write(CONECT.getBytes());

		try {if(socket != null && socket.isConnected()) socket.close(); Thread.sleep(2000);} catch (Exception n) {}

		try {
			
			LG.info("-> doConnectTelnet():" + host+":"+port);
			disconnected = false; 
			socket = new Socket(host, port);
			
			(new Thread(new TelnetThreader(new MacroInputStream(srIn), 
				socket.getOutputStream()) )).start();

			(new Thread(new TelnetThreader(socket.getInputStream(),
				new	MacroOutputStream(srOut)) )).start();
			
			
		} catch (Exception e) {
			LG.log(Level.SEVERE, " Exception:", e);
			try {if(socket != null && socket.isConnected()) socket.close();} catch (Exception n) {}
			srOut.write( (CRLF + e.getMessage() + CRLF).getBytes() );
			disconnected = true;
			esc = false;
			return false;
		} 
		return true;
	}
 
		
	
	protected boolean doConnectXREC(String file) throws IOException   {
		disconnected = false;
		Path path;
		try {
			LG.info("-> doConnectXREC():" + file);
			srOut.write((CRLF+CRLF+" ** XMODEM receiving file "+file+CRLF).getBytes());					
			srOut.flush();
			
			path = Paths.get(new URI(_fl + BatchStartUp.inbound + "/"+file));
			Thread.sleep(5000);
			xModem.receive(path,false);			
			disconnected = true;
			return true;
		
		} catch (Exception e) {
			LG.log(Level.SEVERE, " Exception:", e);
			srOut.write((CRLF+CRLF+CONFAIL+CRLF).getBytes());
			disconnected = true;
			return false;
		} 
	}

	
	protected boolean doConnectXSND(String file) throws IOException   {
		disconnected = false;
		Path path;
		try {			
			LG.info("-> doConnectXSND():" + file);
			srOut.write((CRLF+CRLF+" ** XMODEM sending file :"+file+CRLF).getBytes());
			srOut.flush();
			
			path = Paths.get(new URI(_fl + BatchStartUp.outbound + "/"+file));
			Thread.sleep(5000);
			xModem.send(path, false);			
			disconnected = true;
			return true;
		
		} catch (Exception e) {
			LG.log(Level.SEVERE, " Exception:", e);
			srOut.write((CRLF+CRLF+CONFAIL+CRLF).getBytes());
			disconnected = true;
			return false;
		} 
	}


	protected boolean doConnectYREC() throws IOException    {
		disconnected = false;
		Path path;
		try {			
			LG.info("-> doConnectYREC():");
			srOut.write((CRLF+CRLF+" ** YMODEM batch receive to :"+BatchStartUp.inbound+CRLF).getBytes());
			srOut.flush();
			path = Paths.get(new URI(_fl+BatchStartUp.inbound));
			Thread.sleep(5000);
			yModem.receiveFilesInDirectory(path);
			disconnected = true;
			return true;
		
		} catch (Exception e) {
			LG.log(Level.SEVERE, " Exception:", e);
			srOut.write((CRLF+CRLF+CONFAIL+CRLF).getBytes());
			disconnected = true;
			return false;
		} 		
	}


	protected boolean doConnectYSND() throws IOException   {
		disconnected = false;
		try {
			LG.info("-> doConnectYSND():");
			srOut.write((CRLF+CRLF+" ** YMODEM batch send from :"+BatchStartUp.outbound+CRLF).getBytes());
			srOut.flush();
			
			File [] files = new File(BatchStartUp.outbound).listFiles();
			Thread.sleep(1000);
			yModem.batchSend(files);			
			disconnected = true;
			return true;
		
		} catch (Exception e) {
			LG.log(Level.SEVERE, " Exception:", e);
			srOut.write((CRLF+CRLF+CONFAIL+CRLF).getBytes());
			disconnected = true;
			return false;
		} 
	}

	
	protected boolean doHelp() {
		try {
			srOut.write(help);
			srOut.flush();
		} catch (IOException e) {
			LG.log(Level.SEVERE, " Exception:", e);
			return false;
		}
		return true;
	}
	
	
	protected boolean doListServerFiles(String path) {
		File dir = new File(path);
		try {
			srOut.write((CRLF+"  Files in: "+path+CRLF).getBytes());
			if (!dir.isDirectory())	return false;
			File[] lst = dir.listFiles();
			for (int i = 0; i < lst.length; i++) 
				srOut.write( ((lst[i].isFile()?"   ":" * ") + lst[i].getPath()+CRLF).getBytes());
			
			srOut.write((CRLF+CRLF).getBytes());
			
		} catch (IOException e) {
			LG.log(Level.SEVERE, " Exception:", e);			
			return false;
		}
		return true;
	}



	
	protected boolean doServerDate() {
		try {
			LG.info("-> doServerDate()");
			srOut.write( (new SimpleDateFormat("yyyy MM dd kk mm ss").format(new Date())).getBytes());
			srOut.write(CRLF.getBytes());
			srOut.flush();
		} catch (IOException e) {
			LG.log(Level.SEVERE, " Exception:", e);
			return false;
		}
		return true;
	}

	
	
	protected boolean doServerY2KDate() {
		try {
			LG.info("-> doServerY2KDate()");
			Date d = new Date();
			int year30 =  Integer.parseInt(new SimpleDateFormat("yyyy").format(d)) - 30;
			srOut.write(( Integer.toString(year30) + new SimpleDateFormat(" MM dd kk mm ss").format(d)).getBytes());
			srOut.write(CRLF.getBytes());
			srOut.flush();
		} catch (IOException e) {
			LG.log(Level.SEVERE, " Exception:", e);
			return false;
		}
		return true;
	}
	
	
	protected boolean doSrcBBSListing(String src) throws IOException {

		LG.info("-> doSreachBBSListing:"+src);
		Enumeration<BBSEntry> bbsEnum = bbsHostTable.elements();
		DecimalFormat f = new DecimalFormat("0000");
		StringBuffer mn = new StringBuffer("");
		int i = 0;
		while (bbsEnum.hasMoreElements() && i < 25) {
			BBSEntry bbs = bbsEnum.nextElement();
			String name = bbs.name.toLowerCase();
			if (name.contains(src) && bbs.port > 0) {						
				if(bbs.password != null &&  bbs.password.length() > 0)
					mn.append("* " + f.format(bbs.number) + " - " + bbs.name.trim()
							+ " - " + bbs.host.trim() + ":" + bbs.port  + CRLF);
				else 
					mn.append("  " + f.format(bbs.number) + " - " + bbs.name.trim()
							+ " - " + bbs.host.trim() + ":" + bbs.port  + CRLF);
			}
		}

		if (mn.length() > 0) {
			srOut.write(SLIST.getBytes());
			srOut.write(mn.toString().getBytes());
			srOut.write(CRLF.getBytes());
			srOut.flush();
		}

		return true;
	}
		
	
	protected  String getStringFromPort(boolean isPassword) {	
		StringBuilder line = new StringBuilder();	
	    int c1 = -1, c2 = -1, c3 = -1;
	 
		try {
			while ((c1 = srIn.read()) != -1 && c1 != 0x0D) {		
				c1 = (c1 == 0x7F) ? 0x08 : c1;	 			
				if(c1 == DEL && line.length() > 0) 
					line.deleteCharAt(line.length()-1);				
				else if(c3 == ESC && (c1 == UP || c1 == DOWN)) 
					line = getLastCmd(c1);
				 else 
					line.append((char) (c1 & 0xFF));
				
				
				if(isPassword)
					srOut.write(ST & 0xFF);
				
				else if (c3 != ESC && c2 != ESC &&  c1 != ESC)
					srOut.write(c1 & 0xFF);
				
				c3 = c2; c2 = c1;
			}	
			
		} catch (IOException e) {
			LG.log(Level.SEVERE, " Exception:", e);
			return "";
		}
		
		return line.toString().trim();
	}
	
	protected StringBuilder getLastCmd(int c1) throws IOException {
				
		if (cmdIndex == -1) return new StringBuilder("");
		else if (c1 == UP && cmdIndex < cmdList.size()-1) cmdIndex++;		
		else if (c1 == UP) cmdIndex = 0;		
		else if (c1 == DOWN && cmdIndex > 0)   cmdIndex--;		
		else if (c1 == DOWN) cmdIndex = cmdList.size()-1;		
		
		String line = cmdList.get(cmdIndex);
		srOut.write(prompt);
		srOut.write(line.getBytes());
		
		return new StringBuilder(line);
	}
	
		
	public void go(String portname, int bRate,int bDataBits, String lgr, String logPath) {

		if(!"off".equals(lgr) && portname != null) 
		    setLogger(logPath + "//COM_" + (portname.replaceAll("/","-"))+".log", lgr);

		while (true) {
			try {
				startSession(portname, bRate, bDataBits);				
			} catch (Exception e) {LG.log(Level.SEVERE, " Exception:", e);

			} finally {
				try {srIn.close();}  catch (Exception e) {}
				try {srOut.close();} catch (Exception e) {}
			}

			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {}
		}
	} 


	protected boolean processCommand(String command)  {

		try {
			String opt = "";
			StringTokenizer st = new StringTokenizer(command.trim());
			int stCount = st.countTokens();
			ArrayList<String> cmd = new ArrayList<String>(stCount);

			while (st.hasMoreElements()) cmd.add(st.nextToken());

			if (stCount > 0) opt = cmd.get(0).toLowerCase().trim();

			if(stCount == 1 && StringUtils.isNumeric(command)) 
				return doConnect(Integer.parseInt(command));

			else if(stCount == 2 && "ssh".equals(opt)) 
				return doConnectSSH(cmd.get(1));

			else if("atz".equals(opt) || "cls".equals(opt))  		
				return doClear();

			else if(stCount == 2 && "prompt".equals(opt))  
				return doPromptSet(cmd.get(1));

			else if("?".equals(opt) || "help".equals(opt)) 		
				return doHelp();

			else if("getdtm".equals(opt)) 		
				return doServerDate();

			else if("gety2k".equals(opt)) 
				return doServerY2KDate();

			else if((  (stCount == 2 && "atd".equals(opt)) 
					|| (stCount == 2 && "bbs".equals(opt)))
					&&  cmd.get(1).contains(":") )  			
				return doConnectTelnet(cmd.get(1));

			else if((  (stCount == 3 && "atd".equals(opt)) 
					|| (stCount == 3 && "bbs".equals(opt)))
					&&  StringUtils.isNumeric(cmd.get(2)) )  			
				return doConnectTelnet(cmd.get(1), Integer.parseInt(cmd.get(2)));

			else if(stCount == 2 && "src".equals(opt))  
				return doSrcBBSListing(cmd.get(1).toLowerCase());			

			else if(stCount == 2 && StringUtils.isNumeric(cmd.get(1))
					&& "list".equals(opt))  		
				return doListBBSDirectory(Integer.parseInt(cmd.get(1)));

			else if(stCount == 4 && StringUtils.isNumeric(cmd.get(1)) 
					&& "save".equals(opt))  		
				return doSaveBBSDirectory(Integer.parseInt(cmd.get(1)), cmd.get(2), cmd.get(3));	

			else if("lsi".equals(opt))  
				return doListServerFiles(BatchStartUp.inbound);

			else if("lso".equals(opt))  
				return doListServerFiles(BatchStartUp.outbound);

			else if("ysend".equals(opt))  
				return doConnectYSND();

			else if("yrecv".equals(opt))  
				return doConnectYREC();

			else if(stCount == 2 && "xsend".equals(opt))  
				return doConnectXSND(cmd.get(1));

			else if(stCount == 2 && "xrecv".equals(opt)  )  
				return doConnectXREC(cmd.get(1));

		} catch (Exception e) {			
			try { LG.log(Level.SEVERE, " Exception:", e); } catch (Exception e1) {}
		}

		return false;
	}


	void  startSession(String portName, int bRate, int bDataBits) throws Exception {
		cmdList = new ArrayList<String>();
		cmdIndex = -1;
				
//   	portIdentifier = CommPortIdentifier.getPortIdentifier(portName);		
//	    if (portIdentifier.isCurrentlyOwned()) throw new IOException("Error - Serial port in use!");
//		CommPort commPort = portIdentifier.open(this.getClass().getName(), TO);
//		SerialPort serialPort = (SerialPort) commPort;		
//		serialPort.setSerialPortParams(
//				bRate,
//				SerialPort.DATABITS_8, 
//				SerialPort.STOPBITS_1,
//				SerialPort.PARITY_NONE);
		
		buildMenu();
		
		SerialPort commPort =  SerialPort.getCommPort(portName);
		commPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 0, 0);
		commPort.setComPortParameters(bRate, 
				bDataBits, 
				SerialPort.ONE_STOP_BIT, 
				SerialPort.NO_PARITY);
		
		commPort.openPort(300);
		
		srOut  = commPort.getOutputStream();
		srIn   = commPort.getInputStream();
		
		yModem = new YModem(srIn, srOut);
		xModem = new XModem(srIn, srOut);		
		
		srOut.write(CLEAR);
		srOut.write(header);

	LG.info(" -> Serial Modem Restarted: "+portName+ " @ "+bRate);
		 
		while (true) {
			if(disconnected) {								
				srOut.write(prompt);
				String cmd = getStringFromPort(false).trim();
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
	
	
	protected int  userExit() {			
		disconnected = true;
		esc = false;
		try { 
			srOut.write((CONEXIT).getBytes());
			srOut.flush();
			}   catch (Exception e) {}	
		try {if(socket  != null) socket.close();}         catch (Exception e) {}
		try {if(channel != null) channel.disconnect();}   catch (Exception e) {}
		try {if(session != null) session.disconnect();}   catch (Exception E) {}
		
		return -1;
	}

	protected int userPassword() throws IOException {
		socket.getOutputStream().write((char)0x08);
		socket.getOutputStream().write(bbsHost.password.getBytes());
		socket.getOutputStream().flush();
		
		return -1;
	}

	
	protected int userUserID() throws IOException {
		socket.getOutputStream().write((char)0x08);
		socket.getOutputStream().write(bbsHost.user.getBytes());
		socket.getOutputStream().flush();
		return -1;
	}
	
    public void setLogger(String logpath, String level) {
        LG.setUseParentHandlers(false);
        try {
                FileHandler fh = new FileHandler(logpath);
                LG.addHandler(fh);
                SimpleFormatter formatter = new SimpleFormatter();  
                fh.setFormatter(formatter);
                if ("FINE".equals(level))
                        LG.setLevel(Level.FINEST);

        } catch (SecurityException | IOException e) {
                e.printStackTrace();
        }
    }     
	
	public static void main(String[] args) { 
		(new SerialModem()).go("/dev/ttyUSB0",19200 ,8 ,"off" ,"~");
		}
	
}

