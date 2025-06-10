SUMMARY = "Teledatics' SPI driver for Newracom nrc7292 HaLow chip"
SECTION = "kernel"
LICENSE = "GPL-2.0-only"
LIC_FILES_CHKSUM ?= "file://${COMMON_LICENSE_DIR}/GPL-2.0-only;md5=801f80980d171dd6425610833a22dbe6"

inherit module

SRCBRANCH = "kernel_6_8_support"
SRCREV = "0ec14a27ade38e50059f71da10f72215d41f09a0"
SRC_URI = "git://github.com/teledatics/nrc7394_sw_pkg.git;protocol=https;branch=${SRCBRANCH}"

S = "${WORKDIR}/git/package/src/nrc"

EXTRA_OEMAKE = "KDIR=${STAGING_KERNEL_DIR} KDIR_CONFIG=${STAGING_KERNEL_BUILDDIR}  EXTRA_CFLAGS='-DCONFIG_MAC80211_MESH'"

FILES:${PN} += "${nonarch_base_libdir}/modules/${KERNEL_VERSION}/extra/*.ko"

RPROVIDES:${PN} = "kernel-module-nrc-${KERNEL_VERSION}"
RDEPENDS:${PN} += "kernel-${KERNEL_VERSION}"

KERNEL_MODULE_AUTOLOAD += "mac80211"

do_compile:prepend() {
    sed -i 's/^\(#define CONFIG_SPI_USE_DT\)/\/\/\1/' ${S}/nrc-build-config.h
}

do_install() {
    make -C ${STAGING_KERNEL_DIR} M=${S} INSTALL_MOD_PATH=${D} INSTALL_MOD_DIR=extra modules_install

    if [ "${nonarch_base_libdir}" != "/lib" ]; then
        install -d ${D}${nonarch_base_libdir}/modules/${KERNEL_VERSION}/extra
        mv ${D}/lib/modules/${KERNEL_VERSION}/extra/*.ko ${D}${nonarch_base_libdir}/modules/${KERNEL_VERSION}/extra/
        rm -rf ${D}/lib
    fi
}

#RPROVIDES_${PN} += "${PN}"

# add helper scripts and modprobe conf file
FILESEXTRAPATHS:prepend := "${THISDIR}/files:"
SRC_URI += "file://nrc.conf"
SRC_URI += "file://nrc_load_module.sh"
SRC_URI += "file://nrc_busno.sh"
SRC_URI += "file://nrc_gpiono.sh"

FILES:${PN} += "${sysconfdir}/modprobe.d/* ${bindir}/* ${base_libdir}/firmware/* ${sysconfdir}/modules-load.d"

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
# load mac80211 if it is compiled as a module
    install -d ${D}${sysconfdir}/modules-load.d
    echo "mac80211" > ${D}${sysconfdir}/modules-load.d/mac80211.conf
}
