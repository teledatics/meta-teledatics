SUMMARY = "Newracom CLI utility"
LICENSE = "GPL-2.0-only"
LIC_FILES_CHKSUM ?= "file://${COMMON_LICENSE_DIR}/GPL-2.0-only;md5=801f80980d171dd6425610833a22dbe6"

SRCBRANCH = "nrc-dkms-v1.2.2-rc1"
SRCREV = "d7a3b5370fe4b0fbf8c9e296d43e7813d8347ae2"
SRC_URI = "git://github.com/teledatics/nrc7394_sw_pkg.git;protocol=https;branch=${SRCBRANCH}"

S = "${WORKDIR}/git/package/src/cli_app"

RPROVIDES_${PN} += "${PN}"

FILES:${PN} += "${bindir}/cli_app"

TARGET_CC_ARCH += "${LDFLAGS}"

do_compile() {
    oe_runmake
}

do_install() {
    install -d ${D}${bindir}
    install -m 0755 ${S}/cli_app ${D}${bindir}
}
