#! /bin/sh
rm /var/lock/LCK..ttyUSB1
/usr/local/bin/usbModem.jar "-s=/dev/ttyUSB1" "-b=2400" &
echo "$!" > /var/run/usbModem2.pid

