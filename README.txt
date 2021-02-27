USB_to_RS232 Connector - Set-Up
-------------------------------


-Best run on a Raspberry Pi. When you order the USB/RS232 cable - buy a Pi as well!

-Purchase a USB to RS232 Adapter with PL2303 Chipset (on Amazon)

-If using an 850 interface - build db9 null modem adapter. 
     See nullmodem-atari850-db9-db9.png for pin layout.

-Install java JVM -> https://www.oracle.com/java/technologies/javase-downloads.html

-Install the librxtx-java API -> from http://rxtx.qbang.org/wiki/index.php/Main_Page

-Install the JSch API -> from http://www.jcraft.com/jsch/

-run as -> $ ./usbModem.jar "-s=/dev/ttyUSB0" "-b=19200" where:

   -s = serial port of the adapter
   -b = serial port baud rate

-Connect up the cable to an Atari and run some terminal program - like TAZ or Bobterm -ensure the baud rate is as above and bits/stop bit is 8/1

-Good Luck!



------------------------------------------------------------
 

 Command Line Options and Usage
 ------------------------------

 $ ./usmModem.jar

===========================
RS232_2_USB Connector Usage
===========================

usage: usbModem.jar
Parms
 -b,--baud <baud>             Serial Baud Rate
 -H,--remotehost <remhost>    TCP remote server mode - host
 -i,--inboundfolder <in>      Inbound transfer folder path
 -l,--localport <localport>   TCP server local mode - port number
 -m,--menufile <menu>         Menu-banner file path
 -o,--outboundfolder <out>    Outbound transfer folder path
 -P,--remoteport <remport>    TCP remote server mode - port number
 -s,--serialport <serial>     Serial Port Name
 -x,--xmlfile <xml>           XML BBS directory file path


 Examples:

 - Start with serial usb device ttyUSB0 at 19200
                $ usbModem.jar -s=/dev/ttyUSB0 -b=19200

 - Start with remote tcp (Altirra modem emulation)
                $ usbModem.jar -H=192.168.0.100 -P=8080

 - Start local tcp server
                $ usbModem.jar -l=9090

				
------------------------------------------------------------				

 Configurtaion Files:
 
  * dialdirectory.xml  - BBS directory  
    - used with -x option
    - Current xml at  https://www.telnetbbsguide.com/bbslist/ 
      (remove all non-standard XML after download - i.e. All "&" chars)
    
    
  * banner.asc - Header and help file
    - used with -m option
				

------------------------------------------------------------

 usbModem Commands
 -----------------
 
   help   --> Display help
   
   nn [bbs_#]   --> Connect BBS by listing number
   src [pattern*]   --> Search for BBS listing number
   save [bbs_#] user password   --> Save BBS user/password
   list [bbs_#] --> Display BBS  by listing number
   
   atz   -->  Clear reset screen
   atd hostname/IP port   --> TCP connect to host    
   bbs hostname/IP port   --> TCP connect to host   
   ssh user@hostname:port  -->  ssh to host   
   
   cls   -->  Clear screen        
   prompt value   --> Set command prompt
   
   ysend   --> YMODEM Batch download
   yrecv   --> YMODEM Batch upload 
   xsend filename   --> XMODEM Download
   xrecv filename   --> XMODEM Upload 
   lsi   -->  List inbound folder
   lso   -->  List outbound folder
   
   [esc] -    --> Exit to prompt
   [esc] u    --> User macro
   [esc] p    --> Password marco


 Esc Key Macro Shortcuts
 -----------------------
       
   [esc] -    --> Exit to prompt
   [esc] u    --> User macro
   [esc] p    --> Password Marco

-------------------------------------------------------------


