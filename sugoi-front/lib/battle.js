require('dotenv').config();

const uuid = require('uuid/v4');
const db = require('./db');
const http = require('http');

// マップから対戦人数を取得
function getPunterNum(map_id) {
  return db.maps().then((ms) => ms.find((m) => m.id === map_id).punter_num);
}

// 対戦を実行
// id: 対戦ID
// num: 対戦人数
// map_id: マップID
// punterArray: punterIDのarray
function exec({id = uuid(), num = null, map_id = null, punterArray = null} = {}) {
  let params = {};

  return Promise.resolve().then(() => {
    if (map_id) {
      // マップが指定されていればその人数を返す
      return getPunterNum(map_id);
    } else {
      // マップが指定されてなければランダム選択
      return pickMap().then((map) => {
        map_id = map.id;
        return map.punter_num;
      });
    }
  }).then((pnum) => {
    // numが指定されていればその人数、されてなければマップの人数で選択
    return pickPunter(num || pnum);
  }).then((punters) => {
    params = {
      id: id,
      punter_ids: punterArray || punters.map((p) => p.id),
      map_id: map_id
    };
    return postJenkins(params);
  }).then(() => {
    return db.addGame(params);
  }).then(() => {
    return params
  });
}

// Jenkinsにジョブ登録
function postJenkins({id, map_id, punter_ids}) {
  const params = [
    `game_id=${id}`,
    `map_id=${map_id}`,
    `punter_ids=${punter_ids.join(',')}`,
  ].join('&')
  const options = {
    host: '52.198.25.234',
    port: 8080,
    path: `/job/run/buildWithParameters?${params}`,
    method: 'POST',
  };

  return new Promise((fulfill, reject) => {
    const req = http.request(options);
    req.end();
    fulfill();
  });
}

// punterをランダム選択
// num: 選択数
// return: 選択punter_ids
function pickPunter(num) {
  return db.punters().then((data) => {
    let res = [];
    for (let i=0; i<num; i++) {
      Array.prototype.push.apply(res, data.splice(data.length*Math.random()<<0, 1));
    }
    return res;
  });
}

// mapをランダム選択
function pickMap() {
  return db.maps().then((data) => data.splice(data.length*Math.random()<<0, 1)[0]);
}

function randomLeague({ids = null, playerNum = 4, gameNum = 5, map_id = "sample"} = {}) {
  db.punters().then((records) => {
    for (let i=0; i<gameNum; i++) {
      let data = ids ? ids.concat() : records.map((pid) => pid.id);
      let res = [];
      for (let i=0; i<playerNum; i++) {
        Array.prototype.push.apply(res, data.splice(data.length*Math.random()<<0, 1));
      }
      exec({punterArray: res, map_id: map_id}).then((params) => console.log(params));
    }
  })
}

module.exports = {
  getPunterNum: getPunterNum,
  exec: exec,
  pick: pickMap,
  randomLeague: randomLeague,
};
