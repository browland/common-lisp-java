#! /bin/bash

find . -name '*.java' > source-files
javac @source-files -d build

