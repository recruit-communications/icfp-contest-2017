const app = require('express')();
const http = require('http').Server(app);
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
    const list = data.map((v) => `https://s3-ap-northeast-1.amazonaws.com/${db.bucket}/${v}`)
    res.json(list);
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

http.listen(3000, () => console.log('start server.'));
