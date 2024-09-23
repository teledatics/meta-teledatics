SUMMARY = "Teledatics' SPI driver for Newracom nrc7292 HaLow chip"
SECTION = "kernel"
LICENSE = "GPL-2.0-only"
LIC_FILES_CHKSUM ?= "file://${COMMON_LICENSE_DIR}/GPL-2.0-only;md5=801f80980d171dd6425610833a22dbe6"

inherit module

# find RADIO-VERSION from meta-summit-radio .inc file and copy the backports Module.symver to the working directory
python () {
    import os
    import bb

    # Retrieve all layer paths from BBLAYERS
    layers = d.getVar('BBLAYERS').split()

    # Initialize variable to store the path to meta-summit-radio
    meta_summit_radio_path = None

    # Iterate through layers to find meta-summit-radio
    for layer in layers:
        if "meta-summit-radio" in layer:
            meta_summit_radio_path = layer
            break

    if not meta_summit_radio_path:
        bb.fatal("meta-summit-radio layer not found in BBLAYERS. Please ensure it is included in your build configuration.")

    # Define the relative path to the radio-stack-bdsdmac-hashes.inc file
    include_file_rel = "../radio-stack-bdsdmac-hashes.inc"
    include_file = os.path.join(meta_summit_radio_path, include_file_rel)

    if not os.path.exists(include_file):
        bb.fatal(f"Include file for RADIO_VERSION not found at: {include_file}")

    # Initialize RADIO_VERSION
    radio_version = None

    # Open and parse the include file to extract RADIO_VERSION
    with open(include_file, 'r') as f:
        for line in f:
            line = line.strip()
            if line.startswith("RADIO_VERSION"):
                # Expecting a line like: RADIO_VERSION = "1.2.3"
                parts = line.split("=")
                if len(parts) >= 2:
                    radio_version = parts[1].strip().strip('"')
                    break

    if not radio_version:
        bb.fatal("RADIO_VERSION not defined in the include file.")

    # Set RADIO_VERSION for use in the recipe
    d.setVar("RADIO_VERSION", radio_version)
    bb.note(f"RADIO_VERSION set to: {radio_version}")
}

DEPENDS = "virtual/kernel"

SRCBRANCH = "nrc-dkms-v1.2.2-rc1"
SRCREV = "d7a3b5370fe4b0fbf8c9e296d43e7813d8347ae2"
SRC_URI = "git://github.com/teledatics/nrc7394_sw_pkg.git;protocol=https;branch=${SRCBRANCH}"

S = "${WORKDIR}/git/package/src/nrc"

BACKPORT_DIR = "${WORKDIR}/../../kernel-module-bdsdmac-backports/${RADIO_VERSION}-r0/laird-backport-${RADIO_VERSION}"
MODULES_MODULE_SYMVERS_LOCATION := "${BACKPORT_DIR}"
EXTRA_OEMAKE = "KDIR=${STAGING_KERNEL_DIR} KDIR_CONFIG=${STAGING_KERNEL_BUILDDIR}"
EXTRA_OEMAKE += "EXTRA_CFLAGS=-I${BACKPORT_DIR}/backport-include -I${BACKPORT_DIR}/include"
EXTRA_OEMAKE += "KBUILD_EXTRA_SYMBOLS=${KBUILD_EXTRA_SYMBOLS}"

RPROVIDES_${PN} += "${PN}"
RDEPENDS_${PN} += "kernel-module-bdsdmac-backports"

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
