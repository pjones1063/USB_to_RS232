
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
 


*  Good Luck!





------------------------------------------------------------

 ###  Command Line Options and Usage
 

 * $ ./usmModem.jar


 *  -b,--baud <baud>             Serial Baud Rate
 *  -H,--remotehost <remhost>    TCP remote server mode - host
 *  -i,--inboundfolder <in>      Inbound transfer folder path
 *  -l,--localport <localport>   TCP server local mode - port number
 *  -m,--menufile <menu>         Menu-banner file path
 *  -o,--outboundfolder <out>    Outbound transfer folder path
 *  -P,--remoteport <remport>    TCP remote server mode - port number
 *  -s,--serialport <serial>     Serial Port Name
 *  -x,--xmlfile <xml>           XML BBS directory file path



 #### Examples

 *  Start with serial usb device ttyUSB0 at 19200
 -               $ usbModem.jar -s=/dev/ttyUSB0 -b=19200

 * Start with remote tcp (Altirra modem emulation)
 -               $ usbModem.jar -H=192.168.0.100 -P=8080

 *  Start local tcp server
 -               $ usbModem.jar -l=9090


------------------------------------------------------------


 ### usbModem Commands
 

 *  ? -->  help
 *  000 (bbs #) --> Connect BBS by listing number
 *  src (pattern) --> Search BBS listing
 *  save 000 (bbs #) (user ID) (password) --> save user
 *  atz -->  Clear & Display Menu
 *  atd (hostname) (port) --> TCP connect to host
 *  bbs (hostname) (port) --> TCP connect to host
 *  cls -->  Clear & Display Menu
 *  lsi --> list inbound folder
 *  lso --> list outbound folder
 *  ssh (user@hostname) -->  ssh to host
 *  ysend --> YMODEM batch dowload
 *  yrecv --> YMODEM batch upload
 *  xsend (filename) --> XMODEM dowload
 *  xrecv (filename) --> XMODEM upload


-------------------------------------------------------------

 ### Esc Key Macro Shortcuts
 

 *  [esc] -    --> Exit to prompt
 *  [esc] u    --> User macro
 *  [esc] p    --> Password Marco


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
    
