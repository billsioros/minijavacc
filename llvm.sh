#!/bin/bash

function test
{
    DIR="$(dirname "$1")"
    filename="$(basename "$1")"

    echo "Directory: $DIR Filename: $filename"

    name="${filename%.*}"

    if java Main "$DIR"/"$name".mini > "$OUT"/"$name".str
    then
        mv "$DIR"/"$name".ll "$OUT"/"$name".ll

        if clang -o "$OUT"/"$name".bin "$OUT"/"$name".ll
        then
            if ! "$OUT"/"$name".bin
            then
                code -w "$OUT"/"$name".ll ./examples/llvm/"$name".llvm "$DIR"/"$name".mini "$OUT"/"$name".str
            fi
        fi
    fi
}

DIR="./examples/positive"

OUT="./IR"

mkdir -p "$OUT"

./compile.sh --clean
./compile.sh

if [ ! "$#" -eq 0 ]
then
    for filename in "$@"
    do
        test "$filename"
    done

    exit 0
fi

for filename in $(ls "$DIR")
do
    test "$filename"
done

rm -rfv "$OUT"
