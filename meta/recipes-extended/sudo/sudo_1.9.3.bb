require sudo.inc

SRC_URI = "https://www.sudo.ws/dist/sudo-${PV}.tar.gz \
           ${@bb.utils.contains('DISTRO_FEATURES', 'pam', '${PAM_SRC_URI}', '', d)} \
           file://0001-sudo.conf.in-fix-conflict-with-multilib.patch \
           file://CVE-2021-23239.patch \
           file://CVE-2021-23240.patch \
           file://CVE-2021-3156-1.patch \
           file://CVE-2021-3156-2.patch \
           file://CVE-2021-3156-3.patch \
           file://CVE-2021-3156-4.patch \
           file://CVE-2021-3156-5.patch \
           "

PAM_SRC_URI = "file://sudo.pam"

SRC_URI[sha256sum] = "1d9889cc3b3b15ed8c2c7c3de3aa392a3a726838d020815067c080525c3f5837"

DEPENDS += " virtual/crypt ${@bb.utils.contains('DISTRO_FEATURES', 'pam', 'libpam', '', d)}"
RDEPENDS_${PN} += " ${@bb.utils.contains('DISTRO_FEATURES', 'pam', 'pam-plugin-limits pam-plugin-keyinit', '', d)}"

CACHED_CONFIGUREVARS = " \
        ac_cv_type_rsize_t=no \
        ac_cv_path_MVPROG=${base_bindir}/mv \
        ac_cv_path_BSHELLPROG=${base_bindir}/sh \
        ac_cv_path_SENDMAILPROG=${sbindir}/sendmail \
        ac_cv_path_VIPROG=${base_bindir}/vi \
        "

EXTRA_OECONF += " \
             ${@bb.utils.contains('DISTRO_FEATURES', 'pam', '--with-pam', '--without-pam', d)} \
             ${@bb.utils.contains('DISTRO_FEATURES', 'systemd', '--enable-tmpfiles.d=${nonarch_libdir}/tmpfiles.d', '--disable-tmpfiles.d', d)} \
             --with-rundir=/run/sudo \
             --with-vardir=/var/lib/sudo \
             --libexecdir=${libdir} \
             "

do_install_append () {
	if [ "${@bb.utils.filter('DISTRO_FEATURES', 'pam', d)}" ]; then
		install -D -m 644 ${WORKDIR}/sudo.pam ${D}/${sysconfdir}/pam.d/sudo
		if ${@bb.utils.contains('PACKAGECONFIG', 'pam-wheel', 'true', 'false', d)} ; then
			echo 'auth       required     pam_wheel.so use_uid' >>${D}${sysconfdir}/pam.d/sudo
			sed -i 's/# \(%wheel ALL=(ALL) ALL\)/\1/' ${D}${sysconfdir}/sudoers
		fi
	fi

	chmod 4111 ${D}${bindir}/sudo
	chmod 0440 ${D}${sysconfdir}/sudoers

	# Explicitly remove the /sudo directory to avoid QA error
	rmdir -p --ignore-fail-on-non-empty ${D}/run/sudo
}

FILES_${PN} += "${nonarch_libdir}/tmpfiles.d"
FILES_${PN}-dev += "${libdir}/${BPN}/lib*${SOLIBSDEV} ${libdir}/${BPN}/*.la \
                    ${libdir}/lib*${SOLIBSDEV} ${libdir}/*.la"
