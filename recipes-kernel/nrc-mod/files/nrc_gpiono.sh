#!/bin/bash

# Teledatics TD-XPAH gpio number tool

GPIO=$(for i in $(ls /sys/bus/usb/devices/); do for j in $(ls /sys/bus/usb/devices/$i/); do if [ "$j" == "spi-ft232h.0" ]; then echo $(basename /sys/bus/usb/devices/$i/gpiochip* | sed 's/[^0-9]*//g'); fi; done; done)
GPIO=$(echo "($GPIO - 1) * 32" | bc)
GPIO_NO=${GPIO:--1}
# FIXME - gpio sysfs removed from kernel, hardcode for now, fix later
GPIO_NO=492
echo ${GPIO_NO}
