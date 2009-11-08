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


# The "stop" command isn't actually working 100% of the time. This is a small
# hack to get this working for the time being (unfortunately this kills all
# instances - not just the one in the current directory).
if [ "$1" = "stop" ]
then
	kill $(pgrep -fn no.freecode.trumpeter.App) > /dev/null 2>&1
fi
