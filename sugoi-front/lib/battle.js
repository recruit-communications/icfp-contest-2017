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
  return pick(num).then((cids) => {
    params = {
      id: id,
      clients: cids,
      map: map
    };
    return postJenkins(params);
  }).then(() => {
    return db.addBattle(params);
  }).then(() => {
    return params
  });
}

// Rundeckにジョブ登録
function postRundeck({id, map, clients}) {
  const params = [
    `authtoken=${process.env.RUNDECK_TOKEN}`,
    `argString=-game_id+${id}+-map_id+${map}+-punter_ids+${clients.join(',')}`
  ].join('&')
  const url = `http://52.198.25.234:4440/api/1/job/ea9912d0-da52-4394-883d-d3b7a10a0e3c/run?${params}`

  return new Promise((fulfill, reject) => {
    http.get(url, (res) => {
      fulfill(res);
    });
  });
}

// Jenkinsにジョブ登録
function postJenkins({id, map, clients}) {
  const params = [
    `game_id=${id}`,
    `map_id=${map}`,
    `punter_ids=${clients.join(',')}`,
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

// クライアントをランダム選択
// num: 選択数
// return: 選択クライアントIDs
function pick(num) {
  return db.clients().then((data) => {
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
