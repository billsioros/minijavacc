#!/bin/bash

DIR="./examples/positive"

OUT="./IR"

mkdir -p "$OUT"

./compile.sh --clean
./compile.sh

for file in $(ls "$DIR")
do
    name="${file%.*}"

    if java Main "$DIR"/"$file" > "$OUT"/"$name".str
    then
        mv "$DIR"/"$name".ll "$OUT"/"$name".ll

        if clang -o "$OUT"/"$name".bin "$OUT"/"$name".ll
        then
            if ! "$OUT"/"$name".bin
            then
                code -w "$OUT"/"$name".ll ./examples/llvm/"$name".llvm "$DIR"/"$file" "$OUT"/"$name".str
            fi
        fi
    fi
done

rm -rfv "$OUT"
