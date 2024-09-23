SUMMARY = "Teledatics' SPI driver for Newracom nrc7292 HaLow chip"
SECTION = "kernel"
LICENSE = "GPL-2.0-only"
LIC_FILES_CHKSUM ?= "file://${COMMON_LICENSE_DIR}/GPL-2.0-only;md5=801f80980d171dd6425610833a22dbe6"

inherit module

SRCBRANCH = "nrc-dkms-v1.2.2-rc1"
SRCREV = "549f0a8e414a5d55a9d1132a465a8ebf22ab69e3"
SRC_URI = "git://github.com/teledatics/nrc7394_sw_pkg.git;protocol=https;branch=${SRCBRANCH}"

S = "${WORKDIR}/git/package/src/nrc"

EXTRA_OEMAKE = "KDIR=${STAGING_KERNEL_DIR} KDIR_CONFIG=${STAGING_KERNEL_BUILDDIR}"

do_install() {
    make -C ${STAGING_KERNEL_DIR} M=${S} INSTALL_MOD_PATH=${D} modules_install
}

RPROVIDES_${PN} += "${PN}"

# add helper scripts and modprobe conf file
FILESEXTRAPATHS:prepend := "${THISDIR}/files:"
SRC_URI += "file://nrc.conf"
SRC_URI += "file://nrc_load_module.sh"
SRC_URI += "file://nrc_busno.sh"
SRC_URI += "file://nrc_gpiono.sh"

FILES:${PN} += "${sysconfdir}/modprobe.d/* ${bindir}/* ${base_libdir}/firmware/*"

do_install:append() {
    install -d ${D}${sysconfdir}/modprobe.d/
    install -m 0644 ${WORKDIR}/nrc.conf ${D}${sysconfdir}/modprobe.d/
    install -d ${D}${bindir}
    install -m 0755 ${WORKDIR}/nrc_load_module.sh ${D}${bindir}
    install -m 0755 ${WORKDIR}/nrc_busno.sh ${D}${bindir}
    install -m 0755 ${WORKDIR}/nrc_gpiono.sh ${D}${bindir}
    install -d ${D}${base_libdir}/firmware/
    install -m 644 ${S}/../../evk/binary/nrc7394_cspi.bin ${D}${base_libdir}/firmware/
    install -m 644 ${S}/../../evk/binary/nrc7394_bd.dat ${D}${base_libdir}/firmware/
}
