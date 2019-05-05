#!/bin/bash -x

# Copyright 2011 Red Hat Inc.
#
# This file is part of solenopsis
#
# solenopsis is free software; you can redistribute it and/or
# modify it under the terms of the GNU General Public License
# as published by the Free Software Foundation; either version 3
# of the License, or (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program; if not, write to the Free Software
# Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA

if [ $# = "0" ]
then
	DEB_HOME=~/.solenopsis
else
	DEB_HOME=$1
fi

# -------------------------------------------------------------------

export DEB_BUILD_ROOT=${DEB_HOME}/build/solenopsis_1.2-125
export DEB_BUILD_SOLENOPSIS_ROOT=${DEB_BUILD_ROOT}/usr/share/solenopsis

# -------------------------------------------------------------------

rm -rf ${DEB_BUILD_ROOT}

# -------------------------------------------------------------------

echo "Building DEB in ${DEV_BUILD_ROOT}..."

# -------------------------------------------------------------------

mkdir -p ${DEB_BUILD_ROOT}/DEBIAN \
    ${DEB_BUILD_SOLENOPSIS_ROOT}/usr/share/solenopsis \
    ${DEB_BUILD_SOLENOPSIS_ROOT}/config \
    ${DEB_BUILD_SOLENOPSIS_ROOT}/ant/lib/1.9.6

# -------------------------------------------------------------------

cp config/defaults.cfg ${DEB_BUILD_SOLENOPSIS_ROOT}/config
cp -rf docs ${DEB_BUILD_SOLENOPSIS_ROOT}
cp -rf ant ${DEB_BUILD_SOLENOPSIS_ROOT}
cp -rf scripts ${DEB_BUILD_SOLENOPSIS_ROOT}

cp solenopsis.deb ${DEB_BUILD_ROOT}/DEBIAN/control

# -------------------------------------------------------------------

dpkg-deb --build ${DEB_BUILD_ROOT}