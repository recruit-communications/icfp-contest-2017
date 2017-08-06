require('dotenv').config();

const uuid = require('uuid/v4');
const db = require('./db');
const http = require('http');

// 対戦を実行
// id: 対戦ID
// num: 対戦人数
// map: マップID
// punterArray: punterIDのarray
function exec({id = uuid(), num = 2, map = "sample", punterArray = null} = {}) {
  let params = {};
  return pick(num).then((punters) => {
    params = {
      id: id,
      punter_ids: punterArray || punters.map((p) => p.id),
      map: map
    };
    return postJenkins(params);
  }).then(() => {
    return db.addGame(params);
  }).then(() => {
    return params
  });
}

// Jenkinsにジョブ登録
function postJenkins({id, map, punter_ids}) {
  const params = [
    `game_id=${id}`,
    `map_id=${map}`,
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
function pick(num) {
  return db.punters().then((data) => {
    let res = [];
    for (let i=0; i<num; i++) {
      Array.prototype.push.apply(res, data.splice(data.length*Math.random()<<0, 1));
    }
    return res;
  });
}

function randomLeague({ids = null, playerNum = 4, gameNum = 5, map = "sample"} = {}) {
  db.punters().then((records) => {
    for (let i=0; i<gameNum; i++) {
      let data = ids ? ids.concat() : records.map((pid) => pid.id);
      let res = [];
      for (let i=0; i<playerNum; i++) {
        Array.prototype.push.apply(res, data.splice(data.length*Math.random()<<0, 1));
      }
      exec({punterArray: res}).then((params) => console.log(params));
    }
  })
}

module.exports = {
  pick: pick,
  exec: exec,
  randomLeague: randomLeague,
};
