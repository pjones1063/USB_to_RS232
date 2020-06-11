
# Atari RS232 To Linux (or Windows) USB Adapter
 
  
 ### Connect to any BBS or SSH host from any 8 or 16 bit Atari computer using an RS232 to USB PL2303 cable

 ### Debian - Raspberry Pi Usage

 * Best run on a Raspberry Pi. When you order the USB/RS232 cable - buy a Pi as well!

 * Purchase a USB to RS232 Adapter with PL2303 Chipset (on Amazon)
 
 * If using an 850 interface - build db9 null modem adapter. See nullmodem-atari850-db9-db9.png for pin layout.
  
 * Install java JVM   
       On Raspberry Pi/Debian run  $ sudo apt-get install oracle-java8-jdk  
 
 * Install the librxtx-java API ->  http://rxtx.qbang.org/wiki/index.php/Main_Page  
       On Raspberry Pi/Debian run  $ sudo apt install librxtx-java  
 
 * Install the JSch API ->   http://www.jcraft.com/jsch/  
       On Raspberry Pi/Debian run  $ sudo apt install libjsch-java  
 
 * Run the app:    $ ./usbModem.jar "-s=/dev/ttyUSB0" "-b=19200"
    where:  
    
     -s   =  serial port of the adapter  
     -b   =  serial port baud rate
 
 * Connect up the cable to an Atari and run some terminal 
    program - like TAZ or Bobterm
    -ensure the baud rate is as above and bits/stop bit is 8/1
 
    File 'banner.asc' and 'dialdirectory.xml' can be placed in folder
    '/home/pi/' - this can be changed in src file -
    src/net/jones/serialModem/modem/SerialModem.java
    to whatever path you like.  Recompile and package jar.
    
    Folders /home/pi/Transfer/inbound & outbound can also be changed
    in src file -
    src/net/jones/serialModem/modem/SerialModem.java
    as the YMODEM - send and recv folder.  Recompile and package jar


*  Good Luck!

------------------------------------------------------------

 ### Commands
  

   *    ? -->  help
   *    atz -->  Clear & Display Menu
   *    atd (hostname) (port) --> TCP connect to host    
   *    bbs (hostname) (port) --> TCP connect to host   
   *    cls -->  Clear & Display Menu       
   *    lsi --> list inbound folder
   *    lso --> list outbound folder
   *    ssh (user@hostname) -->  ssh to host 
   *    ysend --> YMODEM batch download
   *    yrecv --> YMODEM batch upload 
   *    xsend (filename) --> XMODEM download
   *    xrecv (filename) --> XMODEM upload 
   *    timer --> set on/off inactive timer

-------------------------------------------------------------

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/
    
