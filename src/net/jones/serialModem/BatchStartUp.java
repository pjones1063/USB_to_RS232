package net.jones.serialModem;

import java.io.PrintWriter;
import java.text.BreakIterator;
import java.util.Date;

import net.jones.serialModem.modem.SerialModem;
import net.jones.serialModem.modem.SocketModem;
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
	private static int pClient = -1;
	private static int pServer = -1;
	
	public static void main(String[] args) {

		final Options options = new Options();
		
		options.addOption( 
				Option.builder("s").argName("serial").longOpt("serial name").required(true)
				.hasArg(true).numberOfArgs(1).desc("Serial Port Name").build());
		
		options.addOption( 
				Option.builder("b").argName("baud").longOpt("baud").required(true)
				.hasArg(true).numberOfArgs(1).desc("Serial Baud Rate").build());

		options.addOption( 
				Option.builder("l").argName("listener").longOpt("listener").required(false)
				.hasArg(true).numberOfArgs(1).desc("listener port number").build());

		options.addOption( 
				Option.builder("c").argName("client").longOpt("client").required(false)
				.hasArg(true).numberOfArgs(1).desc("client port number").build());

		try {
			
			 CommandLineParser cmdLineParser = new DefaultParser();  
			 CommandLine commandLine  = cmdLineParser.parse(options, args);

			 portName = commandLine.getOptionValue('s');		
			 bRate =  Integer.parseInt(commandLine.getOptionValue('b'));
			 String sv = commandLine.getOptionValue('l');
			 String cl = commandLine.getOptionValue('c');

			if(StringUtils.isNumeric(sv))  pServer = Integer.parseInt(sv);
			if(StringUtils.isNumeric(cl))  pClient = Integer.parseInt(cl);


			printUsage(options);

			(new Thread(new Runnable() {
				@Override
				public void run() {
					(new SerialModem()).go(portName,bRate);
				}
			})).start();

			if(pServer > 10)
				(new Thread(new Runnable() {
					@Override
					public void run() {
						(new SocketServerModem()).go(pServer);
					}
				})).start();


			if(pClient > 10)
				(new Thread(new Runnable() {
					@Override
					public void run() {
						(new SocketModem()).go(pClient);
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
			"\n \n Examples: " +
			"\n   $ usbModem.jar -s=/dev/ttyUSB0 -b=19200 -i=9090 -c=9091" +
	        "\n   C:> usbModem.jar -serial=COM1 -baud=2400 \n \n");
		
		pw.flush();  
	} 


}
