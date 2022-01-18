#!/bin/env bash

# TODO: go back to dir user was in


# Get script dir
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"

# Goto project dir
cd "$DIR/../.."

# Build project
mvn -Dstyle.color=always clean package
