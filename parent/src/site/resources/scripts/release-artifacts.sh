#!/bin/sh

# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements.  See the NOTICE file distributed with
# this work for additional information regarding copyright ownership.
# The ASF licenses this file to You under the Apache License, Version 2.0
# (the "License"); you may not use this file except in compliance with
# the License.  You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

set -e

echo "Copying the download artifacts from the repository to the site."

if [ $# -ne 2 ]; then
    echo "Usage: $0 <version> <svn_username>"
    exit 1
fi

VERSION=$1
SVN_USERNAME=$2

MAVEN_REPOSITORY=https://repository.apache.org/content/repositories/releases
DIST_REPOSITORY=https://dist.apache.org/repos/dist/release/myfaces

# download file and hashes/signatures
function download() {
  DIR="$1"
  FILE_ON_REPO="$2"
  FILE="$3"

  curl --fail "${MAVEN_REPOSITORY}/${DIR}/${VERSION}/${FILE_ON_REPO}"      -o ${FILE}
  curl --fail "${MAVEN_REPOSITORY}/${DIR}/${VERSION}/${FILE_ON_REPO}.asc"  -o ${FILE}.asc
  # MD5 checksum files are now discouraged for new releases
  # curl --fail "${MAVEN_REPOSITORY}/${DIR}/${VERSION}/${FILE_ON_REPO}.md5"  -o ${FILE}.md5
  curl --fail "${MAVEN_REPOSITORY}/${DIR}/${VERSION}/${FILE_ON_REPO}.sha1" -o ${FILE}.sha1
}

# this performs check of the hashes (if this fails, something might went wrong absolutely)
function check() {
  FILE="$1"

  echo "Checking file ${FILE}: "

  # MD5 checksum files are now discouraged for new releases
  # md5 -q "${FILE}" > "${FILE}.md5.temp"
  # if ! diff --ignore-all-space "${FILE}.md5" "${FILE}.md5.temp" ; then
  #   echo "Error: MD5 check failed!"
  #   exit -1
  # fi
  # echo "  MD5 hash okay"

  shasum -a 1 "${FILE}" | cut "-d " -f1 > "${FILE}.sha1.temp"
  if ! diff --ignore-all-space "${FILE}.sha1" "${FILE}.sha1.temp" ; then
    echo "Error: SHA1 check failed!"
    exit -1
  fi
  echo "  SHA1 hash okay"

  if ! gpg --verify "${FILE}.asc" ; then
    echo "Error: GPG check failed!"
    exit -1
  fi
  echo "  GPG signature okay"

  # todo: change, if maven creates the sha512
  shasum -a 512 "${FILE}" > "${FILE}.sha512"
  echo "  SHA-512 created"

}

# this uploads the files into the svn dist repo
function upload() {
  PATTERN="$1"
  FOLDER="$2"

  for file in $(find . -type file -name "${PATTERN}" -exec basename \{\} \; ) ; do
    # echo $file;
    svn import --username $SVN_USERNAME -m "Uploading MyFaces Core release artifact ${VERSION} to dist server" $file ${DIST_REPOSITORY}/${FOLDER}/$file;
  done
}

# commands

download "org/apache/myfaces/core/myfaces-core-assembly"         "myfaces-core-assembly-${VERSION}-bin.tar.gz"            "myfaces-core-assembly-${VERSION}-bin.tar.gz"
download "org/apache/myfaces/core/myfaces-core-assembly"         "myfaces-core-assembly-${VERSION}-bin.zip"               "myfaces-core-assembly-${VERSION}-bin.zip"
download "org/apache/myfaces/core/myfaces-core-assembly"         "myfaces-core-assembly-${VERSION}-src.tar.gz"            "myfaces-core-assembly-${VERSION}-src.tar.gz"
download "org/apache/myfaces/core/myfaces-core-assembly"         "myfaces-core-assembly-${VERSION}-src.zip"               "myfaces-core-assembly-${VERSION}-src.zip"

check "myfaces-core-assembly-${VERSION}-bin.tar.gz"
check "myfaces-core-assembly-${VERSION}-bin.zip"
check "myfaces-core-assembly-${VERSION}-src.tar.gz"
check "myfaces-core-assembly-${VERSION}-src.zip"

rm -f *.temp

upload "myfaces-core-assembly-${VERSION}-bin.*"           "binaries"
upload "myfaces-core-assembly-${VERSION}-src.*"           "source"
