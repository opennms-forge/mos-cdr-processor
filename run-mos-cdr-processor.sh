#!/bin/bash

# run from target directory`
# Put your actual runConfig.json inside data (may contain sensitive data)
# The sample one is in the base directory
cd target
java -jar moscdrprocessor-0.0.1-SNAPSHOT-onejar.jar folder -v \
    --config "~/projects/mos-cdr-processor/data/runConfig.json"
