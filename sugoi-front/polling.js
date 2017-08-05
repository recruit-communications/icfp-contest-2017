// punter、対戦ログ, mapのポーリング処理

const db = require('./lib/db');
const log = require('./lib/log');
const sortBy = require('underscore').sortBy;

// punter
db.punters().then((data) => {
  return sortBy(data, (e) => e.created_at).shift().created_at;
}).then((last) => {
  const prefix = 'clients/'
  const suffix = '.tar.gz'
  db.s3List(prefix).then((list) => {
    list.filter((e) => e.LastModified.getTime() > last).forEach((e) => {
      db.addPunter({
        id: db.p2i(e.Key, prefix, suffix),
        created_at: e.LastModified.getTime()
      });
    });
  });
});

// map
db.maps().then((data) => {
  return sortBy(data, (e) => e.created_at).pop().created_at;
}).then((last) => {
  const prefix = 'maps/'
  const suffix = '.json'
  db.s3List(prefix).then((list) => {
    list.filter((e) => e.LastModified.getTime() > last).forEach((e) => {
      db.addMap({
        id: db.p2i(e.Key, prefix, suffix),
        created_at: e.LastModified.getTime()
      });
    });
  });
});

// log
const params = {
  ScanFilter: {
    'results': {
      ComparisonOperator: 'NULL'
    }
  }
};
db.games(params).then((data) => {
  data.forEach((game) => {
    const key = db.i2p(game.id, 'logs/app.', '.log');
    db.s3Get(key).then((obj) => {
      // ログから結果を反映
      const res = log.parse(obj.Body.utf8Slice()).map((r, i) => {
        r.punter = game.punter_ids[i]
        return r;
      });
      game.results = sortBy(res, (r) => r.score).reverse();
      db.updateGame(game);
    }).catch((e) => {
      // s3Getのエラーはスルー
      if (e.name === 'NoSuchKey') return;
      console.log(e);
    });
  });
});
