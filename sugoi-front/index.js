const app = require('express')();
const http = require('http').Server(app);
const aws = require('aws-sdk');
const s3 = new aws.S3();

const bucket = 'icfp2017-kst3-jp';
const prefix = 'clients/';

// S3からクライアント一覧を取得
function listClients() {
  return new Promise((fulfill) => {
    s3.listObjects({
      Bucket: bucket,
      Prefix: prefix
    }, (err, data) => {
      fulfill(data.Contents.slice(1).map((v) => v.Key));
    });
  });
}

app.get('/list', (req, res) => {
    listClients().then((data) => {
      const list = data.map((v) => `https://s3-ap-northeast-1.amazonaws.com/${bucket}/${v}`)
      res.json(list);
    });
});

http.listen(3000, () => console.log('start server.'));
