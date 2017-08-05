#!/bin/bash -ex
cd `dirname $0`

rm -rf ./src
mkdir src

# 親ディレクトリ配下のsubmission以外のディレクトリをsrcに突っ込む
for dir in ../*/; do
    if [ $dir = "../submission/" ]; then
        continue
    fi
    cp -R ${dir%/} src
done
