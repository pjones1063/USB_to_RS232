
# USB_to_RS232 Connector (usbModem)
 
  
 ### Connect to any BBS or SSH host from any 8, 16 or 32 bit - Atari, Commodore or TI-99 computer using a USB to RS232 PL2303 cable

 ### Debian - Raspberry Pi Usage

 * Best run on a Raspberry Pi. When you order the USB/RS232 cable - buy a Pi as well!

 * Purchase a USB to RS232 Adapter with PL2303 Chipset (on Amazon)
 
 *  If using an Atari 850 interface see 850 Null Modem cable wiring: [Atari NullModem](http://atari.fox-1.nl/atari-400-800-xl-xe/400-800-xl-xe-tools/null-modem-cable-wiring/)
 	
 *  If using the Commodore User Port see GGLabs - User Port RS232 for Commodore 8-bit Computers: [User Port RS232 ](https://gglabs.us/node/680)	
    
 * Install [java 1.8 JVM](https://www.oracle.com/java/technologies/javase-downloads.html)
    * Windows and other OS's - see documentation 
    * On Raspberry Pi or other Linux
  
``` 
$ sudo apt-get install oracle-java8-jdk 
```
 
 * Install [librxtx-java API](https://packages.debian.org/search?keywords=librxtx-java)  
    * Windows and other OS's - see documentation 
    * On Raspberry Pi or other Linux
   
``` 
$ sudo apt install librxtx-java
```

       
 * Install [JSch API](http://www.jcraft.com/jsch/) 
    * Windows and other OS's - see documentation
    * On Raspberry Pi or other Linux
       
```       
$ sudo apt install libjsch-java  
```

 * Start the connector:

``` 
$ ./usbModem.jar "-s=/dev/ttyUSB0" "-b=19200"
```
       
   -s   =  serial port of the adapter  
   -b   =  serial port baud rate
 
 * Connect up the cable run some terminal 
    program - like TAZ, Bobterm, ICET, NovaTerm, StrikeTerm, etc... 
    
 * Set the baud rate is as above and bits/stop bit is 8/1
 

------------------------------------------------------------

 ###  Command Line Options and Usage

```

    *  -b,--baud <baud>           Serial Baud Rate
    *  -H,--remotehost <remhost>  TCP remote server mode host
    *  -i,--inboundfolder <in>    Inbound transfer folder path
    *  -l,--localport <localport> TCP server local mode port 
    *  -m,--menufile <menu>       Menu banner file path
    *  -o,--outboundfolder <out>  Outbound transfer folder path
    *  -P,--remoteport <remport>  TCP remote server mode port
    *  -s,--serialport <serial>   Serial Port Name
    *  -x,--xmlfile <xml>         XML BBS directory file path
    
```

------------------------------------------------------------

 ###  Configurtaion Files
 
  * dialdirectory.xml  - BBS directory  
    - used with -x option
    - Current xml at  https://www.telnetbbsguide.com/bbslist/ 
    -   * (remove all non-standard XML after download - i.e. All "&" chars)
    
    
  * banner.asc - Header and help file
    - used with -m option


------------------------------------------------------------

 ### Examples

 *  Start with serial usb device ttyUSB0 at 19200
 
 
```
$ usbModem.jar -s=/dev/ttyUSB0 -b=19200
```


 * Start with remote tcp (Altirra modem emulation)
 

```
$ usbModem.jar -H=192.168.0.100 -P=8080
```


 *  Start local tcp server
 
```
$ usbModem.jar -l=9090
```

------------------------------------------------------------


 ### usbModem Commands
 

*   help   --> Display help
   
*   nn [bbs_#]   --> Connect BBS by listing number
*   src [pattern*]   --> Search for BBS listing number
*   save [bbs_#] user password   --> Save BBS user/password
*   list [bbs_#] --> Display BBS  by listing number

*   atz   -->  Clear reset screen
*   atd hostname/IP port   --> TCP connect to host    
*   bbs hostname/IP port   --> TCP connect to host   
*   ssh user@hostname:port  -->  ssh to host   
*   cls   -->  Clear screen        
*   prompt value   --> Set command prompt   
*   ysend   --> YMODEM Batch download
*   yrecv   --> YMODEM Batch upload 
*   xsend filename   --> XMODEM Download
*   xrecv filename   --> XMODEM Upload 
*   lsi   -->  List inbound folder
*   lso   -->  List outbound folder
*   [esc] -    --> Exit to prompt
*   [esc] u    --> User macro
*   [esc] p    --> Password marco



-------------------------------------------------------------

 ### Esc Key Macro Shortcuts
 

 *  [esc] -    -  Exit to prompt
 *  [esc] u    -  User macro
 *  [esc] p    -  Password Marco


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
    
