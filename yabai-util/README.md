## やばいUtil集

### プログラムのアップロード
#### 事前準備(aws cliの利用)
1. `pip install awscli` でコマンドをインストール
2. [公式ドキュメント](http://docs.aws.amazon.com/ja_jp/cli/latest/userguide/cli-chap-getting-started.html)を参考に分析環境のcredentialの設定

#### 使い方
1. `icfp-post` を alias設定 or PATH配下にコピー
2. アップロードしたいプログラムディレクトリでコマンド実行
3. tar.gzへの圧縮、S3へのアップロードまで行われます

#### オプション
```
icfp-post [NAME [PROFILE]]
```

|項目|詳細|
|----|----|
|NAME|クライアント名(省略時はユーザ名)|
|PROFILE|S3アップロードに使うプロファイル名(省略時はdefault)|
