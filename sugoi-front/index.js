const app = require('express')();
const http = require('http').Server(app);
const fs = require('fs');
const db = require('./lib/db');
const battle = require('./lib/battle');

// クライアント一覧
app.get('/client/list', (req, res) => {
  db.clients().then((data) => {
    res.json(data);
  });
});

// マップ一覧
app.get('/map/list', (req, res) => {
  db.maps().then((data) => {
    res.json(data.map((v) => {
      return {
        id: v,
        url: `https://s3-ap-northeast-1.amazonaws.com/${db.bucket}/maps/${v}.json`,
      }
    }));
  });
});

// 対戦一覧
app.get('/battle/list', (req, res) => {
  db.battles().then((data) => {
    res.json(data);
  });
});

// 対戦実行
app.get('/battle/execute', (req, res) => {
  battle.exec().then((data) => {
    res.json(data);
  });
});

// その他静的ファイル
app.get('/public/*', (req, res) => {
  const path = req.url.split('?')[0];
  fs.readFile(__dirname + path, (err, data) => {
    if (err) {
      res.writeHead(404);
      return res.end(' ');
    }
    res.writeHead(200);
    res.end(data);
  });
});

http.listen(3000, () => console.log('start server.'));
