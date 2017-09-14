#!/bin/bash
#
# Collection of shared functions
#

function die {
    echo -e "$@"
    exit 1
}

function version_gt() {
    test "$(printf '%s\n' "$@" | sort -V | head -n 1)" != "$1";
}

function get_version_name {
    echo $(grep "versionName '" $1 | cut -d"'" -f2)
}

function get_version_code {
    echo $(grep "versionCode " $1 | cut -d" " -f10 ) # VERY FLAKY!
}