#!/usr/bin/env bash

SCRIPTDIR=$(dirname ${BASH_SOURCE[0]})
export JAVAFX=$SCRIPTDIR/javafx-sdk-11.0.2
java --module-path ${JAVAFX}/lib --add-modules="javafx.controls" -jar $SCRIPTDIR/lib/asm.jar
