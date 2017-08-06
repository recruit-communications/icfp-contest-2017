#!/bin/bash -ex
cd `dirname $0`

# 自動生成されるファイル群削除
# installやpunterを入れる必要があるかも
rm -rf ./src

# 親ディレクトリ配下のsubmission以外のディレクトリをsrcに突っ込む
mkdir src
for dir in ../*/; do
    if [ $dir = "../submission/" ]; then
        continue
    fi
    cp -R ${dir%/} src
done

# TODO installとpunter作成

# アーカイブの都合上installとpunterの存在を保証しておく
touch install punter

# tar.gz作る
# 引数がある場合はそれをアーカイブ名にする
if (($# >= 1)); then
	ARCHIVE=$1.tar.gz
else
	TOKEN=57c8be97-ef1c-4730-bf15-5da1d039fab3
	ARCHIVE=icfp-$TOKEN.tar.gz
fi
tar cvz install punter PACKAGES src README > $ARCHIVE
md5 $ARCHIVE
