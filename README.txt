 Lunix/Raspberry Pi Usage
 ------------------------


-Best run on a Raspberry Pi. When you order the USB/RS232 cable - buy a Pi as well!

-Purchase a USB to RS232 Adapter with PL2303 Chipset (on Amazon)

-If using an 850 interface - build db9 null modem adapter. 
     See nullmodem-atari850-db9-db9.png for pin layout.

-Install java JVM on linux and/or Pi host

-Install the librxtx-java API -> from http://rxtx.qbang.org/wiki/index.php/Main_Page

-Install the JSch API -> from http://www.jcraft.com/jsch/

-run on Pi/Linux as -> $ ./usbModem.jar "-s=/dev/ttyUSB0" "-b=19200" where:

   -s = serial port of the adapter
   -b = serial port baud rate

-Connect up the cable to an Atari and run some terminal program - like TAZ or Bobterm -ensure the baud rate is as above and bits/stop bit is 8/1

-File banner.asc can be placed in folder '/home/pi/' - this can be changed in src file - src/net/jones/serialModem/modem/SerialModem.java to whatever path you like. Recompile and package jar.

-Folders /home/pi/Transfer/inbound & outbound can also be changed in src file - src/net/jones/serialModem/modem/SerialModem.java as the YMODEM - send and recv folder. Recompile and package jar



-Good Luck!

------------------------------------------------------------

 Commands
 ---------

       ? -->  help
       atz -->  Clear & Display Menu
       atd (hostname) (port) --> TCP connect to host    
       bbs (hostname) (port) --> TCP connect to host   
       cls -->  Clear & Display Menu       
       lsi --> list inbound folder
       lso --> list outbound folder
       ssh (user@hostname) -->  ssh to host 
       ysend --> YMODEM batch download
       yrecv --> YMODEM batch upload 
       xsend (filename) --> XMODEM download
       xrecv (filename) --> XMODEM upload 
       timmer --> set on/off inactive timer

-------------------------------------------------------------


