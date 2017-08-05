// punter、対戦ログ, mapのポーリング処理

const db = require('./lib/db');

// punter
db.punters().then((data) => {
  return data.sort((a, b) => a.created_at < b.created_at).shift().created_at;
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
  return data.sort((a, b) => a.created_at < b.created_at).shift().created_at;
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