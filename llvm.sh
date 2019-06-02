#!/bin/bash

filename="LinearSearch.mini"

if [ ! -r "./examples/positive/$filename" ]
then
    exit 1
fi

./compile.sh --clean
./compile.sh

if java Main ./examples/positive/"$filename" > ./structure
then
    code ./examples/positive/"$filename" ./structure ./examples/positive/"${filename%.*}".ll ./examples/llvm/"${filename%.*}".llvm

    if clang -o binary ./examples/positive/"${filename%.*}".ll
    then
        ./binary
    fi
fi
