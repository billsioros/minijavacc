#!/bin/bash

./compile.sh --clean
./compile.sh
java Main ./examples/positive/BinaryTree.mini
clang -o binary ./examples/positive/BinaryTree.ll
