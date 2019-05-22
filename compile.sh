#!/bin/bash

if [[ "$*" == *"--clean"* ]]
then
    make clean 1> /dev/null 2>&1

    rm -rfv ./log

    exit 0
fi

{ log=$(make compile 2>&1 >&3 3>&- 1> /dev/null); } 3>&1

if echo "$log" | grep error
then    
    mkdir -p ./log/; echo "$log" > ./log/compile.log

    exit 1
fi

exit 0
