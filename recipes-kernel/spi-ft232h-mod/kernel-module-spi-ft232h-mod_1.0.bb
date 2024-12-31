SUMMARY = "Teledatics' FTDI USB SPI driver"
SECTION = "kernel"
LICENSE = "GPL-2.0-only"
LIC_FILES_CHKSUM ?= "file://${COMMON_LICENSE_DIR}/GPL-2.0-only;md5=801f80980d171dd6425610833a22dbe6"

inherit module

# Dependencies
DEPENDS = "virtual/kernel"

SRCBRANCH = "main"
SRCREV = "af20260193cf2949df4ebcc4820d3cbfb8c5c80e"
SRC_URI = "git://github.com/teledatics/ftdi-spi-linux.git;protocol=https;branch=${SRCBRANCH}"

S = "${WORKDIR}/git"

EXTRA_OEMAKE = "KDIR=${STAGING_KERNEL_DIR} KDIR_CONFIG=${STAGING_KERNEL_BUILDDIR}"

python () {
    import os
    import bb
    
    # set misc variables to support module build
    ksrc = d.getVar('STAGING_KERNEL_DIR')
    kbuild = d.getVar('STAGING_KERNEL_BUILDDIR')
    kernel_version = d.getVar('KERNEL_VERSION')
    
    d.setVar('KDIR', ksrc)
    d.setVar('KDIR_CONFIG', kbuild)

    d.setVar("EXTRA_OEMAKE", f"KDIR={ksrc} KDIR_CONFIG={kbuild} KERNEL_VERSION={kernel_version}")

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
