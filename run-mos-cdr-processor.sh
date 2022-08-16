#!/bin/bash

# run from target directory`
# Put your actual runConfig.json inside data (may contain sensitive data)
# The sample one is in the base directory
java -jar moscdrprocessor-0.0.9-SNAPSHOT-onejar.jar process -v \
    --config "~/projects/mos-cdr-processor/data/runConfig.json"
