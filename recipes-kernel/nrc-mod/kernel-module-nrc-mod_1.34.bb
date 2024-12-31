SUMMARY = "Teledatics' driver for Newracom nrc7394 HaLow chip"
SECTION = "kernel"
LICENSE = "GPL-2.0-only"
LIC_FILES_CHKSUM ?= "file://${COMMON_LICENSE_DIR}/GPL-2.0-only;md5=801f80980d171dd6425610833a22dbe6"

inherit module linux-kernel-base

# prevent automatic DEPENDS generation
KERNEL_SPLIT_MODULES = "0"

DEPENDS = "virtual/kernel "
# RDEPENDS:${PN} = "kernel (=6.1.22+git0+6cefd7849f-r0)"

# Wait for backports and related drivers to be built first
do_configure[depends] = "kernel-module-bdsdmac-backports:do_install"

SRCBRANCH = "nrc-dkms-v1.2.2-rc1"
SRCREV = "69ec23c1158d9e4f8f5a6b71572b59af3943bcb6"
SRC_URI = "git://github.com/teledatics/nrc7394_sw_pkg.git;protocol=https;branch=${SRCBRANCH}"

S = "${WORKDIR}/git/package/src/nrc"
B = "${STAGING_KERNEL_BUILDDIR}"

BACKPORTS_PN = "kernel-module-bdsdmac-backports"
BACKPORTS_PR = "r0"

KERNEL_VERSION = "${@get_kernelversion_headers('${B}')}"

RPROVIDES:${PN} += "${PN}"
# RDEPENDS:${PN} = "kernel (=${KERNEL_VERSION})"
# RDEPENDS:${PN} += "kernel-module-bdsdmac-backports"

python do_configure_backports() {
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

    bb.note(f"Backports radio version is: {radio_version}")

    tmp_dir = d.getVar('TMPDIR')
    tgt_sys = d.getVar('MULTIMACH_TARGET_SYS')
    backports_pn = d.getVar('BACKPORTS_PN')
    backports_pr = d.getVar('BACKPORTS_PR')

    backports_dir = f"{tmp_dir}/work/{tgt_sys}/{backports_pn}/{radio_version}-{backports_pr}/laird-backport-{radio_version}"
    
    bb.note("Backports directory at: %s" % backports_dir)

    if not os.path.isdir(backports_dir):
        bb.fatal("Backports directory is not found where expected.")

    d.setVar("BACKPORTS_DIR", backports_dir)

    ksrc = d.getVar('STAGING_KERNEL_DIR')
    kbuild = d.getVar('STAGING_KERNEL_BUILDDIR')
    kernel_version = d.getVar('KERNEL_VERSION')

    d.setVar('KDIR', ksrc)
    d.setVar('KDIR_CONFIG', kbuild)
    
    d.setVar('KERNEL_SPLIT_MODULES', '0')

    d.setVar("EXTRA_OEMAKE", f"KDIR={ksrc} KDIR_CONFIG={kbuild} KERNEL_VERSION={kernel_version}")
    d.appendVar("EXTRA_OEMAKE", f" EXTRA_CFLAGS=-I{backports_dir}/backport-include -I{backports_dir}/include")
    d.appendVar("EXTRA_OEMAKE", f" EXTRA_SYMVERS={backports_dir}/Module.symvers")
                             
    bb.note(f"Updated EXTRA_OEMAKE: {d.getVar('EXTRA_OEMAKE', True)}")

}

do_configure[prefuncs] += "do_configure_backports"
do_compile[prefuncs] += "do_configure_backports"
do_install[prefuncs] += "do_configure_backports"

# Add helper scripts and modprobe conf file
FILESEXTRAPATHS:prepend := "${THISDIR}/files:"
SRC_URI += "file://nrc.conf"
SRC_URI += "file://nrc_load_module.sh"
SRC_URI += "file://nrc_busno.sh"
SRC_URI += "file://nrc_gpiono.sh"

# FILES:${PN} += "${sysconfdir}/modprobe.d/* ${bindir}/* ${base_libdir}/firmware/*"

FILES:${PN} += "${sysconfdir}/modprobe.d/* ${bindir}/* ${base_libdir}/firmware/* ${base_libdir}/modules/${KERNEL_VERSION}/extra/nrc.ko"
# PACKAGES += "${PN}"
# Assign files to packages
# FILES:${PN}-config   = "${sysconfdir}/modprobe.d/*"
# PACKAGES += "${PN}-config"
# FILES:${PN}-scripts  = "${bindir}/*"
# PACKAGES += "${PN}-scripts"
# FILES:${PN}-firmware = "${base_libdir}/firmware/*"
# PACKAGES += "${PN}-firmware"
# FILES:${PN}-module   = "${base_libdir}/modules/${KERNEL_VERSION}/extra/nrc.ko"
# PACKAGES += "${PN}-module"

do_compile() {
    oe_runmake -C ${S}
}

# do_install() {
#     make -C ${S} M=${S} INSTALL_MOD_PATH=${D} modules_install
# }

do_install() {

    # Install the module
    install -d ${D}${base_libdir}/modules/${KERNEL_VERSION}/extra
    install -m 0644 ${S}/nrc.ko ${D}${base_libdir}/modules/${KERNEL_VERSION}/extra/

    # Install the modprobe configuration file
    install -d ${D}${sysconfdir}/modprobe.d/
    install -m 0644 ${WORKDIR}/nrc.conf ${D}${sysconfdir}/modprobe.d/
    
    # Install helper scripts
    install -d ${D}${bindir}
    install -m 0755 ${WORKDIR}/nrc_load_module.sh ${D}${bindir}
    install -m 0755 ${WORKDIR}/nrc_busno.sh ${D}${bindir}
    install -m 0755 ${WORKDIR}/nrc_gpiono.sh ${D}${bindir}
    
    # Install firmware files
    install -d ${D}${base_libdir}/firmware/
    install -m 644 ${S}/../../evk/binary/nrc7394_cspi.bin ${D}${base_libdir}/firmware/
    install -m 644 ${S}/../../evk/binary/nrc7394_bd.dat ${D}${base_libdir}/firmware/
}
