# search path so the fragment is found first
FILESEXTRAPATHS:prepend := "${THISDIR}/files:"

# ship our fragment into the build
SRC_URI += "file://enable-mesh.cfg"
