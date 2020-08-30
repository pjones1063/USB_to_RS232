package net.jones.serialModem;

import java.io.PrintWriter;

import net.jones.serialModem.modem.SerialModem;
import net.jones.serialModem.modem.RemoteSocketModem;
import net.jones.serialModem.modem.SocketServerModem;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.StringUtils;

public class BatchStartUp {

	private static String portName = null;		
	private static int bRate   = -1;
	private static int pPort = -1;
	private static int pRemPort = -1;
	private static String  pRemHost = null;

	public  static String splush    = null;
	public  static String  dialxml   = null;
	public  static String inbound   = null;
	public  static String outbound  = null;	

	
	public static void main(String[] args) {

		final Options options = new Options();
		
		options.addOption( 
				Option.builder("s").argName("serial").longOpt("serialport").required(false)
				.hasArg(true).numberOfArgs(1).desc("Serial Port Name").build());
		
		options.addOption( 
				Option.builder("b").argName("baud").longOpt("baud").required(false)
				.hasArg(true).numberOfArgs(1).desc("Serial Baud Rate").build());

		options.addOption( 
				Option.builder("P").argName("remport").longOpt("remoteport").required(false)
				.hasArg(true).numberOfArgs(1).desc("TCP remote server mode - port number").build());
		options.addOption( 
				Option.builder("H").argName("remhost").longOpt("remotehost").required(false)
				.hasArg(true).numberOfArgs(1).desc("TCP remote server mode - host").build());
		
		options.addOption( 
				Option.builder("l").argName("localport").longOpt("localport").required(false)
				.hasArg(true).numberOfArgs(1).desc("TCP server local mode - port number").build());

		options.addOption( 
				Option.builder("m").argName("menu").longOpt("menufile").required(false)
				.hasArg(true).numberOfArgs(1).desc("Menu-banner file path").build());
		
		options.addOption( 
				Option.builder("x").argName("xml").longOpt("xmlfile").required(false)
				.hasArg(true).numberOfArgs(1).desc("XML BBS directory file path").build());
		
		options.addOption( 
				Option.builder("i").argName("in").longOpt("inboundfolder").required(false)
				.hasArg(true).numberOfArgs(1).desc("Inbound transfer folder path").build());
		
		options.addOption( 
				Option.builder("o").argName("out").longOpt("outboundfolder").required(false)
				.hasArg(true).numberOfArgs(1).desc("Outbound transfer folder path").build());

		try {

			CommandLineParser cmdLineParser = new DefaultParser();  
			CommandLine commandLine  = cmdLineParser.parse(options, args);
			
			splush   = commandLine.getOptionValue('m',"/home/pi/banner.asc");
			dialxml  = commandLine.getOptionValue('x',"/home/pi/dialdirectory.xml");
			inbound  = commandLine.getOptionValue('i',"/home/pi/Transfer/inbound");
			outbound = commandLine.getOptionValue('o',"/home/pi/Transfer/outbound");
			pRemHost = commandLine.getOptionValue('H',"localhost");
			portName = commandLine.getOptionValue('s');
					
			String br = commandLine.getOptionValue('b');
			String sv = commandLine.getOptionValue('P');
			String pr = commandLine.getOptionValue('l');

			if(StringUtils.isNumeric(br))  bRate   = Integer.parseInt(br);
			if(StringUtils.isNumeric(sv))  pRemPort = Integer.parseInt(sv);
			if(StringUtils.isNumeric(pr))  pPort = Integer.parseInt(pr);
			
			printUsage(options);

			if(bRate > 10)
			(new Thread(new Runnable() {
				@Override
				public void run() {
					(new SerialModem()).go(portName,bRate);
				}
			})).start();

			if(pPort > 10)
				(new Thread(new Runnable() {
					@Override
					public void run() {
						(new SocketServerModem()).go(pPort);
					}
				})).start();

			if(pRemPort > 10)
				(new Thread(new Runnable() {
					@Override
					public void run() {
						(new RemoteSocketModem()).go(pRemHost,pRemPort);
					}
				})).start();


		} catch (ParseException e) {
			printUsage(options);
		}  

	}	

	
	private static void printUsage(final Options options) {  
		System.out.println("\n=======================");  
		System.out.println(  "Atari Serial Comm Usage");  
		System.out.println(  "=======================\n");

		final HelpFormatter formatter = new HelpFormatter();  
		final String syntax = "usbModem.jar  ";  
		final PrintWriter pw  = new PrintWriter(System.out);  

		formatter.printHelp(syntax, "Parms", options, 
			"\n \n Examples: \n " +
			"\n - Connect to serial usb device ttyUSB0 at 19200 " +
			"\n                $ usbModem.jar -s=/dev/ttyUSB0 -b=19200 \n"  +
			"\n - Connect to remote tcp (Altirra modem emulation)" +
			"\n                $ usbModem.jar -H=192.168.0.100 -P=8080 \n"  +			 
			"\n - Start local tcp server" +
			"\n                $ usbModem.jar -l=9090 \n \n");
		
		pw.flush();  
	} 


}
