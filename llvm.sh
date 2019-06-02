#!/bin/bash

./compile.sh --clean
./compile.sh
java Main ./examples/positive/BinaryTree.mini > ./structure
code ./examples/positive/BinaryTree.mini ./structure ./examples/positive/BinaryTree.ll
clang -o binary ./examples/positive/BinaryTree.ll

./binary
