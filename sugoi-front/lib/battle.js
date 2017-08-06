require('dotenv').config();

const uuid = require('uuid/v4');
const db = require('./db');
const request = require('request');

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
  }).then((jobUrl) => {
    params.job = {url: jobUrl};
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
    uri: `http://52.198.25.234:8080/job/run/buildWithParameters?${params}`
  };

  return new Promise((fulfill, reject) => {
    request.post(options, (err, res, body) => {
      if (err) {
        reject(err);
        return;
      }
      const queue = `${res.caseless.dict.location}api/json`;

      // queueはすぐ処理されないので、waitを入れてリクエスト
      setTimeout(() => {
        request.get({uri: queue}, (err, res, body) => {
          if (err) {
            // ジョブ自体は登録されているはずなので成功扱いでも良いのかも？
            reject(err);
            return;
          }
          fulfill(JSON.parse(body).executable.url);
        });
      }, 10000);
    });
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
  postJenkins: postJenkins,
};
