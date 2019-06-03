#!/bin/bash

problematic=0

DIR="./examples/extra/positive"

OUT="./IR"

function test
{
    DIR="$(dirname "$1")"

    filename="$(basename "$1")"

    if [ "${filename##*.}" != "mini" ]
    then
        return
    fi

    echo -e "\nFilename: $1\n"

    name="${filename%.*}"

    if java Main "$DIR"/"$name".mini > "$OUT"/"$name".str
    then
        mv "$DIR"/"$name".ll "$OUT"/"$name".ll

        if clang -Wno-override-module -o "$OUT"/"$name".bin "$OUT"/"$name".ll
        then
            if ! "$OUT"/"$name".bin
            then
                code -w "$OUT"/"$name".ll ./examples/llvm/"$name".llvm "$DIR"/"$name".mini "$OUT"/"$name".str

                ((problematic++))
            fi

            return
        fi
    fi

    read -n1 -r -p "Press any key to continue..."

    ((problematic++))
}

mkdir -p "$OUT"

./compile.sh --clean
./compile.sh

if [ ! "$#" -eq 0 ]
then
    for arguement in "$@"
    do
        if [ -f "$arguement" ]
        then
            test "$arguement"
        elif [ -d "$arguement" ]
        then
            for filename in $(ls "$arguement")
            do
                test "$arguement"/"$filename"
            done
        else
            exit 1
        fi
    done
else
    for filename in $(ls "$DIR")
    do
        test "$DIR"/"$filename"
    done
fi

if [ "$problematic" -eq 0 ]
then
    rm -rfv "$OUT"
fi

exit 0
