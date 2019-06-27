 Usage
 -----

 1- Purchase a USB to RS232 Adapter with PL2303 Chipset (on Amazon)
 2- Install java JVM on host
 3- run on linux as     $ ./usbModem.jar "-s=/dev/ttyUSB0" "-b=19200"
    where:
         -s   =  serial port of the adapter  
         -b   =  serial port baud rate


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
       ysend --> YMODEM batch dowload
       yrecv --> YMODEM batch upload 
       xsend (filename) --> XMODEM dowload
       xrecv (filename) --> XMODEM upload 
       

-------------------------------------------------------------


