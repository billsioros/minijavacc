#!/bin/bash

EDITOR=""

total=0
problematic=0

DIRECTORIES=( "./examples/positive" "./examples/extra/positive" )

OUT="./test"

if [[ "$*" == *"--clean"* ]]
then
    log "[MESSAGE]" "Purging..."

    ./compile.sh --clean

    find . -maxdepth 5 -name "*.ll" -delete -print

    rm -rfv "$OUT"

    exit 0
fi

mkdir -p "$OUT"

OUT="$(realpath "$OUT")"

function log
{
    if [[ "$1" == *"[SUCCESS]"* ]]
    then
        color=118
    elif [[ "$1" == *"[FAILURE]"* ]]
    then
        color=166
    elif [[ "$1" == *"[MESSAGE]"* ]]
    then
        color=6
    elif [[ "$1" == *"[WARNING]"* ]]
    then
        color=170
    else
        color=196
    fi

    echo -e "$(tput setaf "$color")$1$(tput sgr0): $2"
}

function test
{
    ((total++))

    directory="$(dirname "$1")"

    filename="$(basename "$1")"

    if [ "${filename##*.}" != "java" ]
    then
        log "[WARNING]" "Extension Mismatch"

        return
    fi

    name="${filename%.*}"

    if java Main "$directory"/"$name".java > "$OUT"/"$name".structure
    then
        mv "$directory"/"$name".ll "$OUT"/"$name".ll

        if clang -g -Wno-override-module -o "$OUT"/"$name".bin "$OUT"/"$name".ll
        then
            if ! "$OUT"/"$name".bin > "$OUT"/"$name".secondary
            then
                ((problematic++))

                log "[PROBLEM]" "'$1' (Abrupt Termination)"

                if  [ -n "$EDITOR" ]
                then
                    "$EDITOR" -r "$directory"/"$name".java "$OUT"/"$name".structure "$OUT"/"$name".secondary

                    if [ -r ./examples/llvm/"$name".llvm ]
                    then
                        "$EDITOR" -r ./examples/llvm/"$name".llvm
                    fi

                    "$EDITOR" -r -w "$OUT"/"$name".ll
                fi
            else
                (
                    cd "$directory" || return

                    javac "$name".java
                    java  "$name" > "$OUT/$name.original"
                )

                differences="$(diff <( tr -d "[:space:]" <"$OUT"/"$name".secondary ) <( tr -d "[:space:]" <"$OUT/$name.original"))";

                if [ -n "$differences" ]
                then
                    ((problematic++))

                    log "[PROBLEM]" "'$1' (Different output from javac)"

                    if [ -n "$EDITOR" ]
                    then
                        "$EDITOR" -w -r -d "$OUT"/"$name".secondary "$OUT/$name.original"
                    fi
                fi
            fi

            log "[SUCCESS]" "'$1'"

            return
        fi
    fi

    ((problematic++))

    log "[PROBLEM]" "'$1' (Compilation Error)"

    read -n1 -r -p "Press any key to continue..."
}

if [[ "$*" == *"--problem"* ]]
then
    EDITOR="$(command -v code)"
fi

./compile.sh --clean
./compile.sh

if [ "$#" -gt 0 ]
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
            log "[WARNING]" "Failed to resolve arguement '$arguement'"

            continue;
        fi
    done
else
    for directory in "${DIRECTORIES[@]}"
    do
        for filename in $(ls "$directory")
        do
            test "$directory"/"$filename"
        done
    done
fi

if [ "$problematic" -gt 0 ]
then
    echo -e "\n$(tput setaf 196)$(printf "%02d" "$problematic") out of $(printf "%02d" "$total") files were problematic$(tput sgr0)"
fi

exit 0
