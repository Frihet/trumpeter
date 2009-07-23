#! /usr/bin/env bash

java -cp $(ls -1 lib/* | xargs | sed -e "s/ /:/g"):etc no.freecode.trumpeter.App
