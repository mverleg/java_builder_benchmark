#!/usr/bin/env bash

iter_cnt=3
file_cnt=2000

set -eEuo pipefail

base="$(pwd)"
target=/tmp/generated

rm -rf "$target/generated"

export MAVEN_OPTS="-Xms1g -Xmx6g"
export MAVEN_OPTIONS="-Xms1g -Xmx12g"

mvn clean package -q exec:java -Dexec.args="'$target' $file_cnt"

cd "$target"
for d in constructoronly hardcodedbuilder immutableflexiblebuilder immutablestagedbuilder
do
    cp "$base/pom.xml" "$d/pom.xml"
    (
        cd "$d"
        echo "$d warmup"
        mvn clean package -q
        total_time=0
        for i in `seq 1 $iter_cnt`
        do
            echo "$d #$i"
            mvn clean -q
            start_time=$(date +%s%3N)
            mvn package -q
            end_time=$(date +%s%3N)
            total_time=$((total_time + end_time - start_time))
        done
        printf 'TOTAL TIME FOR %s WAS %s MILLIS\n' "$d" "$total_time"
    )
done

