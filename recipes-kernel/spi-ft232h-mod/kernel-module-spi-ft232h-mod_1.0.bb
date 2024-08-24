SUMMARY = "Teledatics' FTDI USB SPI driver"
SECTION = "kernel"
LICENSE = "GPL-2.0-only"
LIC_FILES_CHKSUM ?= "file://${COMMON_LICENSE_DIR}/GPL-2.0-only;md5=801f80980d171dd6425610833a22dbe6"

inherit module

# Dependencies
DEPENDS = "virtual/kernel"

SRCBRANCH = "main"
SRCREV = "7cb17909ecd7b7bef82096d03eabbfc1be2e2b32"
SRC_URI = "git://github.com/teledatics/ftdi-spi-linux.git;protocol=https;branch=${SRCBRANCH}"

S = "${WORKDIR}/git"

EXTRA_OEMAKE = "KDIR=${STAGING_KERNEL_DIR}"

do_configure:prepend() {
    touch ${STAGING_KERNEL_DIR}/.config
}

do_install() {
    make -C ${STAGING_KERNEL_DIR} M=${S} INSTALL_MOD_PATH=${D} modules_install
}

RPROVIDES_${PN} += "${PN}"

# add helper scripts and modprobe conf file
FILESEXTRAPATHS:prepend := "${THISDIR}/files:"
SRC_URI += "file://spi_ft232h.conf"
SRC_URI += "file://blacklist-ftdi_sio.conf"

FILES:${PN} += "${sysconfdir}/modprobe.d/*"

# NOTE: need to blacklist ftdi_sio module
do_install:append() {
    install -d ${D}${sysconfdir}/modprobe.d/
    install -m 0644 ${WORKDIR}/spi_ft232h.conf ${D}${sysconfdir}/modprobe.d/
    install -m 0644 ${WORKDIR}/blacklist-ftdi_sio.conf ${D}${sysconfdir}/modprobe.d/
}
