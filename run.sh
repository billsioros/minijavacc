#!/bin/bash

EXECUTABLE="Main"

declare -a DATASETS

DATASETS=("./examples" "./examples/extra")

declare -a LABELS

LABELS=("positive" "negative")

EDITOR="$(command -v code)"

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

if [[ "$*" == *"--clean"* ]]
then
    log "[MESSAGE]" "Purging..."

    ./compile.sh --clean

    for dataset in "${DATASETS[@]}"
    do
        for label in "${LABELS[@]}"
        do
            rm -rfv "$dataset"/"$label"/out "$dataset"/"$label"/error
        done
    done

    clear

    exit 0
fi

if ! ./compile.sh > /dev/null 2>&1
then
    log "[ERROR]:" "Compilation failed [./log/compile.log]"

    exit 1
fi

if [[ "$*" == *"-all"* ]]
then
    set -- "--success" "--failure" "--problem"
fi

for i in "${!DATASETS[@]}"
do
    dataset="${DATASETS[$i]}"

    if [ ! -d "$dataset" ]
    then
        continue;
    fi

    brief=""; problematic=0

    log "[MESSAGE]" "Processing dataset '$dataset'"

    for j in "${!LABELS[@]}"
    do
        label="${LABELS[$j]}"

        log "[MESSAGE]" "Processing files labeled '$label'\n"

        total=0; faulty=0;

        dir_out="$dataset"/"$label"/out
        dir_off="$dataset"/offset
        dir_err="$dataset"/"$label"/error

        mkdir -p "$dir_out" "$dir_err"

        for file in $(ls "$dataset"/"$label"/*.mini)
        do
            counterpart=""

            result="[SUCCESS]"; ((total++))

            name="$(basename "$file")"

            name="${name%.*}"

            file_out="$dir_out"/"$name".out
            file_off="$dir_off"/"$name".off
            file_err="$dir_err"/"$name".error

            if ! java "$EXECUTABLE" "$file" 2> "$file_err" 1> "$file_out"
            then
                ((faulty++))

                if [ "$label" == "negative" ]
                then
                    result="[FAILURE]";

                    counterpart="$dataset/positive/$(basename "${file%-error.mini}").mini"
                else
                    result="[PROBLEM]"; details="File labeled as 'positive' failed"
                fi
            else
                if [ "$label" == "negative" ]
                then
                    result="[PROBLEM]"; details="File labeled as 'negative' succeeded"
                else
                    differences="$(diff <( tr -d "[:space:]" <"$file_out" ) <( tr -d "[:space:]" <"$file_off"))";

                    if [ -n "$differences" ]
                    then
                        result="[PROBLEM]"; details="Different program structures"
                    fi
                fi
            fi

            if [ "$result" == "[PROBLEM]" ]
            then
                ((problematic++))

                log "$result" "$file ($details)"
            else
                log "$result" "$file"
            fi

            if  [ -n "$EDITOR" ]
            then
                if [[ "$*" == *"--success"* ]] && [ "$result" == "[SUCCESS]" ]
                then
                    "$EDITOR" -r -d "$file_out" "$file_off"; "$EDITOR" -r -w "$file"
                fi

                if [[ "$*" == *"--failure"* ]] && [ "$result" == "[FAILURE]" ]
                then
                    if [ -r "$counterpart" ]
                    then
                        "$EDITOR" -r -d "$file" "$counterpart"

                        "$EDITOR" -r -w "$file_err"
                    else
                        "$EDITOR" -r -w "$file" "$file_err"
                    fi
                fi

                if [[ "$*" == *"--problem"* ]] && [ "$result" == "[PROBLEM]" ]
                then
                    if [ -r "$file_out" ] && [ -r "$file_off" ]
                    then
                        "$EDITOR" -r -d "$file_out" "$file_off"
                    fi

                    "$EDITOR" -r -w "$file" "$file_err"
                fi
            fi
        done

        brief="$brief\n$(printf "%02d" $faulty) out of $(printf "%02d" $total) files labeled as '$label' exhibited problems"

        if [ "$i" -lt "${#DATASETS[@]}" ]
        then
            echo -en '\n'
        fi
    done

    echo -e "$brief\n"

    if [ "$problematic" -gt 0 ]
    then
        echo -e "$(tput setaf 196)$(printf "%02d" "$problematic") out of $(printf "%02d" "$total") files were problematic$(tput sgr0)\n"
    fi
done

exit 0
