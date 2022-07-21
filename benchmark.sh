#!/usr/bin/env bash

set -eEuo pipefail -x

rm -rf generated

mvn clean package exec:java

cp pom.xml generated/constructoronly/
cp pom.xml generated/hardcodedbuilder/
cp pom.xml generated/immutableflexiblebuilder/
cp pom.xml generated/immutablestagedbuilder/
