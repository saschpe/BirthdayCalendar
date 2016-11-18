#!/bin/bash
#
# Collection of shared functions
#

function die {
    echo -e "$@"
    exit 1
}

function safe {
    "$@"
    local status=$?
    if [ $status -ne 0 ]; then
        die "\nBUILD FAILED\nWhen invoking \"$@\"\n" >&2
    fi
    return $status
}

function get_version {
    echo $(grep "version \=" $1 | cut -d"\"" -f2)
}

function get_version_name {
    echo $(grep "versionName \"" $1 | cut -d"\"" -f2)
}

function get_version_code {
    echo $(grep "versionCode " $1 | cut -d" " -f10 ) # VERY FLAKY!
}

function fail_build {
    die "\nBUILD FAILED\n"
}

# Parameters:
#   FILE
#   RELEASE
function release_notes {
   start_line=$(grep -n "$2" "$1" | cut -d":" -f1)
   {
       IFS=''
       # Skip some lines
       for ((i=$start_line;i--;)); do read ; done
       # Read until first blank line
       while read line ;do
           if [ -z "$line" ]; then break; fi
           echo $line
       done
   } < "$1"
}
