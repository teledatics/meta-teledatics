#!/bin/bash 

# Teledatics spi_ft232h & nrc module load helper script

SPI_BUS_NO=`nrc_busno.sh`
SPI_GPIO_NO=`nrc_gpiono.sh`
MOD_PATH="/lib/modules/`uname -r`/extra"
MOD_NAME="nrc"
MOD_PATH_NAME=`ls ${MOD_PATH}/${MOD_NAME}*`

# exit if FTDI USB-SPI module not loaded
# if [ "${SPI_GPIO_NO}" == "-1" ]; then
# 	exit -1;
# fi

# exit if nrc module already loaded
if lsmod | grep -Eq "^${MOD_NAME} "; then
	exit -1;
fi

HIF_SPEED=100000000

insmod ${MOD_PATH_NAME} fw_name=nrc7394_cspi.bin bd_name=nrc7394_bd.dat spi_bus_num=${SPI_BUS_NO} spi_cs_num=0 spi_gpio_irq=-1 spi_polling_interval=5 hifspeed=${HIF_SPEED}

# wait until module is loaded
while ! lsmod | grep -Eq "^${MOD_NAME} "; do
        sleep 1;
done

/bin/ifconfig wlan1 up

# fix for endless deep sleep
/usr/bin/cli_app gpio write 16 1

# set TX power to near maximum
/usr/bin/cli_app set txpwr limit 28
