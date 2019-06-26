#! /bin/sh
rm /var/lock/LCK..ttyUSB2
/usr/local/bin/usbModem.jar "-s=/dev/ttyUSB2" "-b=19200" "-l=9090"  &
echo "$!" > /var/run/usbModem1.pid

rm /var/lock/LCK..ttyUSB1
/usr/local/bin/usbModem.jar "-s=/dev/ttyUSB1" "-b=2400" &
echo "$!" > /var/run/usbModem2.pid


