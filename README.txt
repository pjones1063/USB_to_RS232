 Usage
 -----

 1- Purchase a USB to RS232 Adapter with PL2303 Chipset (on Amazon)
 2- Install java JVM on host PC
 3- run - C:> usbModem.jar -serial=COM1 -baud=2400
    where:
         -serial = serial port of the adapter  
         -baud   = serial port baud rate


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


