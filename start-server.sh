#!/bin/bash
#
# Script for running set game server.
# Normally, you should be able to execute this script with
#
#   ./start-server.sh
#
# and it should do The Right Thing(tm) by default. However, it does take some
# arguments so that you can tweak it for your set gane setup.
#
#   1) how many properties to play with (defaults to 4)

if [ $# -eq 1 ] && [ "$1" = "--help" ]; then
	echo "Usage: $0 [ATTRIBUTES=4]"
	exit 1
fi

# Default to 4 attributes unless otherwise specified
attr=4
if [ $# -gt 0 ]; then
	attr="$1"
fi

java -ea -cp target/classes setgame.ServerMain 8080 "$attr"