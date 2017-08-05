const app = require('express')();
const http = require('http').Server(app);
const db = require('./lib/db');
const battle = require('./lib/battle');

app.get('/client/list', (req, res) => {
  db.clients().then((data) => {
    const list = data.map((v) => `https://s3-ap-northeast-1.amazonaws.com/${db.bucket}/${v}`)
    res.json(list);
  });
});

app.get('/map/list', (req, res) => {
  db.maps().then((data) => {
    const list = data.map((v) => `https://s3-ap-northeast-1.amazonaws.com/${db.bucket}/${v}`)
    res.json(list);
  });
});

app.get('/battle/list', (req, res) => {
  db.battles().then((data) => {
    const list = data.map((v) => `${v}`)
    res.json(list);
  });
});

http.listen(3000, () => console.log('start server.'));
