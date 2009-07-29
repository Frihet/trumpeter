#! /usr/bin/env bash

export LANG=en_US.UTF-8
java -cp $(ls -1 lib/* | xargs | sed -e "s/ /:/g"):etc no.freecode.trumpeter.App
