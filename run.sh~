#!/usr/bin/env bash

usage() { echo "Usage: $0 [-P] [-b] [-p]" 1>&2; exit 1; }

OPT_PRESENT=0

while  getopts "Pbp" o; do
    case "${o}" in
        P)
            cd lib/pac4j
            mvn clean install
            cd ../../
            OPT_PRESENT=1
            ;;
        p)
            cd lib/play-pac4j
            mvn clean install
            cd ../../
            OPT_PRESENT=1
            ;;
        b)
            cd lib/pac4j
            mvn clean install
            cd ../play-pac4j
            mvn clean install
            cd ../../
            OPT_PRESENT=1
            ;;
        \?)
            echo "Invalid option: -$OPTARG" >&2
            OPT_PRESENT=1
            usage
            ;;
    esac
done

if [ "${OPT_PRESENT}" -ne "1" ]; then
    echo "At least one option is required" >&2
    usage
    exit -1
fi

# Run Portal
play run -Dhttp.port=disabled -Dhttps.port=9000 -DapplyEvolutions.default=true -DapplyDownEvolutions.default=true -Dconfig.file=/Users/shliyana/Workspace/HTRC/HTRC-Portal/conf/application.conf -Dhttps.keyStore=/Users/shliyana/Workspace/HTRC/HTRC-Portal/conf/keystore.jks -Dhttps.keyStorePassword=portal


