#!/bin/bash

EDITOR="$(command -v code)"

total=0
problematic=0

DIRECTORIES=( "./examples/positive" "./examples/extra/positive" )

OUT="./IR"

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

                if  [ -n "$EDITOR" ] && [[ "$*" == *"--problem"* ]]
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

                    if [ -n "$EDITOR" ] && [[ "$*" == *"--problem"* ]]
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

if [[ "$*" == *"--clean"* ]]
then
    log "[MESSAGE]" "Purging..."

    ./compile.sh --clean

    rm -rfv "$OUT"

    exit 0
fi

./compile.sh --clean
./compile.sh

for directory in "${DIRECTORIES[@]}"
do
    for filename in $(ls "$directory")
    do
        test "$directory"/"$filename"
    done
done

if [ "$problematic" -gt 0 ]
then
    echo -e "\n$(tput setaf 196)$(printf "%02d" "$problematic") out of $(printf "%02d" "$total") files were problematic$(tput sgr0)"
fi

exit 0
