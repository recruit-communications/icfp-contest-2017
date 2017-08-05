# ICFPC 2017 チームUdon

## 提出用パッケージ作成

1. `submission/install` と `submission/punter` を好きなものに書き換える（そのままtar.gzに含まれる）
1. このREADMEがあるディレクトリで `submission/packaging.sh` を実行する
1. `submission/icfp-57c8be97-ef1c-4730-bf15-5da1d039fab3.tar.gz` が作成される

## tar.gz内のディレクトリ構成

#### ./install
* ビルドスクリプト。ジャッジ環境で1度だけ実行される
* できればネット通信が不要にしてほしい。
* どうしても必要なら理由をREADMEに書いておいて。

#### ./punter
* 実行ファイル。installによって作成しても良い
* ローカルにファイルを置きたいなら、READMEに書いておいて
* 運営が最終的にそれを許可するかどうか決めるよ（というわけで避けた方がいい）
* stdin, stdout, stderr以外のファイルシステムへのアクセスは通常禁止。

#### ./PACKAGES
* installとpunterに必要な追加パッケージを書いたテキスト

#### ./src/
* ソースコード

#### ./README
* チームメンバーの名前とアルゴリズムの説明（アルゴリズムの説明は任意）
