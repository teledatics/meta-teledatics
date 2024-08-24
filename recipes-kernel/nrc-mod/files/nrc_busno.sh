#!/bin/bash

# Teledatics TD-XPAH spi bus number tool

set -e 
BUSNO=$(for i in $(ls /sys/class/spi_master/); do for j in $(ls /sys/class/spi_master/$i/); do if [ "$(basename $(readlink /sys/class/spi_master/$i/$j 2>/dev/null) 2>/dev/null)" == "spi-ft232h.0" ]; then echo $i | sed 's/[^0-9]*//g'; fi; done; done;)
SPI_BUS=${BUSNO:-0}
echo ${SPI_BUS}
