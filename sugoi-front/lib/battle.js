require('dotenv').config();

const uuid = require('uuid/v4');
const db = require('./db');
const http = require('http');

// 対戦を実行
// id: 対戦ID
// num: 対戦人数
// map: マップID
function exec({id = uuid(), num = 2, map = "sample"} = {}) {
  let params = {};
  return pick(num).then((pids) => {
    params = {
      id: id,
      punters: pids.map((pid) => {id: pid}),
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
function postJenkins({id, map, punters}) {
  const params = [
    `game_id=${id}`,
    `map_id=${map}`,
    `punter_ids=${punters.join(',')}`,
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

module.exports = {
  pick: pick,
  exec: exec,
};
