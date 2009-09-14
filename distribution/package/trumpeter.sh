#! /bin/sh
#
# Script for running trumpeter on a Unix-like system.
#

DIRNAME=$(cd $(dirname $0); pwd)
JAVA_EXEC=$(which java)


cd $DIRNAME

# If JAVA_HOME has been set, use the java executable in that folder.
if [ -n "${JAVA_HOME}" ]; then
	JAVA_EXEC="${JAVA_HOME}/bin/java"
fi

if [ -z "${JAVA_EXEC}" ]; then
	echo "Unable to find java. Please set JAVA_HOME or make sure the java executable is in your PATH."
	exit 1
fi

${JAVA_EXEC} -Djava.encoding=UTF-8 -cp $(ls -1 lib/* | xargs | sed -e "s/ /:/g"):etc no.freecode.trumpeter.App $@
## ${JAVA_EXEC} -Djava.encoding=UTF-8 -Djava.ext.dirs=lib:ext no.freecode.trumpeter.App $@
