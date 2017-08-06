const app = require('express')();
const bodyParser = require('body-parser');
const http = require('http').Server(app);
const fs = require('fs');
const db = require('./lib/db');
const battle = require('./lib/battle');
const url = require('url');

app.use(bodyParser.urlencoded({ extended: true }));
app.use(bodyParser.json());

// クライアント一覧
app.get('/punter/list', (req, res) => {
  db.punters().then((data) => {
    res.json(data);
  });
});

// クライアント削除
app.post('/punter/delete', (req, res) => {
  if (req.body.id) {
    db.deletePunter(req.body.id).then((data) => {
      res.json(data);
    })
  } else {
    res.status(400).json({message: 'Punter ID is required.'});
  }
});

// マップ一覧
app.get('/map/list', (req, res) => {
  db.maps().then((data) => {
    res.json(data.map((v) => {
      v.url = `https://s3-ap-northeast-1.amazonaws.com/${db.bucket}/maps/${v.id}.json`;
      return v;
    }));
  });
});

// 対戦一覧
app.get('/game/list', (req, res) => {
  const query = url.parse(req.url, true).query;
  db.games().then((data) => {
    res.json(data);
  });
});

// 対戦実行
app.get('/game/execute', (req, res) => {
  console.log(req.url);
  const query = url.parse(req.url, true).query;
  if (query.punter_ids) query.punterArray = query.punter_ids.split(',');
  battle.exec(query).then((data) => {
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
