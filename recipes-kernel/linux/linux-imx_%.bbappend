FILESEXTRAPATHS:prepend := "${THISDIR}/files:"

SRC_URI += "file://enable-ftdi-sio.cfg"

KCONFIG_MODE = "--alldefconfig"
