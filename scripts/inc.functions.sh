#!/bin/bash
#
# Collection of shared functions
#

GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[0;33m'
NC='\033[0m' # No Color

function approve {
    echo -e "${GREEN}$@${NC}"
}

function warn {
    echo -e "${YELLOW}$@${NC}"
}

function die {
    echo -e "${RED}$@${NC}"
    exit 1
}

function safe {
    "$@"
    local status=$?
    if [[ ${status} -ne 0 ]]; then
        die "\nBUILD FAILED\nAfter invoking \"$@\"\n" >&2
    fi
    return ${status}
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

function sed2 {
    sed -i'.bak' "$1" ${@:2}
    for file in "${@:2}" ; do
        rm "${file}.bak"
    done
}
