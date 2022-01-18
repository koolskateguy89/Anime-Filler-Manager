#!/bin/env bash

# Get script dir
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"

java -jar "$DIR/../../app/target/AFM.jar"
