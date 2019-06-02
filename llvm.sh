#!/bin/bash

filename="Factorial.mini"

if [ ! -r "./examples/positive/$filename" ]
then
    exit 1
fi

./compile.sh --clean
./compile.sh
java Main ./examples/positive/"$filename" > ./structure
code ./examples/positive/"$filename" ./structure ./examples/positive/"${filename%.*}".ll ./examples/llvm/"${filename%.*}".llvm
clang -o binary ./examples/positive/"${filename%.*}".ll

./binary
